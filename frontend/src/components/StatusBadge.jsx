// Colored status pill. Colors per spec/ui-flow.md §8.
const COLORS = {
  OPEN: '#2563eb',
  IN_PROGRESS: '#d97706',
  RESOLVED: '#16a34a',
  CLOSED: '#6b7280',
  CANCELLED: '#dc2626',
};

export default function StatusBadge({ status }) {
  return (
    <span className="badge" style={{ backgroundColor: COLORS[status] || '#6b7280' }}>
      {status}
    </span>
  );
}
