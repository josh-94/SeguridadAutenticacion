const API_URL = 'http://localhost:8080/api';

function getToken() {
  return localStorage.getItem('token');
}

async function request(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({ reason: res.statusText }));
    throw new Error(err.reason || 'Error desconocido');
  }
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  // AUTH
  login:     (data)        => request('POST', '/auth/login', data),
  verifyMfa: (data)        => request('POST', '/auth/mfa/verify', data),
  setupMfa:  ()            => request('POST', '/auth/mfa/setup'),

  // USUARIOS
  getUsers:      ()         => request('GET',    '/admin/users'),
  createUser:    (data)     => request('POST',   '/admin/users', data),
  updateUser:    (id, data) => request('PUT',    `/admin/users/${id}`, data),
  deleteUser:    (id)       => request('DELETE', `/admin/users/${id}`),
  assignRole:    (uid, rid) => request('POST',   `/admin/users/${uid}/roles/${rid}`),
  removeRole:    (uid, rid) => request('DELETE', `/admin/users/${uid}/roles/${rid}`),

  // ROLES
  getRoles:      ()         => request('GET',    '/admin/roles'),
  createRole:    (data)     => request('POST',   '/admin/roles', data),
  updateRole:    (id, data) => request('PUT',    `/admin/roles/${id}`, data),
  deleteRole:    (id)       => request('DELETE', `/admin/roles/${id}`),

  // PRODUCTOS
  getProducts:   ()         => request('GET',    '/products'),
  createProduct: (data)     => request('POST',   '/products', data),
  updateProduct: (id, data) => request('PUT',    `/products/${id}`, data),
  deleteProduct: (id)       => request('DELETE', `/products/${id}`),

  // AUDITORIA
  getAuditLogs:  ()         => request('GET',    '/admin/audit'),
};
