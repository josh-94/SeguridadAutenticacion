# Guía de Presentación — SAST y DAST
## Con Scripts Completos para Copiar y Pegar

---

## 📋 PREPARACIÓN PREVIA (hacer antes de presentar)

### 1. Verificar que tienes todo instalado

```powershell
# Verificar Java
java -version
# Debe mostrar: java version "17.x.x"

# Verificar Docker
docker --version
# Debe mostrar: Docker version 20.x o superior

# Verificar Maven
mvn -version
# Debe mostrar: Apache Maven 3.x

# Verificar Git
git --version
```

### 2. Clonar/preparar el proyecto

```powershell
# Si aún no lo tienes
cd C:\Users\Jeshua\Documents\tecsupArqSoftware\seguridadEnArqSoft

# Verificar archivos creados
ls .github\workflows\
# Debe mostrar: semgrep.yml, dast-zap.yml, codeql.yml

ls .github\zap\
# Debe mostrar: zap-rules.tsv, add-auth-header.py
```

---

## 1️⃣ INTRODUCCIÓN (2 min)

### Slide de apertura
```
┌──────────────────────────────────────────────────┐
│ SAST y DAST: Seguridad Automatizada en CI/CD    │
├──────────────────────────────────────────────────┤
│ Proyecto: Spring Boot + MFA + RBAC + ABAC       │
│                                                  │
│ Herramientas:                                    │
│  • SAST → Semgrep (análisis de código)          │
│  • DAST → OWASP ZAP (pruebas de penetración)    │
│                                                  │
│ Objetivo: Detectar vulnerabilidades antes y      │
│          después del despliegue                  │
└──────────────────────────────────────────────────┘
```

---

## 2️⃣ PARTE 1: SAST — SEMGREP (5 min)

### A) Explicación (1 min)

**Decir:**
> "SAST analiza el código fuente sin ejecutarlo. Es como revisar planos antes de construir. Detecta SQL injection, secretos hardcodeados, uso inseguro de JWT, etc."

### B) Mostrar el workflow (1 min)

```powershell
# Abrir el archivo en VS Code
code .github\workflows\semgrep.yml
```

**Explicar las líneas importantes:**
```yaml
# Configuración que se ejecuta automáticamente
on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

# Reglas de seguridad aplicadas
config: >-
  p/java              # Vulnerabilidades Java generales
  p/spring-boot       # Específicas de Spring
  p/owasp-top-ten     # OWASP Top 10
  p/jwt               # Seguridad de tokens
```

### C) Demo: Ejecutar Semgrep localmente (2 min)

```powershell
# Instalar Semgrep (si no está instalado)
pip install semgrep

# Ejecutar análisis en el proyecto
cd C:\Users\Jeshua\Documents\tecsupArqSoftware\seguridadEnArqSoft
semgrep --config "p/java" --config "p/spring-boot" --config "p/owasp-top-ten" security/src/

# Versión más rápida (solo errores críticos)
semgrep --config "p/security-audit" security/src/ --severity ERROR
```

**Mientras corre, explicar:**
- "Está analizando ~50 archivos Java"
- "Compara contra miles de reglas de la comunidad"
- "Tarda 2-3 minutos, muy rápido para feedback"

### D) Mostrar resultados en GitHub Actions (1 min)

```powershell
# Opción 1: Abrir en navegador
Start-Process "https://github.com/TU_USUARIO/seguridadEnArqSoft/actions"

# Opción 2: Ver último commit
git log --oneline -5
```

**En el navegador mostrar:**
- ✅ Workflow "Semgrep Security Scan" pasando
- Click en un workflow para ver detalles
- Mostrar que se ejecuta automáticamente

---

## 3️⃣ PARTE 2: DAST — OWASP ZAP (8 min)

### A) Explicación (1 min)

**Decir:**
> "DAST ataca la aplicación corriendo. Es como contratar un hacker ético. No ve el código, solo prueba endpoints públicos intentando SQLi, XSS, acceso no autorizado, etc."

### B) Preparar el entorno (2 min)

