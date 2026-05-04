import { Link, Outlet } from 'react-router';
import { useAuth } from '../AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();
  return (
    <div className="min-h-screen flex flex-col">
      <nav className="bg-blue-700 text-white px-6 py-3 flex items-center justify-between">
        <span className="font-bold text-lg">Tecsup Security</span>
        <div className="flex gap-4 text-sm">
          <Link to="/dashboard" className="hover:underline">Dashboard</Link>
          <Link to="/users"     className="hover:underline">Usuarios</Link>
          <Link to="/roles"     className="hover:underline">Roles</Link>
          <Link to="/products"  className="hover:underline">Productos</Link>
          <Link to="/audit"     className="hover:underline">Auditoría</Link>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs opacity-75">{user?.sub}</span>
          <button onClick={logout} className="bg-white text-blue-700 px-3 py-1 rounded text-xs font-semibold">
            Salir
          </button>
        </div>
      </nav>
      <main className="flex-1 p-6 bg-gray-50">
        <Outlet />
      </main>
    </div>
  );
}
