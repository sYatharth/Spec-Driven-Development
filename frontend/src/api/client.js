// API client: thin fetch wrapper that normalizes the backend's uniform error
// body (spec/api-contract.md §9) into a consistent ApiError the UI can render
// (FR-15). No secrets here; base URL comes from VITE_API_BASE.

const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

export class ApiError extends Error {
  constructor(status, message, fieldErrors = []) {
    super(message || 'Request failed');
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors; // [{ field, message }]
  }

  /** Convenience: map fieldErrors into { field: message } for inline display. */
  fieldMap() {
    const map = {};
    for (const fe of this.fieldErrors) {
      if (fe.field && !map[fe.field]) map[fe.field] = fe.message;
    }
    return map;
  }
}

async function request(path, options = {}) {
  let res;
  try {
    res = await fetch(`${BASE}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch (networkErr) {
    // Backend unreachable / CORS / offline.
    throw new ApiError(0, 'Cannot reach the server. Is the backend running?');
  }

  if (res.status === 204) return null;

  let body = null;
  const text = await res.text();
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = null;
    }
  }

  if (!res.ok) {
    const message = body?.message || `Request failed (${res.status})`;
    const fieldErrors = body?.fieldErrors || [];
    throw new ApiError(res.status, message, fieldErrors);
  }

  return body;
}

export const api = {
  listTickets({ q, status } = {}) {
    const params = new URLSearchParams();
    if (q) params.set('q', q);
    if (status) params.set('status', status);
    const qs = params.toString();
    return request(`/tickets${qs ? `?${qs}` : ''}`);
  },

  getTicket(id) {
    return request(`/tickets/${id}`);
  },

  createTicket(payload) {
    return request('/tickets', { method: 'POST', body: JSON.stringify(payload) });
  },

  updateTicket(id, payload) {
    return request(`/tickets/${id}`, { method: 'PATCH', body: JSON.stringify(payload) });
  },

  changeStatus(id, status) {
    return request(`/tickets/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    });
  },

  addComment(id, payload) {
    return request(`/tickets/${id}/comments`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
};
