import { useState } from 'react';

// Add-comment form (FR-8). Author optional (backend defaults to "agent").
export default function CommentForm({ onSubmit, busy }) {
  const [body, setBody] = useState('');
  const [author, setAuthor] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!body.trim()) return;
    const payload = { body };
    if (author.trim()) payload.author = author.trim();
    const ok = await onSubmit(payload);
    if (ok) {
      setBody('');
    }
  }

  return (
    <form className="comment-form" onSubmit={handleSubmit}>
      <textarea
        placeholder="Write a comment..."
        value={body}
        onChange={(e) => setBody(e.target.value)}
        rows={3}
      />
      <div className="comment-form-row">
        <input
          type="text"
          placeholder="Author (optional)"
          value={author}
          onChange={(e) => setAuthor(e.target.value)}
        />
        <button className="btn btn-primary" type="submit" disabled={busy || !body.trim()}>
          Add comment
        </button>
      </div>
    </form>
  );
}
