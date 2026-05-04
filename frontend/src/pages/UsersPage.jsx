import { useEffect, useState } from 'react';
import { api } from '../api';

export default function UsersPage() {
  const [users, setUsers]   = useState([]);
  const [roles, setRoles]   = useState([]);
  const [error, setError]   = useState('');
  const [form, setForm]     = useState({ username: '', email: '', password: '', area: '', region: '', seniority: 'junior', department: '' });
  const [editId, setEditId] = useState(null);

  useEffect(() => {
    load();
    api.getRoles().then(setRoles).catch(() => {});
  }, []);

  async function load() {
    try { setUsers(await api.getUsers()); }
    catch (e) { setError(e.message); }
  }

  async function save(e) {
    e.preventDefault();
    try {
      if (editId) { await api.updateUser(editId, form); setEditId(null); }
      else        { await api.createUser(form); }
      setForm({ username: '', email: '', password: '', area: '', region: '', seniority: 'junior', department: '' });
      load();
    } catch (e) { setError(e.message); }
  }

  async function remove(id) {
    if (!confirm('¿Eliminar usuario?')) return;
    try { await api.deleteUser(id); load(); }
    catch (e) { setError(e.message); }
  }

  async function assignRole(userId, roleId) {
    try { await api.assignRole(userId, roleId); load(); }
    catch (e) { setError(e.message); }
  }

  function startEdit(u) {
    setEditId(u.id);
    setForm({ username: u.username, email: u.email, password: '', area: u.area || '', region: u.region || '', seniority: u.seniority || 'junior', department: u.department || '' });
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Gestión de Usuarios (RBAC)</h2>
      {error && <p className="text-red-500 mb-3">{error}</p>}

      <form onSubmit={save} className="bg-white p-4 rounded-xl shadow mb-6 grid grid-cols-2 md:grid-cols-4 gap-3">
        <input className="input" placeholder="Username" value={form.username} onChange={e => setForm({...form,username:e.target.value})} required={!editId} />
        <input className="input" placeholder="Email" type="email" value={form.email} onChange={e => setForm({...form,email:e.target.value})} required />
        <input className="input" placeholder="Password" type="password" value={form.password} onChange={e => setForm({...form,password:e.target.value})} required={!editId} />
        <input className="input" placeholder="Área (ABAC)" value={form.area} onChange={e => setForm({...form,area:e.target.value})} />
        <input className="input" placeholder="Región (ABAC)" value={form.region} onChange={e => setForm({...form,region:e.target.value})} />
        <select className="input" value={form.seniority} onChange={e => setForm({...form,seniority:e.target.value})}>
          <option value="junior">Junior</option>
          <option value="senior">Senior</option>
        </select>
        <input className="input" placeholder="Departamento" value={form.department} onChange={e => setForm({...form,department:e.target.value})} />
        <div className="flex gap-2">
          <button className="btn-primary flex-1">{editId ? 'Actualizar' : 'Crear'}</button>
          {editId && <button type="button" className="btn-secondary" onClick={() => setEditId(null)}>Cancelar</button>}
        </div>
      </form>

      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
            <tr>{['ID','Usuario','Email','Área','Región','Seniority','Roles','Acciones'].map(h=><th key={h} className="px-4 py-2 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {users.map(u => (
              <tr key={u.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-2">{u.id}</td>
                <td className="px-4 py-2 font-medium">{u.username}</td>
                <td className="px-4 py-2">{u.email}</td>
                <td className="px-4 py-2">{u.area}</td>
                <td className="px-4 py-2">{u.region}</td>
                <td className="px-4 py-2">{u.seniority}</td>
                <td className="px-4 py-2">
                  <div className="flex flex-wrap gap-1">
                    {(u.roles||[]).map(r => <span key={r} className="bg-blue-100 text-blue-700 px-1 rounded text-xs">{r}</span>)}
                    <select className="text-xs border rounded px-1" onChange={e => e.target.value && assignRole(u.id, e.target.value)} defaultValue="">
                      <option value="">+rol</option>
                      {roles.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
                    </select>
                  </div>
                </td>
                <td className="px-4 py-2 flex gap-2">
                  <button className="btn-sm-blue" onClick={() => startEdit(u)}>Editar</button>
                  <button className="btn-sm-red"  onClick={() => remove(u.id)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
