import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { STATUSES } from '../lib/statusMachine';
import StatusBadge from '../components/StatusBadge';
import ErrorBanner from '../components/ErrorBanner';

// Ticket list with keyword search (FR-9) and status filter (FR-10).
export default function TicketListPage() {
  const [tickets, setTickets] = useState([]);
  const [q, setQ] = useState('');
  const [status, setStatus] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // Debounced load whenever the query or filter changes.
  useEffect(() => {
    const handle = setTimeout(() => {
      load();
    }, 250);
    return () => clearTimeout(handle);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [q, status]);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await api.listTickets({ q: q.trim(), status: status || undefined });
      setTickets(data);
    } catch (err) {
      setError(err);
      setTickets([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div className="page-head">
        <h1>All Tickets</h1>
        <Link to="/tickets/new" className="btn btn-primary">
          + New Ticket
        </Link>
      </div>

      <div className="toolbar">
        <div className="search-field">
          <span className="search-icon" aria-hidden="true">🔍</span>
          <input
            type="search"
            className="search-input"
            placeholder="Search by keyword..."
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
        <select
          className="status-filter"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </div>

      <ErrorBanner error={error} onDismiss={() => setError(null)} />

      {loading ? (
        <p className="muted">Loading…</p>
      ) : tickets.length === 0 ? (
        <p className="muted">No tickets match your search/filter.</p>
      ) : (
        <table className="ticket-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Title</th>
              <th>Status</th>
              <th>Priority</th>
              <th>Assignee</th>
            </tr>
          </thead>
          <tbody>
            {tickets.map((t) => (
              <tr
                key={t.id}
                className="clickable"
                onClick={() => navigate(`/tickets/${t.id}`)}
              >
                <td>{t.id}</td>
                <td>{t.title}</td>
                <td>
                  <StatusBadge status={t.status} />
                </td>
                <td>{t.priority}</td>
                <td>{t.assignee || <span className="muted">—</span>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
