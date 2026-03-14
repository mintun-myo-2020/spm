export interface CreateTeacherForm {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  phoneNumber?: string;
  specialization?: string;
}

export interface CreateParentForm {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  phoneNumber?: string;
  studentId: string;
}

export interface CreateStudentForm {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  dateOfBirth?: string;
  grade?: string;
}

export interface CreateClassForm {
  name: string;
  subjectId: string;
  teacherId: string;
  description?: string;
  maxStudents?: number;
}

export interface SubQuestionInput {
  label: string;
  score: number;
  maxScore: number;
  topicId: string;
}

export interface QuestionInput {
  questionNumber: string;
  maxScore: number;
  subQuestions: SubQuestionInput[];
}

export interface CreateTestScoreForm {
  studentId: string;
  classId: string;
  testName: string;
  testDate: string;
  overallScore: number;
  maxScore?: number;
  questions: QuestionInput[];
}

export interface CreateFeedbackForm {
  strengths?: string;
  areasForImprovement?: string;
  recommendations?: string;
  additionalNotes?: string;
}

export interface CreateSubjectForm {
  name: string;
  code: string;
  description?: string;
}

export interface CreateTopicForm {
  name: string;
  code: string;
  description?: string;
}

export interface UpdateNotificationPreferencesForm {
  emailNotificationsEnabled: boolean;
  smsNotificationsEnabled: boolean;
  preferredContactMethod: 'EMAIL' | 'SMS' | 'BOTH';
}

export interface GenerateReportForm {
  reportType: string;
  startDate?: string;
  endDate?: string;
  selectedTestIds?: string[];
}
