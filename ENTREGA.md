# 🎮 Tuckersoft Branch Engine - Informe de Entrega

**Equipo:** G15
**Integrantes:**
- Mathias Pariona (202210245)
- Adriano Raffo (202210481)
- Gonzalo Gaviño (202210146)

---

## 1. Resumen de Estrellas Alcanzadas

| Estrella | Nombre | Estado |
|:---|:---|:---|
| ★1 | SEGURIDAD | ✔ Completado |
| ★2 | NODOS | ✔ Completado |
| ★3 | PARTIDAS | ✔ Completado |
| ★4 | DECISIONES | ✔ Completado |
| ★5 | ASINCRONIA | ✔ Completado |

**Resultado total:** 5 / 5 ★★★★★ (20 Puntos)

---

## 2. Explicación del Flujo Asíncrono

El motor de notificaciones asíncronas fue diseñado para garantizar el desacoplamiento transaccional y la respuesta inmediata al cliente REST:

1. **Retorno Inmediato (HTTP 201):**
   Al procesar `POST /api/v1/decisions`, el `DecisionService` registra la decisión en estado `REGISTRADA` y dispara `DecisionCommittedEvent` mediante `ApplicationEventPublisher`. La transacción principal confirma los cambios y devuelve la respuesta HTTP 201 inmediatamente (< 1500 ms).

2. **Ejecución Post-Commit (`@TransactionalEventListener`):**
   El componente `BranchNotificationListener` escucha el evento configurado con `phase = TransactionPhase.AFTER_COMMIT`. Esto asegura que el listener en segundo plano solo intente enviar el correo una vez que la decisión esté confirmada en PostgreSQL.

3. **Pool de Hilos Dedicado (`@Async`):**
   El listener se ejecuta en un pool de hilos independiente configurado en `AsyncConfig` (`branchExecutor`, prefijo `branch-worker-`, `corePoolSize = 2`, `maxPoolSize = 4`).

4. **Transacción Independiente (`Propagation.REQUIRES_NEW`):**
   El método del listener utiliza `@Transactional(propagation = Propagation.REQUIRES_NEW)` para crear su propio contexto de transacción aislada. En esta transacción, la decisión pasa a estado `PROCESANDO`.

5. **Envío SMTP e Auditoría en `RealityLog`:**
   - Si el envío mediante `JavaMailSender` es exitoso, la decisión se actualiza a `ESTABILIZADA` y se inserta un registro en `RealityLog` con `logStatus = SENT` y la marca de tiempo `sentAt`.
   - Si ocurre una falla SMTP o se activa la simulación `X-Bandersnatch-Simulate: MAIL_FAILURE`, la excepción es capturada, la decisión pasa a estado `ERROR` y se registra un `RealityLog` con `logStatus = FAILED` y el mensaje detallado de la excepción.
   - En ambos casos se imprime en la consola el log con formato `[BRANCH-LOG]` registrando el nombre del hilo trabajador (`branch-worker-X`).

---

## 3. Pruebas Unitarias

Se implementaron los 5 tests unitarios requeridos en `DecisionServiceTest.java`:
1. Clasificación por precedencia de la regla 2 (`RUPTURA_CUARTA_PARED`) ante palabras clave combinadas (`"Stefan destruye la camara"`).
2. Clasificación de `ENTRADA_CORRUPTA` ante textos sin letras y no modificación de la partida.
3. Cálculo exacto y restricción de límites [0, 100] ante impactos `CRITICO`.
4. Prioridad de finalización `ENDING_PAC_SYMBOL` cuando `controlLevel` llega a 100.
5. Verificación de publicación de eventos con `publishEvent()` únicamente en decisiones válidas.
