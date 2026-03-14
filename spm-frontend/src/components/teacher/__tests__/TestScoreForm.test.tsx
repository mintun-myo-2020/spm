import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { TestScoreForm } from '../TestScoreForm';

// Mock services
vi.mock('../../../services/testScoreService', () => ({
  testScoreService: { createTestScore: vi.fn() },
}));

vi.mock('../../../services/subjectService', () => ({
  subjectService: {
    getSubjects: vi.fn(() => Promise.resolve({ data: { data: [] } })),
    getSubjectWithTopics: vi.fn(),
  },
}));

vi.mock('../../shared/Toast', () => ({
  useToast: () => ({ showToast: vi.fn() }),
}));

function renderForm() {
  return render(
    <MemoryRouter initialEntries={['/teacher/classes/c1/students/s1/score']}>
      <Routes>
        <Route path="/teacher/classes/:classId/students/:studentId/score" element={<TestScoreForm />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('TestScoreForm', () => {
  it('should render the form after loading', async () => {
    renderForm();
    // Initially shows loading, then the form
    expect(await screen.findByTestId('test-score-form')).toBeInTheDocument();
  });

  it('should render test name and date inputs', async () => {
    renderForm();
    expect(await screen.findByTestId('test-name-input')).toBeInTheDocument();
    expect(screen.getByTestId('test-date-input')).toBeInTheDocument();
  });

  it('should render add question button', async () => {
    renderForm();
    expect(await screen.findByTestId('add-question-button')).toBeInTheDocument();
  });
});
