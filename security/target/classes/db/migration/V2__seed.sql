-- V2__seed.sql
-- Datos de prueba: roles, permisos y usuarios iniciales

-- =============================================
-- ROLES
-- =============================================
INSERT INTO roles (name, description) VALUES
  ('ADMIN',    'Administrador del sistema: CRUD completo y gestion de roles'),
  ('OPERADOR', 'Puede consultar usuarios y roles'),
  ('CONSULTA', 'Solo lectura de usuarios');

-- =============================================
-- PERMISOS ADMINISTRATIVOS
-- =============================================
INSERT INTO permissions (resource, action, description) VALUES
  ('users',    'read',   'Listar y ver usuarios'),
  ('users',    'create', 'Crear usuarios'),
  ('users',    'update', 'Editar usuarios'),
  ('users',    'delete', 'Eliminar usuarios'),
  ('roles',    'read',   'Listar y ver roles'),
  ('roles',    'create', 'Crear roles'),
  ('roles',    'update', 'Editar roles'),
  ('roles',    'delete', 'Eliminar roles'),
  ('roles',    'assign', 'Asignar roles a usuarios'),
  ('products', 'read',   'Ver productos'),
  ('products', 'create', 'Crear productos'),
  ('products', 'update', 'Editar productos'),
  ('products', 'delete', 'Eliminar productos');

-- =============================================
-- ASIGNACION PERMISOS -> ROLES
-- ADMIN: todos los permisos
-- =============================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- OPERADOR: read de users y roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON (p.resource = 'users' AND p.action = 'read')
                   OR (p.resource = 'roles'  AND p.action = 'read')
WHERE r.name = 'OPERADOR';

-- CONSULTA: solo read de users
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.resource = 'users' AND p.action = 'read'
WHERE r.name = 'CONSULTA';

-- =============================================
-- USUARIOS DE PRUEBA
-- Password: Admin123! -> bcrypt hash
-- =============================================
INSERT INTO users (username, email, password, area, region, seniority, department, mfa_enabled, enabled) VALUES
  ('admin',    'admin@tecsup.pe',    '$2b$10$rf4OkmYXlcVLOOBpo3Or1uoHN2iAEMVxckfbNwExkNUgwTxsGnWjS', 'TI',     'LIMA',  'senior', 'tecnologia', FALSE, TRUE),
  ('operador', 'operador@tecsup.pe', '$2b$10$rf4OkmYXlcVLOOBpo3Or1uoHN2iAEMVxckfbNwExkNUgwTxsGnWjS', 'VENTAS', 'LIMA',  'junior', 'ventas',     FALSE, TRUE),
  ('consulta', 'consulta@tecsup.pe', '$2b$10$rf4OkmYXlcVLOOBpo3Or1uoHN2iAEMVxckfbNwExkNUgwTxsGnWjS', 'SOPORTE','NORTE', 'junior', 'soporte',    FALSE, TRUE);

-- ASIGNACION USUARIOS -> ROLES
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin'    AND r.name = 'ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'operador' AND r.name = 'OPERADOR';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'consulta' AND r.name = 'CONSULTA';

-- =============================================
-- PRODUCTOS DE PRUEBA
-- =============================================
INSERT INTO products (name, category, region, owner_user_id, status, price) VALUES
  ('Laptop HP',       'ELECTRONICA', 'LIMA',  1, 'ACTIVE', 2500.00),
  ('Mouse Logitech',  'ELECTRONICA', 'LIMA',  2, 'ACTIVE',   85.00),
  ('Silla Ergonomica','MUEBLES',     'NORTE', 3, 'ACTIVE',  450.00),
  ('Monitor Dell',    'ELECTRONICA', 'LIMA',  1, 'ACTIVE', 1200.00);

-- =============================================
-- POLITICAS ABAC PARA PRODUCTOS
-- =============================================
INSERT INTO abac_policies (name, description, resource, action, effect, condition_expression, priority, active) VALUES
  ('allow-select-same-region',
   'Permite leer productos si el usuario es de la misma region',
   'products', 'read', 'ALLOW',
   '{"type":"SAME_REGION"}', 10, TRUE),

  ('allow-insert-ventas',
   'Permite crear productos si el usuario es del area ventas',
   'products', 'create', 'ALLOW',
   '{"type":"USER_AREA","value":"VENTAS"}', 10, TRUE),

  ('allow-update-owner-or-senior',
   'Permite editar si el usuario es dueno del producto o tiene seniority senior',
   'products', 'update', 'ALLOW',
   '{"type":"OWNER_OR_SENIORITY","seniority":"senior"}', 10, TRUE),

  ('deny-delete-outside-hours',
   'Deniega eliminar productos fuera del horario 08:00-18:00',
   'products', 'delete', 'DENY',
   '{"type":"OUTSIDE_HOURS","start":8,"end":18}', 5, TRUE),

  ('allow-delete-admin-area',
   'Permite eliminar productos si el usuario es del area TI',
   'products', 'delete', 'ALLOW',
   '{"type":"USER_AREA","value":"TI"}', 10, TRUE),

  ('allow-read-ti-area',
   'Permite leer/listar productos si el usuario es del area TI',
   'products', 'read', 'ALLOW',
   '{"type":"USER_AREA","value":"TI"}', 5, TRUE),

  ('allow-create-ti-area',
   'Permite crear productos si el usuario es del area TI',
   'products', 'create', 'ALLOW',
   '{"type":"USER_AREA","value":"TI"}', 5, TRUE);
