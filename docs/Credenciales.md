# 🔐 Credenciales para Desarrollo y Demo

## Usuarios predeterminados

El sistema crea dos usuarios por defecto en **modo de desarrollo**:

### 👤 Usuario regular
- **Nombre de usuario:** `user`
- **Contraseña:** `pass`
- **Correo:** `user@example.com`
- **Rol:** `USER`
- **Acceso:** Panel, Búsqueda de invocadores, Perfil

### 🛡️ Administrador
- **Nombre de usuario:** `admin`
- **Contraseña:** `admin`
- **Correo:** `admin@example.com`
- **Rol:** `ADMIN`
- **Acceso:** Panel de administración (Gestión de usuarios)

## ⚠️ Restricciones importantes de acceso

### Limitaciones del administrador
- **Los administradores NO PUEDEN acceder a funciones de usuario** (Panel, Búsqueda de invocadores, etc.)
- Los administradores están restringidos únicamente al Panel de administración
- **Si un administrador quiere usar funciones de usuario, debe iniciar sesión con una cuenta de usuario regular**

### Justificación
Esta separación garantiza:
- Límites claros entre roles
- Mejor seguridad (los administradores no pueden realizar acciones de usuario por error)
- Fomenta la gestión adecuada de cuentas
- Sigue el principio de menor privilegio

### Ejemplo de uso
Si un administrador quiere:
1. Gestionar usuarios → Iniciar sesión como `admin`
2. Buscar invocadores o usar el panel → Iniciar sesión como `user` (o crear una cuenta de usuario separada)