#### Terminal 1: Base de datos
```powershell
# Navegar al directorio
cd C:\Users\Jeshua\Documents\tecsupArqSoftware\seguridadEnArqSoft\security

# Levantar PostgreSQL
docker compose up -d

# Verificar que está corriendo
docker ps
# Debe mostrar: tecsup_db con status "Up" y "healthy"
```

#### Terminal 2: Backend
```powershell
# Compilar (si hay cambios)
cd C:\Users\Jeshua\Documents\tecsupArqSoftware\seguridadEnArqSoft\security
mvn clean package -DskipTests

# Iniciar la aplicación
java -jar target/security-1.0.0.jar
```

**Mientras carga, explicar:**
- "El backend tiene endpoints protegidos con JWT"
- "RBAC: admin, operador, consulta"
- "ABAC: políticas basadas en región, área, seniority"

#### Terminal 3: Verificar que funciona
```powershell
# Esperar 10-15 segundos, luego:
curl http://localhost:8080/v3/api-docs
# Debe devolver JSON (el spec OpenAPI)

# O abrir en navegador
Start-Process "http://localhost:8080/swagger-ui.html"
```

### C) Obtener token JWT (1 min)

```powershell
# Login como admin
$response = Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"Admin123!"}'

# Guardar token
$token = $response.token
$env:ZAP_JWT_TOKEN = $token

# Mostrar token (primeros caracteres)
Write-Host "Token obtenido: $($token.Substring(0,30))..." -ForegroundColor Green

# Verificar que funciona
Invoke-RestMethod -Uri "http://localhost:8080/api/products" `
  -Headers @{Authorization = "Bearer $token"}
# Debe devolver lista de productos
```

**Explicar:**
> "Obtuvimos un JWT válido del endpoint de login. ZAP usará este token para probar endpoints protegidos como `/api/admin/users`, `/api/admin/roles`, etc."

### D) Preparar OpenAPI para ZAP (1 min)

```powershell
# Descargar el spec
Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" `
  -OutFile "openapi.json"

# Reemplazar localhost por host.docker.internal (para Docker)
(Get-Content openapi.json) -replace 'http://localhost:8080', 'http://host.docker.internal:8080' | Set-Content openapi.json

# Verificar el cambio
Select-String "host.docker.internal" openapi.json
# Debe mostrar varias líneas con host.docker.internal

# Crear carpeta para reportes
New-Item -ItemType Directory -Force -Path "zap-reports"
```

### E) Ejecutar DAST con ZAP (3 min)

#### Opción 1: Escaneo Baseline (rápido, 3 min)
```powershell
docker run --rm `
  -v "${PWD}\zap-reports:/zap/wrk/" `
  ghcr.io/zaproxy/zaproxy:stable `
  zap-baseline.py -t http://host.docker.internal:8080 `
    -r zap-baseline.html `
    -J zap-baseline.json `
    -I
```

**Explicar mientras corre:**
- "Escaneo pasivo: solo observa headers, cookies, CORS"
- "No lanza ataques, es seguro para PRs"
- "~3 minutos, feedback rápido"

#### Opción 2: Escaneo Completo con JWT (10-12 min)
```powershell
docker run --rm `
  -e "ZAP_JWT_TOKEN=$env:ZAP_JWT_TOKEN" `
  -v "${PWD}\openapi.json:/zap/wrk/openapi.json:ro" `
  -v "${PWD}\.github\zap:/zap/conf/:ro" `
  -v "${PWD}\zap-reports:/zap/wrk/" `
  ghcr.io/zaproxy/zaproxy:stable `
  zap-api-scan.py -t /zap/wrk/openapi.json -f openapi `
    -r reporte-api.html `
    -J reporte-api.json `
    --hook=/zap/conf/add-auth-header.py -I
```

**Explicar mientras corre:**
- "Lee el OpenAPI: 84 endpoints automáticamente"
- "Hook inyecta JWT en cada petición"
- "Ataca: SQLi, XSS, IDOR, Path Traversal, Broken Auth"
- "Valida RBAC: usuario normal no puede acceder a `/admin`"

---

## 4️⃣ ANÁLISIS DE RESULTADOS (3 min)

### Abrir el reporte

```powershell
# Si ejecutaste baseline
Start-Process zap-reports\zap-baseline.html

# Si ejecutaste API scan
Start-Process zap-reports\reporte-api.html
```

