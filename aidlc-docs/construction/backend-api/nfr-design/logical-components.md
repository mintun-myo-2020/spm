# Logical Components - Backend API

## Overview
This document defines the logical infrastructure components and their configurations required to implement the NFR requirements for the Student Progress Tracking System Backend API.

---

## 1. Compute Components

### 1.1 Application Container (ECS Fargate)

**Component**: AWS ECS Task running Spring Boot application

**Configuration**:
- **Launch Type**: Fargate (serverless containers)
- **CPU**: 0.5 vCPU (512 CPU units) initially, scale to 1 vCPU
- **Memory**: 1 GB initially, scale to 2 GB
- **Container Image**: spm-backend-api:latest from ECR
- **Port Mapping**: Container port 8080 → Host port 8080
- **Health Check**: HTTP GET /actuator/health every 30s

**Environment Variables**:
```yaml
SPRING_PROFILES_ACTIVE: prod
SPRING_DATASOURCE_URL: ${DB_URL}
SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
KEYCLOAK_ISSUER_URI: ${KEYCLOAK_URL}
AWS_REGION: ap-southeast-1
```

**Secrets** (from AWS Secrets Manager):
- Database credentials
- Keycloak client secret
- JWT signing key

**Resource Limits**:
- CPU reservation: 256 units (50%)
- Memory reservation: 512 MB (50%)
- CPU limit: 512 units (100%)
- Memory limit: 1024 MB (100%)

---

### 1.2 ECS Service

**Component**: ECS Service managing task lifecycle

**Configuration**:
- **Desired Count**: 2 tasks (high availability)
- **Min Healthy Percent**: 50% (allow 1 task down during deployment)
- **Max Percent**: 200% (allow 2 extra tasks during deployment)
- **Deployment Type**: Rolling update
- **Health Check Grace Period**: 60 seconds

**Auto Scaling**:
- **Target Tracking**: CPU 70%, Memory 80%
- **Scale Out**: Add 1 task when threshold exceeded for 5 minutes
- **Scale In**: Remove 1 task when below 30% CPU and 40% memory for 10 minutes
- **Min Tasks**: 2
- **Max Tasks**: 10
- **Cooldown**: 300 seconds

---

### 1.3 Application Load Balancer (ALB)

**Component**: AWS Application Load Balancer

**Configuration**:
- **Scheme**: Internet-facing
- **IP Address Type**: IPv4
- **Subnets**: 2 public subnets across availability zones
- **Security Group**: Allow HTTPS (443) from internet, HTTP (80) redirect to HTTPS

**Listeners**:
- **HTTPS (443)**: Forward to target group, SSL certificate from ACM
- **HTTP (80)**: Redirect to HTTPS

**Target Group**:
- **Protocol**: HTTP
- **Port**: 8080
- **Target Type**: IP (Fargate)
- **Health Check**: /actuator/health, 30s interval, 2 healthy threshold, 3 unhealthy threshold
- **Deregistration Delay**: 30 seconds
- **Stickiness**: Disabled (stateless API)

---

## 2. Database Components

### 2.1 Primary Database (RDS PostgreSQL)

**Component**: AWS RDS PostgreSQL Multi-AZ

**Configuration**:
- **Engine**: PostgreSQL 18
- **Instance Class**: db.t3.small (2 vCPU, 2 GB RAM)
- **Storage**: 100 GB General Purpose SSD (gp3)
- **IOPS**: 3000 (baseline)
- **Multi-AZ**: Yes (automatic failover)
- **Backup Retention**: 30 days
- **Backup Window**: 02:00-03:00 UTC (off-peak)
- **Maintenance Window**: Sunday 03:00-04:00 UTC

**Security**:
- **Encryption at Rest**: AES-256
- **Encryption in Transit**: SSL/TLS required
- **Security Group**: Allow PostgreSQL (5432) from ECS tasks only
- **Public Access**: No

**Parameters**:
- **max_connections**: 100
- **shared_buffers**: 512 MB
- **effective_cache_size**: 1536 MB
- **work_mem**: 5 MB
- **maintenance_work_mem**: 128 MB

