# Entrega — Tuckersoft Branch Engine

**Equipo:** G15

| Integrante | Código |
|:--|:--|
| Mathias Pariona | 202210245 |
| Adriano Raffo | 202210481 |
| Gonzalo Gaviño | 202210146 |

Esta entrega cubre las dos primeras estrellas: **★1 Seguridad** y **★2 Nodos**.
Las capas de partidas, decisiones y asincronía no están implementadas todavía.

---

## 1. Resumen de estrellas

Marcador de la última corrida de los autotests:

```
  ──────────────────────────────────────────────────────────────
   TUCKERSOFT · CONTROL DE CALIDAD
   motor: http://localhost:8080        corrida: MUBXA4YT
  ──────────────────────────────────────────────────────────────

   ★★☆☆☆   2 / 5

   ✔  ★1  SEGURIDAD    65 comprobaciones
   ✔  ★2  NODOS        37 comprobaciones
   ✘  ★3  PARTIDAS     0 comprobaciones antes de fallar
        └ POST /api/v1/playthroughs responde 201
   ·  ★4  DECISIONES   no evaluado
   ·  ★5  ASINCRONIA   no evaluado
  ──────────────────────────────────────────────────────────────
```

Las 102 comprobaciones de ★1 y ★2 pasan. La ★3 falla porque sus endpoints
todavía no existen, y por el encadenamiento de la batería las dos siguientes
quedan como *no evaluado*.

---

## 2. Qué está implementado

### ★1 — Seguridad

- **`POST /api/v1/auth/register`** devuelve 201 con el token. Email repetido → 409;
  email inválido, contraseña de menos de 6 caracteres o `displayName` de menos de 3 → 400.
  El `role` se fija en el service y siempre es `ROLE_USER`: el DTO de entrada ni
  siquiera tiene ese campo, así que pedir `ROLE_ADMIN` desde el cuerpo no concede nada.
- **`POST /api/v1/auth/login`** devuelve 200. Una contraseña incorrecta y un email
  inexistente producen el mismo 401, sin revelar cuál de los dos falló.
- **`GET /api/v1/users/me`** para cualquier autenticado; **`GET /api/v1/users`** y
  **`PATCH /api/v1/users/{id}/role`** solo para `ROLE_ADMIN`. El cambio de rol valida
  el valor contra la lista (400), el id inexistente (404) y que nadie se degrade a sí
  mismo (400), para no dejar la instalación sin ningún administrador.
- **`DataInitializer`** crea al administrador al arrancar leyendo `ADMIN_NAME`,
  `ADMIN_EMAIL` y `ADMIN_PASSWORD` del `.env`, con la contraseña codificada con BCrypt.
  Si ya existe un usuario con ese email, no hace nada.
- Ningún response incluye jamás el campo `password`.

### ★2 — Nodos

- **`POST /api/v1/nodes`** solo para `ROLE_ADMIN` (403 para un usuario normal, 401 sin
  token), devuelve 201 con `currentBranches` en 0, que fija el service y no llega en el
  request. `nodeCode` repetido → 409; `branchCapacity = 0`, campos faltantes o
  `sceneText` de menos de 10 caracteres → 400.
- **`GET /api/v1/nodes`** devuelve un array simple de DTOs y
  **`GET /api/v1/nodes/{id}`** el nodo, o 404 si no existe.
- `primaryBranchCode` y `glitchBranchCode` se guardan como Strings, no como llaves
  foráneas, para que los nodos se puedan crear en cualquier orden y apuntar a escenas
  que todavía no existen.

---

## 3. Decisiones de diseño

- **El rol se lee de la base de datos, no del token.** El JWT lleva únicamente el email
  en el `subject`; `AppUserDetailsService` construye las autoridades consultando
  PostgreSQL en cada petición. Gracias a eso, cuando un administrador promueve a
  alguien, el token que esa persona ya tenía en la mano sirve inmediatamente con los
  permisos nuevos, sin volver a iniciar sesión.
- **Nunca se serializan entidades JPA.** Todas las respuestas son `record` DTO. Devolver
  la entidad expondría el campo `password` y, en cuanto existan relaciones
  bidireccionales, metería a Jackson en un bucle infinito.
- **El formato de error está centralizado** en `GlobalExceptionHandler`. Los 401 y 403
  que Spring Security responde antes de llegar al controller los escriben
  `RestAuthenticationEntryPoint` y `RestAccessDeniedHandler`, registrados a mano en el
  `SecurityFilterChain`, porque por defecto salen con el cuerpo vacío.
- **`SecurityFilterChain`** con `csrf` deshabilitado y `SessionCreationPolicy.STATELESS`.
  El filtro JWT solo acepta cabeceras que empiecen exactamente por `Bearer `, y captura
  las excepciones de jjwt para responder 401 en vez de reventar con un 500.

---

## 4. Lo que queda pendiente

- **★3 Partidas:** entidad `Playthrough`, apertura de partidas con los valores
  iniciales, control de capacidad del nodo, aislamiento entre usuarios y el recorrido.
- **★4 Decisiones:** entidad `Decision`, las cinco reglas de clasificación, las tablas
  de departamento y consecuencia, los stats por nivel de impacto, la resolución del
  nodo destino y los tres finales.
- **★5 Asincronía:** entidad `RealityLog`, el evento tras el commit, el listener
  asíncrono y el envío del Informe de Realidad por correo.