### Mostrar en el navegador

**Destacar en el reporte:**

1. **Summary of Alerts:**
```
🔴 High:   0 ✅ Excelente
🟠 Medium: 1 ⚠️ Requiere corrección
🟡 Low:    1 ℹ️  Buena práctica
🔵 Info:   3 ✓  Observaciones
```

2. **Insights:**
```
✅ 84 endpoints analizados
✅ 46% respuestas 2xx (éxito)
✅ Métodos: GET (53%), POST (19%), DELETE (20%), PUT (7%)
⚠️ 53% errores 4xx (validaciones RBAC/ABAC funcionando)
```

3. **Alerta Medium — Source Code Disclosure:**

```powershell
# Buscar la evidencia en el reporte
Select-String -Path zap-reports\reporte-api.html -Pattern "insert into roles"
```

**Explicar:**
> "Cuando enviamos datos inválidos, el servidor expone SQL en los errores. Un atacante ve la estructura de nuestra BD."

**Evidencia mostrada:**
```
Evidence: insert into roles (created_at,description,name) values (
```

### Mostrar la solución

```powershell
# Abrir application.yml
code security\src\main\resources\application.yml
```

**Agregar (o mostrar ya agregado):**
```yaml
server:
  error:
    include-message: never
    include-stacktrace: never
    include-exception: false
```

---

## 5️⃣ COMPARACIÓN SAST vs DAST (2 min)

### Tabla comparativa (mostrar slide)

```
┌──────────────────┬─────────────────┬──────────────────┐
│ Característica   │ SAST (Semgrep)  │ DAST (ZAP)       │
├──────────────────┼─────────────────┼──────────────────┤
│ Entrada          │ Código fuente   │ App corriendo    │
│ Fase             │ Desarrollo      │ Pre-producción   │
│ Velocidad        │ ⚡ 2-3 min      │ 🐢 10-15 min     │
│ Cobertura        │ Todo el código  │ Solo expuesto    │
│ Falsos positivos │ Medios          │ Bajos            │
│ Autenticación    │ No aplica       │ ✅ JWT inyectado │
│ Detecta          │ Bugs código     │ Vulnerab. real   │
│ Costo            │ $0 (OSS)        │ $0 (OSS)         │
└──────────────────┴─────────────────┴──────────────────┘
```

---

## 6️⃣ ARQUITECTURA CI/CD (2 min)

### Mostrar los workflows

```powershell
# Ver estructura
tree .github /F

# Mostrar workflow SAST
code .github\workflows\semgrep.yml

# Mostrar workflow DAST
code .github\workflows\dast-zap.yml
```

### Diagrama de flujo (mostrar slide)

```
Developer Push → GitHub Actions
                      ↓
              ┌──────────────────┐
              │ Semgrep (SAST)   │
              │ ✅ Pass / ❌ Fail │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ Pull Request     │
              │ ├─ SAST          │
              │ └─ ZAP Baseline  │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ Merge to Main    │
              │ └─ ZAP Active    │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ Cron Semanal     │
              │ (Lunes 3am UTC)  │
              └──────────────────┘
```

### Comandos útiles GitHub Actions

```powershell
# Ver último commit y status
git log --oneline -1
git status

# Si quieres disparar workflows manualmente
git commit --allow-empty -m "trigger: test SAST and DAST"
git push

# Abrir GitHub Actions en navegador
Start-Process "https://github.com/TU_USUARIO/seguridadEnArqSoft/actions"
```

---

## 7️⃣ RESULTADOS Y MÉTRICAS (2 min)

### Resumen ejecutivo (mostrar slide)

