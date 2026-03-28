export interface UserInfo {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  profileId: string;
  profileType: 'TEACHER' | 'PARENT' | 'STUDENT' | 'ADMIN';
  linkedStudents: LinkedStudent[];
}

export interface LinkedStudent {
  studentId: string;
  studentName: string;
}

export interface TeacherDTO {
  id: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  specialization: string;
  isActive: boolean;
  createdAt: string;
}

export interface ParentDTO {
  id: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  studentId: string;
  studentName: string;
  preferredContactMethod: 'EMAIL' | 'SMS' | 'BOTH';
  emailNotificationsEnabled: boolean;
  smsNotificationsEnabled: boolean;
  isActive: boolean;
  createdAt: string;
}

export interface StudentDTO {
  id: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  grade: string;
  enrollmentDate: string;
  parentId: string | null;
  parentName: string;
  isActive: boolean;
  createdAt: string;
}

export interface ClassDTO {
  id: string;
  name: string;
  subjectId: string;
  subjectName: string;
  teacherId: string;
  teacherName: string;
  description: string;
  maxStudents: number;
  currentStudentCount: number;
  isActive: boolean;
  createdAt: string;
}

export interface ClassDetailDTO extends ClassDTO {
  students: ClassStudentDTO[];
}

export interface ClassStudentDTO {
  id: string;
  name: string;
  email: string;
  enrollmentDate: string;
  status: 'ACTIVE' | 'WITHDRAWN';
}

export interface EnrollmentDTO {
  id: string;
  classId: string;
  studentId: string;
  studentName: string;
  enrollmentDate: string;
  status: 'ACTIVE' | 'WITHDRAWN';
}

export interface SubjectDTO {
  id: string;
  name: string;
  code: string;
  description: string;
  isDefault: boolean;
  isActive: boolean;
  topicCount: number;
}

export interface TopicDTO {
  id: string;
  name: string;
  code: string;
  description: string;
  isDefault: boolean;
  isActive: boolean;
}

export interface SubjectDetailDTO extends SubjectDTO {
  topics: TopicDTO[];
}

export interface SubQuestionDTO {
  id: string;
  label: string;
  score: number;
  maxScore: number;
  topicId: string;
  topicName: string;
  studentAnswer: string | null;
  teacherRemarks: string | null;
}

export interface McqOptionDTO {
  key: string;
  text: string;
}

export interface QuestionDTO {
  id: string;
  questionNumber: string;
  maxScore: number;
  questionText: string | null;
  questionType: 'OPEN' | 'MCQ';
  mcqOptions: McqOptionDTO[];
  subQuestions: SubQuestionDTO[];
}

export interface TestScoreDTO {
  id: string;
  studentId: string;
  studentName: string;
  classId: string;
  className: string;
  teacherId: string;
  teacherName: string;
  testName: string;
  testDate: string;
  overallScore: number;
  maxScore: number;
  testSource: 'SCHOOL' | 'CENTRE';
  questions: QuestionDTO[];
  createdAt: string;
  updatedAt: string;
}

