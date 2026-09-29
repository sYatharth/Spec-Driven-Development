import { allowedActions, isTerminal } from '../lib/statusMachine';

// Renders ONLY the transitions allowed from the current status (FR-11).
// Backend still enforces; a 409 surfaces via the caller's ErrorBanner (FR-12/15).
export default function StatusActions({ status, onChange, busy }) {
  const actions = allowedActions(status);

  if (isTerminal(status)) {
    return (
      <p className="muted">
        This ticket is {status === 'CLOSED' ? 'closed' : 'cancelled'} — no further status changes.
      </p>
    );
  }

  return (
    <div className="status-actions">
      {actions.map((a) => (
        <button
          key={a.to}
          className="btn"
          disabled={busy}
          onClick={() => onChange(a.to)}
          title={`${status} → ${a.to}`}
        >
          {a.label}
        </button>
      ))}
    </div>
  );
}