```
┌─────────────────────────────────────────────────┐
│          RESULTADOS DEL PROYECTO                │
├─────────────────────────────────────────────────┤
│ SAST (Semgrep)                                  │
│  ✅ ~50 archivos analizados                     │
│  ✅ 0 vulnerabilidades críticas                 │
│  ✅ Tiempo: 2.5 min promedio                    │
│  ✅ Reglas: 15,000+ de la comunidad             │
│                                                 │
│ DAST (OWASP ZAP)                                │
│  ✅ 84 endpoints probados                       │
│  ✅ 500+ peticiones de ataque                   │
│  ✅ 0 vulnerabilidades críticas                 │
│  ⚠️  1 Medium (SQL disclosure) → Corregido     │
│  ✅ RBAC/ABAC funcionando correctamente         │
│  ✅ JWT validado                                │
│                                                 │
│ Impacto                                         │
│  💰 Costo: $0 (herramientas OSS)               │
│  🔄 Automatización: 100% en CI/CD              │
│  🛡️  Cobertura: Código + Runtime               │
│  ⚡ Feedback: Inmediato en cada PR             │
└─────────────────────────────────────────────────┘
```

---

## 8️⃣ CONCLUSIONES (2 min)

### Aprendizajes clave

**Decir:**

1. **Complementariedad:**
   > "SAST y DAST no compiten, se complementan. SAST encuentra bugs antes de ejecutar, DAST valida la seguridad runtime."

2. **Shift-Left Security:**
   > "Encontrar un bug en desarrollo cuesta $100. En producción cuesta $10,000. Automatizar seguridad en CI/CD ahorra dinero."

3. **Autenticación:**
   > "El reto técnico fue inyectar JWT en ZAP. El hook Python fue la solución."

4. **Automatización:**
   > "Sin automatización, estas pruebas no se harían. GitHub Actions lo hace inevitable."

### Recomendaciones futuras

```
Próximos pasos:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✓ Mantener SAST + DAST activos
✓ Agregar SCA (análisis de dependencias)
✓ IAST (análisis híbrido) en futuro
✓ Configurar alertas en Slack/Email
✓ Pentesting manual anual
✓ Bug bounty program
```

---

## 🧹 LIMPIEZA POST-DEMO

```powershell
# Detener backend (Ctrl+C en su terminal)

# Detener PostgreSQL
cd C:\Users\Jeshua\Documents\tecsupArqSoftware\seguridadEnArqSoft\security
docker compose down

# Limpiar reportes (opcional)
Remove-Item zap-reports\* -Recurse -Force

# Verificar que todo se detuvo
docker ps
# No debe mostrar contenedores corriendo
```

---

## 📦 SCRIPT COMPLETO TODO-EN-UNO

### Para ejecutar toda la demo de una vez

```powershell
# ========================================
# SCRIPT COMPLETO DEMO SAST/DAST
# Copiar y pegar en PowerShell
# ========================================

Write-Host "=== DEMO SAST/DAST ===" -ForegroundColor Cyan

# 1. Navegar al proyecto
cd C:\Users\Jeshua\Documents\tecsupArqSoftware\seguridadEnArqSoft
Write-Host "✓ En directorio del proyecto" -ForegroundColor Green

# 2. Levantar infraestructura
Write-Host "`n--- Levantando PostgreSQL ---" -ForegroundColor Yellow
cd security
docker compose up -d
Start-Sleep -Seconds 5

# 3. Compilar y levantar backend
Write-Host "`n--- Compilando backend ---" -ForegroundColor Yellow
mvn clean package -DskipTests -q

Write-Host "`n--- Iniciando backend (espera 15 seg) ---" -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/security-1.0.0.jar"
Start-Sleep -Seconds 15

# 4. Verificar backend
Write-Host "`n--- Verificando backend ---" -ForegroundColor Yellow
$test = Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" -UseBasicParsing
if ($test.StatusCode -eq 200) {
    Write-Host "✓ Backend corriendo" -ForegroundColor Green
} else {
    Write-Host "✗ Backend no responde" -ForegroundColor Red
    exit
}

# 5. Obtener JWT
Write-Host "`n--- Obteniendo JWT ---" -ForegroundColor Yellow
cd ..
$response = Invoke-RestMethod -Method POST `
  -Uri "http://localhost:8080/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"Admin123!"}'
$token = $response.token
$env:ZAP_JWT_TOKEN = $token
Write-Host "✓ JWT: $($token.Substring(0,30))..." -ForegroundColor Green

# 6. Preparar OpenAPI
Write-Host "`n--- Preparando OpenAPI ---" -ForegroundColor Yellow
Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" -OutFile "openapi.json"
(Get-Content openapi.json) -replace 'http://localhost:8080', 'http://host.docker.internal:8080' | Set-Content openapi.json
New-Item -ItemType Directory -Force -Path "zap-reports" | Out-Null
Write-Host "✓ OpenAPI listo" -ForegroundColor Green