export interface FeedbackDTO {
  id: string;
  testScoreId: string;
  teacherId: string;
  teacherName: string;
  studentId: string;
  strengths: string;
  areasForImprovement: string;
  recommendations: string;
  additionalNotes: string;
  isEdited: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TestScoreDetailDTO extends TestScoreDTO {
  feedback: FeedbackDTO | null;
}

export interface FeedbackTemplateDTO {
  id: string;
  category: 'STRENGTHS' | 'IMPROVEMENTS' | 'RECOMMENDATIONS' | 'GENERAL';
  title: string;
  content: string;
  isSystemWide: boolean;
  teacherId: string | null;
}

export interface OverallProgressDTO {
  studentId: string;
  studentName: string;
  trendData: TrendDataPoint[];
  averageScore: number;
  improvementVelocity: ImprovementVelocity | null;
}

export interface TrendDataPoint {
  testDate: string;
  testName: string;
  score: number;
}

export interface ImprovementVelocity {
  improvement: number;
  velocityPerMonth: number;
  firstAverage: number;
  recentAverage: number;
}

export interface TopicProgressDTO {
  studentId: string;
  topicId: string;
  topicName: string;
  trendData: TopicTrendDataPoint[];
  averagePercentage: number;
}

export interface TopicTrendDataPoint {
  testScoreId: string;
  testDate: string;
  testName: string;
  topicScore: number;
  topicMaxScore: number;
  percentage: number;
  questionCount: number;
}

export type Trend = 'IMPROVING' | 'DECLINING' | 'STABLE' | 'INSUFFICIENT_DATA';

export interface TopicProgressSummaryDTO {
  topicId: string;
  topicName: string;
  testCount: number;
  questionCount: number;
  averagePercentage: number;
  latestPercentage: number;
  trend: Trend;
}

export interface NotificationDTO {
  id: string;
  type: string;
  channel: 'EMAIL' | 'SMS';
  subject: string;
  message: string;
  status: 'PENDING' | 'SENT' | 'FAILED';
  sentAt: string;
  relatedEntityType: string;
  relatedEntityId: string;
  createdAt: string;
}

export interface NotificationPreferencesDTO {
  emailNotificationsEnabled: boolean;
  smsNotificationsEnabled: boolean;
  preferredContactMethod: 'EMAIL' | 'SMS' | 'BOTH';
}

export interface ProgressReportDTO {
  id: string;
  studentId: string;
  reportType: string;
  startDate: string;
  endDate: string;
  reportUrl: string | null;
  generatedAt: string;
  expiresAt: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  planJson: string | null;
  errorMessage: string | null;
}

// Structured plan parsed from planJson
export interface ImprovementPlan {
  studentName: string;
  subjectName: string;
  strengths: PlanStrength[];
  improvementAreas: PlanImprovementArea[];
  actionPlan: PlanActionItem[];
  periodComparisons: PlanPeriodComparison[];
  overallSummary: string;
}

export interface PlanStrength {
  topic: string;
  description: string;
  evidence: string;
}

export interface PlanImprovementArea {
  topic: string;
  description: string;
  evidence: string;
  suggestedApproach: string;
}

export interface PlanActionItem {
  priority: number;
  action: string;
  targetTopic: string;
  timeframe: string;
  expectedOutcome: string;
  completed: boolean;
}

export interface PlanPeriodComparison {
  topic: string;
  previousPeriod: string;
  previousAvgPercent: number;
  currentAvgPercent: number;
  change: number;
  commentary: string;
}

export interface ClassSummaryDTO {
  classId: string;
  studentCount: number;
  testCount: number;
  meanScore: number;
  medianScore: number;
  strongestTopic: TopicStat | null;
  weakestTopic: TopicStat | null;
  topicStats: TopicStat[];
  overallTrend: Trend;
}

export interface TopicStat {
  topicId: string;
  topicName: string;
  averagePercentage: number;
  trend: Trend;
}

// --- OCR Test Paper Upload Types ---

export interface TestPaperUploadDTO {
  uploadId: string;
  testScoreId: string | null;
  studentId: string;
  classId: string;
  status: 'UPLOADED' | 'PROCESSING' | 'COMPLETED' | 'PARTIALLY_FAILED' | 'FAILED';
  pages: TestPaperPageDTO[];
  aggregatedQuestions: AggregatedQuestion[];
  createdAt: string;
}

export interface TestPaperPageDTO {
  pageId: string;
  pageNumber: number;
  fileName: string;
  contentType: string;
  fileSizeBytes: number;
  status: 'PENDING' | 'EXTRACTING' | 'PARSING' | 'COMPLETED' | 'FAILED';
  fileUrl: string;
  extractedText: string | null;
  ocrConfidence: number | null;
  parsedResult: ParsedResultDTO | null;
  createdAt: string;
}

export interface ParsedResultDTO {
  questions: ParsedQuestionDTO[];
  totalDetectedMarks: number | null;
  parsingNotes: string[];
}

export interface ParsedQuestionDTO {
  questionNumber: string;
  questionText: string;
  questionType: string;
  mcqOptions: { key: string; text: string }[];
  maxScore: number | null;
  subQuestions: ParsedSubQuestionDTO[];
  confidence: number;
  rawTextSpan: string;
}

export interface ParsedSubQuestionDTO {
  label: string;
  questionText: string;
  maxScore: number | null;
  studentAnswer: string | null;
  confidence: number;
}

export interface AggregatedQuestion {
  questionNumber: string;
  questionText: string;
  questionType: string;
  maxScore: number | null;
  subQuestions: { label: string; questionText: string; maxScore: number | null; studentAnswer: string | null; confidence: number }[];
  mcqOptions: { key: string; text: string }[];
  confidence: number;
  sourcePage: number;
}


// --- Class Scheduling & Attendance Types ---

export type SessionStatus = 'SCHEDULED' | 'CANCELLED' | 'COMPLETED';
export type AttendanceStatus = 'UNMARKED' | 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';
export type RsvpStatus = 'ATTENDING' | 'NOT_ATTENDING';

export interface ScheduleDTO {
  id: string;
  classId: string;
  className: string;
  dayOfWeek: number | null;
  dayOfWeekName: string | null;
  startTime: string;
  endTime: string;
  location: string | null;
  isRecurring: boolean;
  effectiveFrom: string;
  effectiveUntil: string | null;
  sessionCount: number;
  createdAt: string;
}

export interface SessionDTO {
  id: string;
  scheduleId: string | null;
  classId: string;
  className: string;
  sessionDate: string;
  dayOfWeekName: string;
  startTime: string;
  endTime: string;
  location: string | null;
  status: SessionStatus;
  cancelReason: string | null;
  enrolledCount: number;
  markedCount: number;
  notAttendingRsvpCount: number;
  createdAt: string;
}

export interface SessionDetailDTO extends Omit<SessionDTO, 'enrolledCount' | 'markedCount' | 'notAttendingRsvpCount'> {
  attendance: AttendanceDTO[];
}

export interface AttendanceDTO {
  id: string;
  sessionId: string;
  studentId: string;
  studentName: string;
  status: AttendanceStatus;
  studentRsvp: RsvpStatus;
  rsvpReason: string | null;
  markedBy: string | null;
  markedAt: string | null;
}

export interface SessionUpdateResponseDTO {
  session: SessionDTO;
  warnings: string[];
}

export interface StudentAttendanceStatsDTO {
  studentId: string;
  studentName: string;
  classId: string;
  totalSessions: number;
  presentCount: number;
  absentCount: number;
  lateCount: number;
  excusedCount: number;
  unmarkedCount: number;
  attendanceRate: number;
}

export interface ClassAttendanceStatsDTO {
  classId: string;
  className: string;
  totalSessions: number;
  sessionsWithAttendance: number;
  averageAttendanceRate: number;
  studentStats: StudentAttendanceStatsDTO[];
}
