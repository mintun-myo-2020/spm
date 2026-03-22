import type { Column } from './DataTable';
import type { TestScoreDTO } from '../../types/domain';

const sourceColumn: Column<TestScoreDTO> = {
  key: 'testSource', header: 'Source', render: (r) => r.testSource === 'SCHOOL' ? 'School' : 'Centre',
};

const baseColumns: Column<TestScoreDTO>[] = [
  { key: 'testName', header: 'Test' },
  sourceColumn,
];

const dateScoreColumns: Column<TestScoreDTO>[] = [
  { key: 'testDate', header: 'Date', render: (r) => new Date(r.testDate).toLocaleDateString() },
  { key: 'overallScore', header: 'Score', render: (r) => `${r.overallScore}/${r.maxScore}` },
];

/** Columns for views scoped to a single class (teacher, admin) */
export const testScoreColumns: Column<TestScoreDTO>[] = [
  ...baseColumns,
  ...dateScoreColumns,
];

/** Columns for cross-class views (student, parent) — includes Class column */
export const testScoreColumnsWithClass: Column<TestScoreDTO>[] = [
  ...baseColumns,
  { key: 'className', header: 'Class' },
  ...dateScoreColumns,
];
