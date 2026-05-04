import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [user, setUser]   = useState(() => {
    const t = localStorage.getItem('token');
    if (!t) return null;
    try {
      return JSON.parse(atob(t.split('.')[1]));
    } catch { return null; }
  });

  function saveToken(newToken) {
    localStorage.setItem('token', newToken);
    setToken(newToken);
    try {
      setUser(JSON.parse(atob(newToken.split('.')[1])));
    } catch { setUser(null); }
  }

  function logout() {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ token, user, saveToken, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