---

### 2.2 Read Replica

**Component**: RDS Read Replica for reporting queries

**Configuration**:
- **Source**: Primary database
- **Instance Class**: db.t3.small (same as primary)
- **Replication**: Asynchronous
- **Lag Monitoring**: CloudWatch alarm if lag > 5 seconds
- **Promotion**: Can be promoted to standalone if primary fails

**Usage**:
- Progress calculation queries
- Report generation
- Analytics and dashboards
- Non-critical read operations

---

## 3. Storage Components

### 3.1 S3 Bucket (Reports)

**Component**: AWS S3 bucket for report storage

**Configuration**:
- **Bucket Name**: spm-reports-{centre-id}
- **Region**: ap-southeast-1 (Singapore)
- **Versioning**: Disabled
- **Encryption**: SSE-S3 (AES-256)
- **Public Access**: Blocked (all public access blocked)
- **Object Ownership**: Bucket owner enforced

**Lifecycle Policy**:
```json
{
  "Rules": [
    {
      "Id": "TransitionToIA",
      "Status": "Enabled",
      "Transitions": [
        {
          "Days": 365,
          "StorageClass": "STANDARD_IA"
        }
      ]
    },
    {
      "Id": "DeleteAfter2Years",
      "Status": "Enabled",
      "Expiration": {
        "Days": 730
      }
    }
  ]
}
```

**Access Control**:
- IAM role for ECS tasks (read/write)
- Pre-signed URLs for user access (1-hour expiration)

---

### 3.2 ECR Repository (Container Images)

**Component**: AWS Elastic Container Registry

**Configuration**:
- **Repository Name**: spm-backend-api
- **Image Tag Immutability**: Enabled
- **Scan on Push**: Enabled (vulnerability scanning)
- **Encryption**: AES-256

**Lifecycle Policy**:
```json
{
  "rules": [
    {
      "rulePriority": 1,
      "description": "Keep last 10 images",
      "selection": {
        "tagStatus": "any",
        "countType": "imageCountMoreThan",
        "countNumber": 10
      },
      "action": {
        "type": "expire"
      }
    },
    {
      "rulePriority": 2,
      "description": "Delete untagged images after 7 days",
      "selection": {
        "tagStatus": "untagged",
        "countType": "sinceImagePushed",
        "countUnit": "days",
        "countNumber": 7
      },
      "action": {
        "type": "expire"
      }
    }
  ]
}
```

---

## 4. Notification Components

### 4.1 Email Service (SES)

**Component**: AWS Simple Email Service

**Configuration**:
- **Region**: ap-southeast-1 (Singapore)
- **Verified Domain**: tuitioncentre.com
- **DKIM**: Enabled (email authentication)
- **Configuration Set**: spm-notifications (track opens, clicks, bounces)
- **Sending Limits**: 200 emails/day initially, request increase as needed

**Email Templates**:
- New test score notification
- Test score updated notification
- New feedback notification
- Feedback updated notification

**Bounce Handling**:
- SNS topic for bounces and complaints
- Automatic suppression list management

---

### 4.2 SMS Service (SNS)

**Component**: AWS Simple Notification Service

**Configuration**:
- **SMS Type**: Transactional (high priority)
- **Default Sender ID**: TuitionCtr
- **Monthly Spend Limit**: $100 per centre
- **Delivery Status Logging**: Enabled (CloudWatch Logs)

**SMS Templates**:
- New test score alert
- Test score updated alert
- New feedback alert

---

## 5. Monitoring Components

### 5.1 CloudWatch Logs

**Component**: AWS CloudWatch Logs for centralized logging

**Configuration**:
- **Log Group**: /ecs/spm-backend-api
- **Retention**: 30 days
- **Encryption**: KMS encryption
- **Export to S3**: After 30 days (90-day retention in S3)

**Log Streams**:
- One stream per ECS task
- Automatic stream creation
- JSON structured logs

---

