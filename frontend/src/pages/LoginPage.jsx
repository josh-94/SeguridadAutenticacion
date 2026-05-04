import { useState } from 'react';
import { useNavigate } from 'react-router';
import { api } from '../api';
import { useAuth } from '../AuthContext';

export default function LoginPage() {
  const [form, setForm]       = useState({ username: '', password: '' });
  const [mfaStep, setMfaStep] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);
  const { saveToken }         = useAuth();
  const navigate              = useNavigate();

  async function handleLogin(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await api.login(form);
      if (res.mfaRequired) {
        setMfaStep(true);
      } else {
        saveToken(res.token);
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleMfa(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await api.verifyMfa({ username: form.username, totpCode: mfaCode });
      saveToken(res.token);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-xl shadow-md w-full max-w-sm">
        <h1 className="text-2xl font-bold mb-6 text-center text-blue-700">Tecsup Security</h1>

        {!mfaStep ? (
          <form onSubmit={handleLogin} className="space-y-4">
            <input className="input" placeholder="Usuario" value={form.username}
              onChange={e => setForm({ ...form, username: e.target.value })} required />
            <input className="input" type="password" placeholder="Contraseña" value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })} required />
            <button className="btn-primary w-full" disabled={loading}>
              {loading ? 'Ingresando...' : 'Ingresar'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleMfa} className="space-y-4">
            <p className="text-sm text-gray-600 text-center">Ingresa el código de tu app autenticadora</p>
            <input className="input text-center text-xl tracking-widest" placeholder="000000"
              value={mfaCode} onChange={e => setMfaCode(e.target.value)} maxLength={6} required />
            <button className="btn-primary w-full" disabled={loading}>
              {loading ? 'Verificando...' : 'Verificar MFA'}
            </button>
          </form>
        )}

        {error && <p className="mt-4 text-red-500 text-sm text-center">{error}</p>}
      </div>
    </div>
  );
}