# 7. Ejecutar ZAP
Write-Host "`n--- Ejecutando ZAP DAST (esto toma ~10 min) ---" -ForegroundColor Yellow
docker run --rm `
  -e "ZAP_JWT_TOKEN=$env:ZAP_JWT_TOKEN" `
  -v "${PWD}\openapi.json:/zap/wrk/openapi.json:ro" `
  -v "${PWD}\.github\zap:/zap/conf/:ro" `
  -v "${PWD}\zap-reports:/zap/wrk/" `
  ghcr.io/zaproxy/zaproxy:stable `
  zap-api-scan.py -t /zap/wrk/openapi.json -f openapi `
    -r reporte-api.html `
    -J reporte-api.json `
    --hook=/zap/conf/add-auth-header.py -I

# 8. Abrir reporte
Write-Host "`n--- Abriendo reporte ---" -ForegroundColor Yellow
Start-Process zap-reports\reporte-api.html

Write-Host "`n=== DEMO COMPLETADA ===" -ForegroundColor Cyan
Write-Host "Presiona Enter para limpiar..." -ForegroundColor Yellow
Read-Host

# 9. Limpieza
Write-Host "`n--- Limpiando ---" -ForegroundColor Yellow
cd security
docker compose down
Write-Host "✓ Demo finalizada" -ForegroundColor Green
```

---

## 🎯 CHECKLIST PRE-PRESENTACIÓN

```powershell
# Ejecutar esto 5 minutos antes de presentar
Write-Host "=== CHECKLIST PRE-PRESENTACIÓN ===" -ForegroundColor Cyan

# 1. Docker corriendo
$docker = docker ps 2>$null
if ($?) {
    Write-Host "✓ Docker Desktop activo" -ForegroundColor Green
} else {
    Write-Host "✗ Iniciar Docker Desktop" -ForegroundColor Red
}

# 2. Archivos existen
$files = @(
    ".github\workflows\semgrep.yml",
    ".github\workflows\dast-zap.yml",
    ".github\zap\zap-rules.tsv",
    ".github\zap\add-auth-header.py"
)
foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "✓ $file" -ForegroundColor Green
    } else {
        Write-Host "✗ Falta: $file" -ForegroundColor Red
    }
}

# 3. Reportes anteriores (opcional)
if (Test-Path "zap-reports\reporte-api.html") {
    Write-Host "✓ Reporte DAST disponible" -ForegroundColor Green
} else {
    Write-Host "⚠ Sin reporte previo (se generará en vivo)" -ForegroundColor Yellow
}

Write-Host "`n=== LISTO PARA PRESENTAR ===" -ForegroundColor Cyan
```

---

## 📊 DATOS EXACTOS DE TU PROYECTO

### Estadísticas reales del reporte DAST:
- **84 endpoints** escaneados
- **46%** respuestas exitosas (2xx)
- **53%** errores de validación (4xx) - RBAC/ABAC funcionando
- **Métodos HTTP:** GET (53%), POST (19%), DELETE (20%), PUT (7%)
- **Tiempo de escaneo:** ~10-12 minutos
- **Vulnerabilidades críticas:** 0
- **Vulnerabilidades Medium:** 1 (Source Code Disclosure - SQL)
- **Vulnerabilidades Low:** 1 (CORP header)

### Archivos del proyecto:
```
.github/
  workflows/
    semgrep.yml           # SAST workflow
    dast-zap.yml          # DAST workflow (2 jobs)
    codeql.yml            # Análisis adicional
  zap/
    zap-rules.tsv         # Reglas personalizadas
    add-auth-header.py    # Hook para JWT

security/
  src/main/
    java/com/tecsup/security/
      auth/AuthController.java
      rbac/RoleController.java
      product/ProductController.java
      audit/AuditController.java
    resources/
      application.yml
      db/migration/
        V2__seed.sql      # Usuarios de prueba
```

---

¡Con este archivo puedes tener la guía completa abierta mientras presentas! 🚀