### 5.2 CloudWatch Metrics

**Component**: AWS CloudWatch for metrics and monitoring

**Metric Namespaces**:
- **AWS/ECS**: Task CPU, memory, network
- **AWS/ApplicationELB**: Request count, latency, errors
- **AWS/RDS**: Database CPU, connections, IOPS
- **Custom/SPM**: Business metrics (test scores, logins)

**Dashboards**:
- System Health Dashboard (CPU, memory, errors)
- API Performance Dashboard (latency, throughput)
- Business Metrics Dashboard (user activity)

---

### 5.3 CloudWatch Alarms

**Component**: CloudWatch Alarms for alerting

**Critical Alarms** (PagerDuty):
- Error rate > 10%
- p95 latency > 5s
- CPU > 90%
- Memory > 95%
- Database connections > 90%
- All tasks unhealthy

**Warning Alarms** (Email + Slack):
- Error rate > 5%
- p95 latency > 2s
- CPU > 80%
- Memory > 85%
- Database connections > 80%

---

## 6. Security Components

### 6.1 Secrets Manager

**Component**: AWS Secrets Manager for sensitive credentials

**Secrets Stored**:
- **rds-credentials**: Database username and password
- **keycloak-client-secret**: Keycloak OAuth2 client secret
- **jwt-signing-key**: JWT token signing key
- **ses-smtp-credentials**: SES SMTP credentials (if needed)

**Configuration**:
- **Automatic Rotation**: Enabled for database credentials (30 days)
- **Encryption**: KMS encryption
- **Access Control**: IAM role for ECS tasks only

---

### 6.2 Security Groups

**Component**: VPC Security Groups for network isolation

**ALB Security Group**:
- Inbound: HTTPS (443) from 0.0.0.0/0
- Inbound: HTTP (80) from 0.0.0.0/0 (redirect to HTTPS)
- Outbound: HTTP (8080) to ECS tasks

**ECS Task Security Group**:
- Inbound: HTTP (8080) from ALB security group
- Outbound: PostgreSQL (5432) to RDS security group
- Outbound: HTTPS (443) to internet (Keycloak, SES, SNS)

**RDS Security Group**:
- Inbound: PostgreSQL (5432) from ECS task security group
- Outbound: None

---

### 6.3 IAM Roles

**Component**: IAM roles for service permissions

**ECS Task Execution Role**:
- Pull images from ECR
- Write logs to CloudWatch
- Read secrets from Secrets Manager

**ECS Task Role**:
- Read/write S3 (reports bucket)
- Send emails via SES
- Send SMS via SNS
- Publish metrics to CloudWatch
- Read secrets from Secrets Manager

---

## 7. Networking Components

### 7.1 VPC Configuration

**Component**: Virtual Private Cloud for network isolation

**Configuration**:
- **CIDR Block**: 10.0.0.0/16
- **Availability Zones**: 2 AZs (ap-southeast-1a, ap-southeast-1b)
- **Public Subnets**: 2 subnets (10.0.1.0/24, 10.0.2.0/24) for ALB
- **Private Subnets**: 2 subnets (10.0.11.0/24, 10.0.12.0/24) for ECS tasks
- **Database Subnets**: 2 subnets (10.0.21.0/24, 10.0.22.0/24) for RDS

**NAT Gateway**:
- 1 NAT Gateway per AZ (high availability)
- Allows private subnets to access internet (Keycloak, SES, SNS)

**Internet Gateway**:
- Attached to VPC
- Routes traffic from public subnets to internet

---

### 7.2 Route Tables

**Public Route Table**:
- 0.0.0.0/0 → Internet Gateway
- Associated with public subnets

**Private Route Table**:
- 0.0.0.0/0 → NAT Gateway
- Associated with private subnets

**Database Route Table**:
- No internet access
- Associated with database subnets

---

## 8. CI/CD Components

### 8.1 GitHub Actions Workflow

**Component**: GitHub Actions for CI/CD pipeline

