# 🔐 Auth Service

Microservicio de autenticación y autorización basado en Spring Boot y Keycloak, diseñado para proporcionar una capa común de seguridad a las aplicaciones del ecosistema.

---

## 📋 Descripción

`auth-service` es un microservicio reutilizable que permite proteger APIs REST mediante tokens JWT emitidos por Keycloak.

Su objetivo es servir como base para centralizar progresivamente las funcionalidades de identidad, autorización y seguridad compartidas por distintas aplicaciones, evitando duplicar lógica entre proyectos.

Keycloak continúa siendo el proveedor de identidad encargado de gestionar los usuarios, autenticar a los usuarios y emitir los tokens. `auth-service` valida dichos tokens en sus endpoints protegidos y ofrece funcionalidades comunes a las aplicaciones consumidoras.

## ✨ Características principales

- Validación de tokens JWT emitidos por Keycloak.
- Protección de endpoints REST mediante autenticación.
- Control de acceso basado en roles.
- Conversión de roles de Keycloak en autoridades de Spring Security.
- Arquitectura sin sesiones HTTP en el microservicio (`STATELESS`).
- Documentación interactiva de la API mediante Swagger UI.
- Endpoints de salud y monitorización mediante Spring Boot Actuator.
- Despliegue en Docker.
- Configuración específica para el entorno Docker mediante perfiles de Spring.
- Preparado como base para incorporar funcionalidades compartidas de identidad y autorización.

## 🏗️ Arquitectura

La solución separa la gestión de identidad de la protección de las APIs.

```text
                 ┌──────────────────────┐
                 │  Aplicación cliente  │
                 │  SportEvents, etc.   │
                 └──────────┬───────────┘
                            │
                 Login      │      Petición a la API
                            │      Authorization: Bearer JWT
                            ▼
                 ┌──────────────────────┐
                 │       Keycloak       │
                 │                      │
                 │ Usuarios y login     │
                 │ Emisión de JWT       │
                 │ Roles de identidad   │
                 └──────────────────────┘

                            │
                            │ JWT
                            ▼
                 ┌──────────────────────┐
                 │     auth-service     │
                 │                      │
                 │ Validación del JWT   │
                 │ Autenticación        │
                 │ Autorización por rol │
                 │ API REST protegida   │
                 └──────────────────────┘
```

### Flujo de autenticación

1. La aplicación cliente redirige al usuario a Keycloak para iniciar sesión.
2. Keycloak autentica al usuario y emite los tokens correspondientes.
3. La aplicación cliente utiliza el token de acceso para realizar peticiones a las APIs protegidas.
4. `auth-service` valida el token, incluyendo su firma y las comprobaciones de seguridad configuradas.
5. Spring Security determina si la petición está autenticada y si dispone de los roles necesarios.
6. La API responde según los permisos del usuario.

La autenticación del usuario y la autorización de las operaciones son responsabilidades relacionadas, pero distintas.

## 🧰 Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 25 | Lenguaje de programación |
| Spring Boot 4.1.1 | Framework de aplicación |
| Spring Security | Seguridad y control de acceso |
| OAuth 2.0 / JWT | Protección de APIs mediante tokens |
| Keycloak 26.4 | Proveedor de identidad |
| Springdoc OpenAPI 3.0.1 | Documentación de la API |
| Spring Boot Actuator | Salud y monitorización |
| Maven | Gestión de dependencias y construcción |
| Docker | Contenedorización y despliegue |
| Nginx Proxy Manager | Publicación HTTPS mediante proxy inverso |
| Tailscale | Comunicación privada entre servidores |

## 🔌 API REST

