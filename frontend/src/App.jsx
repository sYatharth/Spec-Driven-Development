import { Routes, Route, Link } from 'react-router-dom';
import TicketListPage from './pages/TicketListPage';
import CreateTicketPage from './pages/CreateTicketPage';
import TicketDetailPage from './pages/TicketDetailPage';
import ThemeToggle from './components/ThemeToggle';

export default function App() {
  return (
    <div className="container">
      <header className="app-bar">
        <Link to="/" className="app-brand">🎫 Support Tickets</Link>
        <ThemeToggle />
      </header>
      <Routes>
        <Route path="/" element={<TicketListPage />} />
        <Route path="/tickets/new" element={<CreateTicketPage />} />
        <Route path="/tickets/:id" element={<TicketDetailPage />} />
      </Routes>
    </div>
  );
}
