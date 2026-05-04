import { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { useAuth } from '../AuthContext';
import { api } from '../api';

export default function Dashboard() {
  const { user } = useAuth();
  const authorities = user?.authorities ?? [];
  const [mfaData, setMfaData]     = useState(null);
  const [mfaLoading, setMfaLoading] = useState(false);
  const [mfaError, setMfaError]   = useState('');

  async function handleSetupMfa() {
    setMfaLoading(true);
    setMfaError('');
    try {
      const res = await api.setupMfa();
      setMfaData(res);
    } catch (err) {
      setMfaError(err.message);
    } finally {
      setMfaLoading(false);
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">Bienvenido, {user?.sub}</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card title="Roles" value={authorities.filter(a => a.startsWith('ROLE_')).map(a => a.replace('ROLE_','')).join(', ')} />
        <Card title="Permisos activos" value={authorities.filter(a => !a.startsWith('ROLE_')).length} />
        <Card title="Estado" value="Autenticado ✓" green />
      </div>
      <div className="mt-6 bg-white rounded-xl p-4 shadow">
        <h3 className="font-semibold mb-2 text-gray-700">Todos tus permisos</h3>
        <div className="flex flex-wrap gap-2">
          {authorities.map(a => (
            <span key={a} className="bg-blue-100 text-blue-700 px-2 py-1 rounded text-xs">{a}</span>
          ))}
        </div>
      </div>

      {/* Sección MFA Setup */}
      <div className="mt-6 bg-white rounded-xl p-4 shadow">
        <h3 className="font-semibold mb-2 text-gray-700">Autenticación de dos factores (MFA)</h3>
        {!mfaData ? (
          <div>
            <p className="text-sm text-gray-500 mb-3">
              Activa MFA para proteger tu cuenta con un segundo factor usando Google Authenticator o similar.
            </p>
            <button className="btn-primary" onClick={handleSetupMfa} disabled={mfaLoading}>
              {mfaLoading ? 'Generando QR...' : 'Activar MFA'}
            </button>
            {mfaError && <p className="mt-2 text-red-500 text-sm">{mfaError}</p>}
          </div>
        ) : (
          <div className="flex flex-col items-center gap-4">
            <p className="text-sm text-gray-600 text-center">
              Escanea este código QR con <strong>Google Authenticator</strong> o <strong>Microsoft Authenticator</strong>
            </p>
            <QRCodeSVG value={mfaData.otpAuthUrl} size={200} />
            <p className="text-xs text-gray-400 text-center">
              O ingresa el secreto manualmente: <code className="bg-gray-100 px-1 rounded">{mfaData.secret}</code>
            </p>
            <p className="text-xs text-green-600 font-medium">
              ✓ MFA activado. La próxima vez que inicies sesión se pedirá el código.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

function Card({ title, value, green }) {
  return (
    <div className={`bg-white rounded-xl p-4 shadow border-l-4 ${green ? 'border-green-500' : 'border-blue-500'}`}>
      <p className="text-xs text-gray-500 uppercase">{title}</p>
      <p className="text-xl font-bold mt-1">{value}</p>
    </div>
  );
}
