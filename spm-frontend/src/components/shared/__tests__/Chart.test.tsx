import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Chart } from '../Chart';

// Recharts uses SVG internally; we test the wrapper behavior
describe('Chart', () => {
  const data = [
    { month: 'Jan', score: 70 },
    { month: 'Feb', score: 80 },
    { month: 'Mar', score: 85 },
  ];

  const lines = [{ dataKey: 'score', name: 'Score', color: '#3b82f6' }];

  it('should render the chart container', () => {
    render(<Chart data={data} xAxisKey="month" lines={lines} />);
    expect(screen.getByTestId('chart')).toBeInTheDocument();
  });

  it('should render the title when provided', () => {
    render(<Chart data={data} xAxisKey="month" lines={lines} title="Progress" />);
    expect(screen.getByText('Progress')).toBeInTheDocument();
  });

  it('should not render title when not provided', () => {
    render(<Chart data={data} xAxisKey="month" lines={lines} />);
    expect(screen.queryByRole('heading')).not.toBeInTheDocument();
  });
});
