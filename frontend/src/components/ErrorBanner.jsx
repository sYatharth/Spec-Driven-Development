// Renders a human-readable error message + any field errors (FR-15).
// Never shows stack traces or raw HTTP jargon.
export default function ErrorBanner({ error, onDismiss }) {
  if (!error) return null;
  return (
    <div className="banner banner-error" role="alert">
      <div className="banner-row">
        <span>{error.message}</span>
        {onDismiss && (
          <button className="banner-close" onClick={onDismiss} aria-label="Dismiss">
            ×
          </button>
        )}
      </div>
      {error.fieldErrors?.length > 0 && (
        <ul className="banner-fields">
          {error.fieldErrors.map((fe, i) => (
            <li key={i}>
              <strong>{fe.field}</strong>: {fe.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
