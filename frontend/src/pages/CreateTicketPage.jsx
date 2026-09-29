import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { PRIORITIES } from '../lib/statusMachine';
import ErrorBanner from '../components/ErrorBanner';

// Create ticket (FR-1). Backend is source of truth for validation; 400 field
// errors are mapped to inline messages (FR-14/15).
export default function CreateTicketPage() {
  const [form, setForm] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    assignee: '',
  });
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setFieldErrors({});
    try {
      const payload = {
        title: form.title,
        description: form.description,
        priority: form.priority,
      };
      if (form.assignee.trim()) payload.assignee = form.assignee.trim();
      const created = await api.createTicket(payload);
      navigate(`/tickets/${created.id}`);
    } catch (err) {
      setError(err);
      if (err.fieldMap) setFieldErrors(err.fieldMap());
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <div className="page-head">
        <h1>New Ticket</h1>
        <Link to="/" className="btn">
          ← Back
        </Link>
      </div>

      <ErrorBanner error={error} onDismiss={() => setError(null)} />

      <form className="card form" onSubmit={handleSubmit}>
        <label>
          Title *
          <input
            type="text"
            value={form.title}
            onChange={(e) => update('title', e.target.value)}
          />
          {fieldErrors.title && <span className="field-error">⚠ {fieldErrors.title}</span>}
        </label>

        <label>
          Description *
          <textarea
            rows={5}
            value={form.description}
            onChange={(e) => update('description', e.target.value)}
          />
          {fieldErrors.description && (
            <span className="field-error">⚠ {fieldErrors.description}</span>
          )}
        </label>

        <div className="form-row">
          <label>
            Priority
            <select value={form.priority} onChange={(e) => update('priority', e.target.value)}>
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </label>

          <label>
            Assignee
            <input
              type="text"
              value={form.assignee}
              onChange={(e) => update('assignee', e.target.value)}
              placeholder="(optional)"
            />
            {fieldErrors.assignee && (
              <span className="field-error">⚠ {fieldErrors.assignee}</span>
            )}
          </label>
        </div>

        <button className="btn btn-primary" type="submit" disabled={busy}>
          {busy ? 'Creating…' : 'Create ticket'}
        </button>
      </form>
    </div>
  );
}
