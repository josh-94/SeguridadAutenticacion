import { useEffect, useState } from 'react';
import { api } from '../api';

export default function AuditPage() {
  const [logs, setLogs]   = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getAuditLogs().then(setLogs).catch(e => setError(e.message));
  }, []);

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Logs de Auditoría</h2>
      {error && <p className="text-red-500 mb-3">{error}</p>}
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-xs">
          <thead className="bg-gray-100 text-gray-600 uppercase">
            <tr>{['ID','Usuario','Acción','Recurso','Recurso ID','Decisión','Motivo','IP','Fecha'].map(h=><th key={h} className="px-3 py-2 text-left">{h}</th>)}</tr>
          </thead>
          <tbody>
            {logs.map(l => (
              <tr key={l.id} className={`border-t ${l.decision==='DENY'?'bg-red-50':''}`}>
                <td className="px-3 py-2">{l.id}</td>
                <td className="px-3 py-2 font-medium">{l.username}</td>
                <td className="px-3 py-2">{l.action}</td>
                <td className="px-3 py-2">{l.resource}</td>
                <td className="px-3 py-2">{l.resourceId}</td>
                <td className="px-3 py-2">
                  <span className={`px-2 py-0.5 rounded font-semibold ${l.decision==='ALLOW'?'bg-green-100 text-green-700':'bg-red-100 text-red-700'}`}>{l.decision}</span>
                </td>
                <td className="px-3 py-2 max-w-xs truncate">{l.reason}</td>
                <td className="px-3 py-2">{l.ipAddress}</td>
                <td className="px-3 py-2 whitespace-nowrap">{l.createdAt?.replace('T',' ').substring(0,19)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
