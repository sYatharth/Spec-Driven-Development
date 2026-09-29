function formatDate(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

export default function CommentList({ comments }) {
  if (!comments || comments.length === 0) {
    return <p className="muted">No comments yet.</p>;
  }
  return (
    <ul className="comment-list">
      {comments.map((c) => (
        <li key={c.id} className="comment">
          <div className="comment-meta">
            <strong>{c.author}</strong>
            <span className="muted"> · {formatDate(c.createdAt)}</span>
          </div>
          <div className="comment-body">{c.body}</div>
        </li>
      ))}
    </ul>
  );
}
