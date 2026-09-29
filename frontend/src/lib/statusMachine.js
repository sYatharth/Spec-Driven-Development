// Client-side MIRROR of the backend state machine (spec/state-machine.md).
// UX ONLY: used to show just the allowed next actions. The backend remains the
// single source of truth and enforcement point (FR-11/12). If these ever drift,
// the backend still rejects invalid transitions with 409.

export const STATUSES = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED'];

export const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

// from -> [{ to, label }]
const TRANSITIONS = {
  OPEN: [
    { to: 'IN_PROGRESS', label: 'Start' },
    { to: 'CANCELLED', label: 'Cancel' },
  ],
  IN_PROGRESS: [
    { to: 'RESOLVED', label: 'Resolve' },
    { to: 'CANCELLED', label: 'Cancel' },
  ],
  RESOLVED: [{ to: 'CLOSED', label: 'Close' }],
  CLOSED: [],
  CANCELLED: [],
};

export function allowedActions(status) {
  return TRANSITIONS[status] || [];
}

export function isTerminal(status) {
  return allowedActions(status).length === 0;
}
