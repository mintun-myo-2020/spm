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
}

export interface TopicProgressSummaryDTO {
  topicId: string;
  topicName: string;
  testCount: number;
  averagePercentage: number;
  latestPercentage: number;
  trend: 'IMPROVING' | 'DECLINING' | 'STABLE';
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
  reportUrl: string;
  generatedAt: string;
  expiresAt: string;
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
  overallTrend: 'IMPROVING' | 'DECLINING' | 'STABLE';
}

export interface TopicStat {
  topicId: string;
  topicName: string;
  averagePercentage: number;
  trend: 'IMPROVING' | 'DECLINING' | 'STABLE';
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
