# Tecsup Security - Backend (Spring Boot)

Breve guía para ejecutar, probar y depurar el backend de la demo (MFA + RBAC + ABAC).

## Requisitos
- Java 17
- Maven (o un IDE que ejecute proyectos Maven)
- Docker (solo para la base de datos PostgreSQL)

## Levantar la base de datos (Docker Compose)
Desde la carpeta `security`:

```powershell
docker-compose down -v
docker-compose up -d
```

La base de datos usada por el proyecto se crea y popula mediante Flyway (migraciones en `src/main/resources/db/migration`).

## Construir y ejecutar la aplicación
Usa Maven:

```powershell
cd security
mvn -U clean package
mvn spring-boot:run
```

O ejecútalo desde tu IDE (Run/Debug) importando como proyecto Maven.

El backend expone la API en `http://localhost:8080/api` por defecto.

## Endpoints principales
- `POST /api/auth/login` - login (username/password)
- `POST /api/auth/mfa/verify` - verificación TOTP
- `POST /api/auth/mfa/setup` - generar secreto + otpAuthUrl
- `/api/admin/*` - endpoints administrativos (users, roles, audit)
- `/api/products` - CRUD productos (sujeto a ABAC)

Consulta el código en `src/main/java/com/tecsup/security` para rutas y controladores.

## Credenciales por defecto (semilla)
- admin / Admin123! (área=TI, región=LIMA)
- operador / Admin123! (área=VENTAS, región=LIMA)
- consulta / Admin123! (área=SOPORTE, región=NORTE)

## ABAC (políticas)
- Las políticas ABAC están en la tabla `abac_policies` y se crean en las migraciones SQL (`src/main/resources/db/migration`).
- Cada política tiene `resource`, `action`, `effect` (ALLOW/DENY), `condition_expression` (JSON) y `priority`.
- Comportamiento del engine: la primera política coincidente (según prioridad) determina la decisión; si no hay coincidencia → DENY (deny-by-default).

Si quieres añadir políticas nuevas, crea una migración SQL (`V3__your_policy.sql`) siguiendo el formato de `V2__seed.sql`.

## MFA
- TOTP compatible con Google Authenticator. Flujos: login -> si `mfaRequired` el cliente solicita TOTP -> `POST /api/auth/mfa/verify`.

## Problemas comunes y soluciones
- Error "Type definition error: ByteBuddyInterceptor": registrar `jackson-datatype-hibernate6` y/o añadir `@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})` en entidades. (Se agregó `JacksonConfig` en el código.)
- Problemas con `@Builder` de Lombok (valores por defecto nulos): usar `@Builder.Default` en campos con valor por defecto (ej. `createdAt`, `roles`).
- ABAC: si ves `Sin política aplicable: acceso denegado por defecto` al listar, revisa que exista una política que cubra el caso de listado global (o que `findAll` pase `resourceRegion` adecuado).

## Auditoría
- Las decisiones ABAC y acciones importantes se registran por el servicio de auditoría. Revisa la tabla de auditoría o el endpoint `/api/admin/audit` para ver `ALLOW`/`DENY` y `reason`.

## Tests
Ejecuta las pruebas unitarias/integración con:

```powershell
cd security
mvn test
```

## Comandos útiles
- Recompilar sin cache: `mvn -U clean package`
- Limpiar y recrear DB: `docker-compose down -v && docker-compose up -d`

## Contribuir / Notas
- Las migraciones Flyway contienen la semilla inicial (`V2__seed.sql`) con usuarios, roles, productos y políticas ABAC de ejemplo.
- Para agregar ejemplos ABAC o ajustar políticas, añade nuevas migraciones (no edites las existentes en producción).

Si quieres, puedo añadir un `V3__abac_examples.sql` con políticas ejemplo (SameRegion, OwnerCheck, HourRange, IP restriction, MFA threshold). ¿Lo genero ahora?