La API dispone de endpoints para comprobar la identidad autenticada, verificar el control de acceso y consultar el estado del servicio.

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/auth/me` | Autenticado | Devuelve la información disponible del usuario autenticado |
| GET | `/api/auth/admin` | Rol `ADMIN` | Endpoint de comprobación de autorización administrativa |
| GET | `/actuator/health` | Público | Estado de salud del servicio |
| GET | `/actuator/info` | Público | Información de la aplicación |
| GET | `/swagger-ui/index.html` | Público | Interfaz de Swagger UI |
| GET | `/v3/api-docs` | Público | Especificación OpenAPI |

Los endpoints de ejemplo permiten comprobar la autenticación y la autorización. La API podrá ampliarse con operaciones funcionales comunes a las aplicaciones consumidoras.

### Respuestas de seguridad

- `200 OK`: petición procesada correctamente.
- `401 Unauthorized`: falta el token o no es válido.
- `403 Forbidden`: el usuario está autenticado, pero no dispone de los permisos requeridos.

## ⚙️ Configuración

La configuración se realiza mediante los ficheros de Spring y las variables de entorno del contenedor.

| Parámetro | Descripción |
|---|---|
| `SERVER_PORT` | Puerto HTTP de la aplicación. Por defecto, `8087` |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo, por ejemplo `docker` |
| `KEYCLOAK_ISSUER_URI` | URL del emisor JWT que debe utilizarse para validar los tokens |

El emisor configurado actualmente corresponde al realm `platform` de Keycloak.

```text
https://auth-service-keycloak.duckdns.org/realms/platform
```

No deben almacenarse credenciales, contraseñas ni secretos reales en el repositorio. La configuración sensible debe proporcionarse mediante mecanismos externos al código fuente.

## 🐳 Despliegue con Docker

La aplicación se distribuye como una imagen Docker y se ejecuta en el servidor junto al resto de componentes de la plataforma.

Imagen utilizada:

```text
jgf78/auth-service:latest
```

Características del despliegue:

- Contenedor independiente.
- Perfil de Spring `docker`.
- Puerto interno `8087`.
- Reinicio automático del contenedor.
- Comunicación privada con el resto de componentes mediante la red configurada.
- Publicación de la API a través de Nginx Proxy Manager con HTTPS.
- Acceso directo al puerto de la aplicación limitado a la interfaz de Tailscale del servidor.

### URL pública

```text
https://auth-service-api.duckdns.org
```

Ejemplo de comprobación de salud:

```text
GET https://auth-service-api.duckdns.org/actuator/health
```

El despliegue utiliza Docker Compose para definir el servicio y sus parámetros de ejecución.

## 🔒 Seguridad

La protección de la API se basa en Spring Security y tokens JWT emitidos por Keycloak.

- Las peticiones protegidas deben incluir el token de acceso en la cabecera `Authorization`.
- Los endpoints se protegen mediante autenticación y roles.
- La aplicación funciona sin mantener sesiones HTTP.
- La comunicación pública se realiza mediante HTTPS a través del proxy inverso.
- El puerto de la aplicación no se publica directamente en Internet.
- Los endpoints de salud e información configurados como públicos no requieren autenticación.
- Swagger UI está habilitado para facilitar la consulta y prueba de la API.

CORS deberá configurarse con los orígenes concretos de las aplicaciones web que consuman directamente esta API desde el navegador. CORS no sustituye a la autenticación ni a la autorización.

## 📈 Evolución prevista

El proyecto parte de una base funcional de validación de JWT y control de acceso. Las siguientes funcionalidades se incorporarán progresivamente según las necesidades de las aplicaciones consumidoras.

- Endpoint común de identidad del usuario autenticado.
- Gestión centralizada de permisos por aplicación.
- Reglas de autorización compartidas entre microservicios.
- Integración con aplicaciones web y APIs REST.
- Auditoría de accesos y decisiones de autorización.
- Mejoras de observabilidad, documentación y gestión de errores.

## 📌 Estado actual

**Versión inicial:** infraestructura desplegada y API protegida operativa.

La aplicación valida tokens de Keycloak, aplica control de acceso por roles y dispone de documentación OpenAPI y endpoints de monitorización.

El siguiente objetivo es definir las funcionalidades comunes que aporten valor a varias aplicaciones, manteniendo Keycloak como proveedor de identidad.

## 📄 Licencia

Proyecto de uso personal. La licencia se definirá cuando se determine el modelo de distribución.

---

Desarrollado como parte del ecosistema de microservicios reutilizables.