**Workflow Stages**:
1. **Build**: Compile code, run unit tests
2. **Test**: Run integration tests with Testcontainers
3. **Security**: SonarQube scan, OWASP dependency check
4. **Package**: Build Docker image
5. **Push**: Push image to ECR
6. **Deploy**: Update ECS service

**Triggers**:
- Push to develop → Deploy to staging
- Push to main → Manual approval → Deploy to production

---

### 8.2 Deployment Strategy

**Component**: Rolling deployment with health checks

**Process**:
1. Build new Docker image
2. Push to ECR with new tag
3. Update ECS task definition with new image
4. ECS starts new tasks with new image
5. Wait for new tasks to pass health checks
6. ECS drains connections from old tasks
7. ECS stops old tasks
8. Deployment complete

**Rollback**:
- Automatic rollback if health checks fail
- Manual rollback by deploying previous image tag

---

## 9. Backup and Recovery Components

### 9.1 Database Backups

**Component**: RDS automated backups

**Configuration**:
- **Automated Backups**: Daily snapshots
- **Backup Window**: 02:00-03:00 UTC
- **Retention**: 30 days
- **Point-in-Time Recovery**: Enabled (5-minute granularity)
- **Cross-Region Backup**: Copy to secondary region (disaster recovery)

---

### 9.2 Application Backups

**Component**: S3 versioning and lifecycle

**Configuration**:
- **S3 Reports**: Lifecycle policy (Standard → IA → Delete)
- **ECR Images**: Keep last 10 images
- **CloudWatch Logs**: Export to S3 after 30 days

---

## 10. Cost Optimization Components

### 10.1 Reserved Instances

**Component**: RDS Reserved Instances for cost savings

**Strategy**:
- Purchase 1-year reserved instance after 3 months of stable usage
- 30% cost savings compared to on-demand
- Convertible RI for flexibility

---

### 10.2 Cost Monitoring

**Component**: AWS Cost Explorer and Budgets

**Configuration**:
- **Budget**: $400/month per centre
- **Alerts**: Email when 80% and 100% of budget
- **Cost Allocation Tags**: Environment, Centre, Service

---

## Component Interaction Diagram

```
┌─────────────┐
│   Internet  │
└──────┬──────┘
       │
       │ HTTPS
       ▼
┌─────────────────────┐
│  Application Load   │
│     Balancer        │
└──────┬──────────────┘
       │
       │ HTTP
       ▼
┌─────────────────────┐      ┌──────────────┐
│   ECS Fargate       │─────▶│  CloudWatch  │
│   (2-10 tasks)      │      │  Logs/Metrics│
└──────┬──────────────┘      └──────────────┘
       │
       ├─────────────────────┐
       │                     │
       ▼                     ▼
┌─────────────┐      ┌──────────────┐
│  RDS Multi  │      │  Secrets     │
│  -AZ + Read │      │  Manager     │
│  Replica    │      └──────────────┘
└─────────────┘
       │
       ▼
┌─────────────┐
│  S3 Bucket  │
│  (Reports)  │
└─────────────┘
       │
       ├─────────────────────┐
       │                     │
       ▼                     ▼
┌─────────────┐      ┌──────────────┐
│  SES (Email)│      │  SNS (SMS)   │
└─────────────┘      └──────────────┘
```

---

## Summary

These logical components provide:
- **Compute**: ECS Fargate with auto-scaling (2-10 tasks)
- **Database**: RDS Multi-AZ with read replica
- **Storage**: S3 for reports, ECR for images
- **Notifications**: SES for email, SNS for SMS
- **Monitoring**: CloudWatch Logs, Metrics, Alarms
- **Security**: Secrets Manager, Security Groups, IAM Roles
- **Networking**: VPC with public/private subnets, NAT Gateway
- **CI/CD**: GitHub Actions with rolling deployment
- **Backup**: RDS automated backups, S3 lifecycle policies

All components are configured for high availability, security, and cost optimization, supporting the NFR requirements defined for the system.

---

**Document Version**: 1.0  
**Created**: 2026-03-08  
**Status**: Draft
