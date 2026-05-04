import { useEffect, useState } from 'react';
import { api } from '../api';

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [error, setError]       = useState('');
  const [form, setForm]         = useState({ name:'', category:'', region:'', status:'ACTIVE', price:'' });
  const [editId, setEditId]     = useState(null);

  useEffect(() => { load(); }, []);

  async function load() {
    try { setProducts(await api.getProducts()); }
    catch (e) { setError(e.message); }
  }

  async function save(e) {
    e.preventDefault();
    setError('');
    try {
      const payload = { ...form, price: parseFloat(form.price) || 0 };
      if (editId) { await api.updateProduct(editId, payload); setEditId(null); }
      else        { await api.createProduct(payload); }
      setForm({ name:'', category:'', region:'', status:'ACTIVE', price:'' });
      load();
    } catch (e) { setError(e.message); }
  }

  async function remove(id) {
    if (!confirm('¿Eliminar producto? (se evaluará política ABAC)')) return;
    setError('');
    try { await api.deleteProduct(id); load(); }
    catch (e) { setError(e.message); }
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-1">Productos (ABAC)</h2>
      <p className="text-sm text-gray-500 mb-4">Cada acción es evaluada por el Policy Engine según tus atributos.</p>
      {error && <p className="text-red-500 mb-3 p-2 bg-red-50 rounded">{error}</p>}

      <form onSubmit={save} className="bg-white p-4 rounded-xl shadow mb-6 grid grid-cols-2 md:grid-cols-3 gap-3">
        <input className="input" placeholder="Nombre" value={form.name} onChange={e=>setForm({...form,name:e.target.value})} required />
        <input className="input" placeholder="Categoría" value={form.category} onChange={e=>setForm({...form,category:e.target.value})} />
        <input className="input" placeholder="Región" value={form.region} onChange={e=>setForm({...form,region:e.target.value})} />
        <input className="input" placeholder="Precio" type="number" value={form.price} onChange={e=>setForm({...form,price:e.target.value})} />
        <select className="input" value={form.status} onChange={e=>setForm({...form,status:e.target.value})}>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
        <div className="flex gap-2">
          <button className="btn-primary flex-1">{editId ? 'Actualizar' : 'Crear'}</button>
          {editId && <button type="button" className="btn-secondary" onClick={()=>setEditId(null)}>Cancelar</button>}
        </div>
      </form>

      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
            <tr>{['ID','Nombre','Categoría','Región','Owner','Status','Precio','Acciones'].map(h=><th key={h} className="px-4 py-2 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {products.map(p => (
              <tr key={p.id} className="border-t hover:bg-gray-50">
                <td className="px-4 py-2">{p.id}</td>
                <td className="px-4 py-2 font-medium">{p.name}</td>
                <td className="px-4 py-2">{p.category}</td>
                <td className="px-4 py-2">{p.region}</td>
                <td className="px-4 py-2">{p.owner?.username}</td>
                <td className="px-4 py-2">
                  <span className={`px-2 py-1 rounded text-xs ${p.status==='ACTIVE'?'bg-green-100 text-green-700':'bg-gray-200 text-gray-500'}`}>{p.status}</span>
                </td>
                <td className="px-4 py-2">S/ {p.price}</td>
                <td className="px-4 py-2 flex gap-2">
                  <button className="btn-sm-blue" onClick={()=>{ setEditId(p.id); setForm({name:p.name,category:p.category||'',region:p.region||'',status:p.status,price:p.price||''}); }}>Editar</button>
                  <button className="btn-sm-red"  onClick={()=>remove(p.id)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
