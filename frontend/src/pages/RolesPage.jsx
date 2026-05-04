import { useEffect, useState } from 'react';
import { api } from '../api';

export default function RolesPage() {
  const [roles, setRoles] = useState([]);
  const [form, setForm]   = useState({ name: '', description: '' });
  const [editId, setEditId] = useState(null);
  const [error, setError]   = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    try { setRoles(await api.getRoles()); }
    catch (e) { setError(e.message); }
  }

  async function save(e) {
    e.preventDefault();
    try {
      if (editId) { await api.updateRole(editId, form); setEditId(null); }
      else        { await api.createRole(form); }
      setForm({ name: '', description: '' });
      load();
    } catch (e) { setError(e.message); }
  }

  async function remove(id) {
    if (!confirm('¿Eliminar rol?')) return;
    try { await api.deleteRole(id); load(); }
    catch (e) { setError(e.message); }
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Gestión de Roles (RBAC)</h2>
      {error && <p className="text-red-500 mb-3">{error}</p>}

      <form onSubmit={save} className="bg-white p-4 rounded-xl shadow mb-6 flex gap-3">
        <input className="input flex-1" placeholder="Nombre del rol" value={form.name} onChange={e => setForm({...form,name:e.target.value})} required />
        <input className="input flex-1" placeholder="Descripción" value={form.description} onChange={e => setForm({...form,description:e.target.value})} />
        <button className="btn-primary">{editId ? 'Actualizar' : 'Crear'}</button>
        {editId && <button type="button" className="btn-secondary" onClick={() => setEditId(null)}>Cancelar</button>}
      </form>

      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
            <tr>{['ID','Nombre','Descripción','Permisos','Acciones'].map(h=><th key={h} className="px-4 py-2 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {roles.map(r => (
              <tr key={r.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-2">{r.id}</td>
                <td className="px-4 py-2 font-medium">{r.name}</td>
                <td className="px-4 py-2 text-gray-500">{r.description}</td>
                <td className="px-4 py-2">
                  <div className="flex flex-wrap gap-1">
                    {(r.permissions||[]).map(p => (
                      <span key={p.id} className="bg-green-100 text-green-700 px-1 rounded text-xs">
                        {p.resource}.{p.action}
                      </span>
                    ))}
                  </div>
                </td>
                <td className="px-4 py-2 flex gap-2">
                  <button className="btn-sm-blue" onClick={() => { setEditId(r.id); setForm({name:r.name,description:r.description||''}); }}>Editar</button>
                  <button className="btn-sm-red"  onClick={() => remove(r.id)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
