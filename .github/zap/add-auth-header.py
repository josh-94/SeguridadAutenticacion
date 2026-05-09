"""
ZAP Hook Script – Inyeccion de JWT Bearer Token
================================================
Este script es cargado por ZAP via --hook=/ruta/add-auth-header.py

ZAP llama a zap_started() automaticamente cuando termina de inicializar.
El hook usa la API de ZAP para agregar una regla de "Replacer":
cada peticion HTTP que ZAP haga incluira el header Authorization con el JWT.

Esto permite que el Active Scan explore endpoints protegidos (/api/products,
/api/users, /api/roles, /api/audit) que de otro modo devolverian 401.

Variable de entorno requerida:
  ZAP_JWT_TOKEN – el JWT obtenido del endpoint /api/auth/login
"""

import os


def zap_started(zap, target):
    """
    Hook llamado por ZAP una vez que esta listo para escanear.

    Args:
        zap:    instancia del cliente ZAP Python API
        target: URL objetivo del escaneo
    """
    token = os.environ.get("ZAP_JWT_TOKEN", "").strip()

    if not token:
        print(
            "[ZAP HOOK] ADVERTENCIA: ZAP_JWT_TOKEN no definido. "
            "ZAP escaneara solo endpoints publicos (401 en rutas protegidas)."
        )
        return

    print(f"[ZAP HOOK] Configurando header Authorization para: {target}")
    print(f"[ZAP HOOK] Token (primeros 20 chars): {token[:20]}...")

    try:
        # Agrega una regla al addon "Replacer" de ZAP.
        # Cada peticion que ZAP envie incluira: Authorization: Bearer <token>
        zap.replacer.add_rule(
            description="JWT Bearer Token – DAST Auth",
            enabled=True,
            matchtype="REQ_HEADER",       # reemplaza/agrega un header HTTP
            matchstring="Authorization",  # nombre del header
            matchregex=False,
            replacement=f"Bearer {token}",
        )
        print("[ZAP HOOK] Header Authorization inyectado correctamente.")
        print("[ZAP HOOK] ZAP podra explorar endpoints con RBAC/ABAC activo.")

    except Exception as exc:
        print(f"[ZAP HOOK] ERROR al configurar el replacer: {exc}")
        print("[ZAP HOOK] El escaneo continuara sin autenticacion.")
