import { BrowserRouter, Routes, Route, Navigate } from 'react-router';
import { useAuth } from './AuthContext';
import Layout       from './components/Layout';
import LoginPage    from './pages/LoginPage';
import Dashboard    from './pages/Dashboard';
import UsersPage    from './pages/UsersPage';
import RolesPage    from './pages/RolesPage';
import ProductsPage from './pages/ProductsPage';
import AuditPage    from './pages/AuditPage';

function PrivateRoute({ children }) {
  const { token } = useAuth();
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<PrivateRoute><Layout /></PrivateRoute>}>
          <Route index                  element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard"       element={<Dashboard />} />
          <Route path="users"           element={<UsersPage />} />
          <Route path="roles"           element={<RolesPage />} />
          <Route path="products"        element={<ProductsPage />} />
          <Route path="audit"           element={<AuditPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
