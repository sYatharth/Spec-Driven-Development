import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { PRIORITIES } from '../lib/statusMachine';
import StatusBadge from '../components/StatusBadge';
import StatusActions from '../components/StatusActions';
import CommentList from '../components/CommentList';
import CommentForm from '../components/CommentForm';
import ErrorBanner from '../components/ErrorBanner';

// View + edit a ticket (FR-3,4-7), change status (FR-11/12), add comments (FR-8).
export default function TicketDetailPage() {
  const { id } = useParams();
  const [ticket, setTicket] = useState(null);
  const [form, setForm] = useState(null);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getTicket(id);
      applyTicket(data);
    } catch (err) {
      setError(err);
      setTicket(null);
    } finally {
      setLoading(false);
    }
  }

  function applyTicket(data) {
    setTicket(data);
    setForm({
      title: data.title,
      description: data.description,
      priority: data.priority,
      assignee: data.assignee || '',
    });
  }

  async function saveFields(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setFieldErrors({});
    try {
      // Send assignee explicitly: "" clears it on the backend, per contract.
      const updated = await api.updateTicket(id, {
        title: form.title,
        description: form.description,
        priority: form.priority,
        assignee: form.assignee,
      });
      applyTicket(updated);
    } catch (err) {
      setError(err);
      if (err.fieldMap) setFieldErrors(err.fieldMap());
    } finally {
      setBusy(false);
    }
  }

  async function changeStatus(target) {
    setBusy(true);
    setError(null);
    try {
      const updated = await api.changeStatus(id, target);
      applyTicket(updated);
    } catch (err) {
      // e.g. 409 invalid transition -> shown in banner (FR-15)
      setError(err);
    } finally {
      setBusy(false);
    }
  }

  async function addComment(payload) {
    setError(null);
    try {
      await api.addComment(id, payload);
      await load(); // refresh to show the new comment
      return true;
    } catch (err) {
      setError(err);
      return false;
    }
  }

  if (loading) return <p className="muted">Loading…</p>;

  if (!ticket) {
    return (
      <div>
        <Link to="/" className="btn">← Back</Link>
        <ErrorBanner error={error} onDismiss={() => setError(null)} />
      </div>
    );
  }

  return (
    <div>
      <div className="page-head">
        <Link to="/" className="btn">← Back</Link>
        <h1>Ticket #{ticket.id}</h1>
        <StatusBadge status={ticket.status} />
      </div>

      <ErrorBanner error={error} onDismiss={() => setError(null)} />

      <form className="card form" onSubmit={saveFields}>
        <label>
          Title
          <input
            type="text"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
          {fieldErrors.title && <span className="field-error">⚠ {fieldErrors.title}</span>}
        </label>

        <label>
          Description
          <textarea
            rows={5}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          {fieldErrors.description && (
            <span className="field-error">⚠ {fieldErrors.description}</span>
          )}
        </label>

        <div className="form-row">
          <label>
            Priority
            <select
              value={form.priority}
              onChange={(e) => setForm({ ...form, priority: e.target.value })}
            >
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>{p}</option>
              ))}
            </select>
          </label>
          <label>
            Assignee
            <input
              type="text"
              value={form.assignee}
              onChange={(e) => setForm({ ...form, assignee: e.target.value })}
              placeholder="(empty to clear)"
            />
          </label>
        </div>

        <button className="btn btn-primary" type="submit" disabled={busy}>
          Save changes
        </button>
      </form>

      <div className="card">
        <h3>Status</h3>
        <StatusActions status={ticket.status} onChange={changeStatus} busy={busy} />
      </div>

      <div className="card">
        <h3>Comments ({ticket.comments?.length || 0})</h3>
        <CommentList comments={ticket.comments} />
        <CommentForm onSubmit={addComment} busy={busy} />
      </div>
    </div>
  );
}
