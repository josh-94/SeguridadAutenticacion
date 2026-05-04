-- V1__schema.sql
-- Esquema base: usuarios, roles, permisos, productos, politicas ABAC y auditoria

-- =============================================
-- USUARIOS
-- =============================================
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    area        VARCHAR(100),           -- atributo ABAC
    region      VARCHAR(100),           -- atributo ABAC
    seniority   VARCHAR(50),            -- atributo ABAC: junior | senior
    department  VARCHAR(100),           -- atributo ABAC
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret  VARCHAR(255),
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- ROLES
-- =============================================
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- PERMISOS
-- =============================================
CREATE TABLE permissions (
    id          BIGSERIAL PRIMARY KEY,
    resource    VARCHAR(100) NOT NULL,  -- ej: users, roles, products
    action      VARCHAR(50)  NOT NULL,  -- ej: create, read, update, delete, assign
    description VARCHAR(255),
    UNIQUE (resource, action)
);

-- =============================================
-- RELACION ROL - PERMISO
-- =============================================
CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- =============================================
-- RELACION USUARIO - ROL
-- =============================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =============================================
-- PRODUCTOS
-- =============================================
CREATE TABLE products (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    category      VARCHAR(100),
    region        VARCHAR(100),          -- atributo ABAC
    owner_user_id BIGINT REFERENCES users(id),
    status        VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | INACTIVE
    price         NUMERIC(10,2),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- POLITICAS ABAC
-- =============================================
CREATE TABLE abac_policies (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(150) NOT NULL UNIQUE,
    description          VARCHAR(500),
    resource             VARCHAR(100) NOT NULL,  -- products
    action               VARCHAR(50)  NOT NULL,  -- select | insert | update | delete
    effect               VARCHAR(10)  NOT NULL CHECK (effect IN ('ALLOW','DENY')),
    -- condiciones como JSON simple: {"userArea":"ventas"} o {"sameRegion":true}
    condition_expression TEXT         NOT NULL,
    priority             INT          NOT NULL DEFAULT 10,
    active               BOOLEAN      NOT NULL DEFAULT TRUE
);

-- =============================================
-- AUDITORIA
-- =============================================
CREATE TABLE audit_logs (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT,
    username   VARCHAR(100),
    action     VARCHAR(100) NOT NULL,   -- LOGIN, MFA_VERIFY, products.update, etc.
    resource   VARCHAR(100),
    resource_id VARCHAR(100),
    decision   VARCHAR(10) NOT NULL,    -- ALLOW | DENY
    reason     VARCHAR(500),
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
