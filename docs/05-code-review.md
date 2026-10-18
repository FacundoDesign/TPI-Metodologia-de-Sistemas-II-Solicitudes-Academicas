# Code review profesional

> El code review **no es una auditoría ni una crítica personal**: es una práctica colaborativa para mejorar el software (Unidad 3).
> Alcance revisado: el repositorio en el tag `etapa-pruebas` (commit `90d3bf5`), antes de aplicar las correcciones.
> Revisor/a: **[COMPLETAR: integrante revisor/a]** · Autor/a del cambio: **[COMPLETAR: integrante autor/a]** · Fecha: **[COMPLETAR]**
> (Idealmente, cada refactoring se integra mediante un *Pull Request* revisado por otra persona del equipo; los comentarios quedan en GitHub como evidencia.)

## 1. Checklist técnico

Marcado: ✔ cumple · ✖ no cumple (hallazgo) · ◐ cumple con observación.

### Lógica y funcionalidad
- [✔] ¿El código hace lo que se supone? → 6 escenarios de referencia idénticos a la versión inicial.
- [✔] ¿Cubre casos de borde? (legajo corto/largo/con letras, nombre vacío, email sin arroba/punto, tipo desconocido, id inexistente, transiciones inválidas, archivo dañado).
- [◐] ¿Hay condiciones lógicas que podrían fallar? → R-4 (consistencia memoria/disco) y R-2 (criterio nulo en filtro).
- [✔] ¿Los casos de error están manejados en un solo lugar? → un `catch` en `ConsolaMenu.ejecutar`.

### Legibilidad
- [✔] Nombres descriptivos y del dominio; sin variables de una letra.
- [✔] Un desarrollador nuevo puede seguir el flujo: `Main` → `ConsolaMenu` → `SolicitudService` → `Solicitud`.
- [✔] Comentarios explican *por qué* (decisiones y reglas), no repiten el código.
- [✔] Sin código muerto ni comentado.

### Diseño y arquitectura
- [✔] SRP: cada clase tiene una responsabilidad; [✔] DIP: el servicio depende de `SolicitudRepository`.
- [✖] Long Parameter List: `Solicitud.reconstruir` recibía 8 parámetros → R-1.
- [✖] DRY/OCP: las opciones válidas de los *prompts* estaban escritas a mano, duplicando los enums → R-3.
- [◐] `desde()` devuelve `null` cuando no encuentra (Clean Code recomienda no devolver `null`) → R-5.
- [◐] `ConsolaMenu` consulta `obtener(id)` solo para preservar el orden de los errores → R-6.

### Seguridad
- [✔] Sin datos personales reales (datos sintéticos); sin credenciales en el repositorio.
- [✔] Sin inyección posible (no hay SQL ni ejecución de comandos); el separador `;` se escapa en el CSV.
- [◐] El archivo CSV no está cifrado ni protegido: aceptable para el alcance académico → R-9.

### Rendimiento
- [◐] Búsqueda lineal y reescritura completa del archivo en cada cambio → R-8 (suficiente para el volumen esperado).

### Criterios de aceptación técnica (TAC)
| Criterio | Estado | Evidencia |
|---|---|---|
| El código compila y pasan todas las pruebas | ✔ | `docs/evidencias/tests-resultado.txt` (85/85) |
| No rompe funcionalidades existentes | ✔ | `./scripts/comparar-versiones.sh` (6 de 6) |
| Respeta SOLID / DRY | ✔ | `docs/02-solid-clean-code.md` |
| Estándares de nombres y estilo | ✔ | revisión manual; *linter* automático pendiente (R-7) |
| Incluye pruebas unitarias o de integración | ✔ | 85 pruebas en 5 niveles |
| Manejo correcto de errores y excepciones | ✔ | sin `catch` vacíos; excepciones propias |
| No introduce deuda técnica innecesaria | ◐ | R-4, R-5, R-6 documentadas como deuda *consciente* |
| Cobertura ≥ 80 % | ⏳ | se mide en CI con JaCoCo (ver `docs/04`) |
| Atomicidad: un cambio = un propósito | ✔ | un commit por refactoring, con su hash |
| Documentación actualizada | ✔ | README + `docs/` |

## 2. Observaciones y feedback

Cada observación sigue la estructura *hecho objetivo → impacto → propuesta* (feedback sobre el código, no sobre la persona).

| ID | Sev. | Ubicación | Observación (hecho) | Impacto | Propuesta | Estado |
|---|---|---|---|---|---|---|
| R-1 | Media | `Solicitud.reconstruir` | Recibe 8 parámetros; varios son del mismo concepto (`NuevaSolicitud` ya los agrupa). | Firma frágil: dos `String` consecutivos se pueden intercambiar sin error de compilación. | Reutilizar `NuevaSolicitud` como *Parameter Object*: 8 → 5. | ✔ Corregido en `d737d7d` |
| R-2 | Baja | `FiltroSolicitudes` | Un criterio `null` produce `NullPointerException` en `coincide`. | Falla al combinar filtros desde código nuevo (no desde el menú, que siempre pasa texto). | Normalizar `null` a "todos" en el constructor compacto + prueba. | ✔ Corregido en `d737d7d` |
| R-3 | Baja | `ConsolaMenu` | Las opciones de tipo/responsable del *prompt* estaban escritas a mano. | Al agregar un tipo hay que acordarse de editar el texto (viola OCP). | `Enums.opciones(Class)` genera la lista; agregar un tipo es 1 línea. | ✔ Corregido en `d737d7d` |
| R-4 | **Alta** | `SolicitudService` | Si `guardar()` falla *después* de modificar la solicitud en memoria, memoria y archivo quedan distintos hasta el próximo guardado exitoso. | Posible desajuste visible en la misma sesión tras un error de disco. | Guardar antes de confirmar el cambio (*unit of work*) o revertir el cambio ante el fallo. | ⏳ Diferido (fuera de alcance; documentado en conclusiones) |
| R-5 | Baja | enums `desde()` | Devuelven `null` si el texto no corresponde. | El llamador debe acordarse de chequear; `requerir` lo centraliza en la consola. | Cambiar a `Optional` en una iteración futura. | ◐ Aceptado |
| R-6 | Baja | `ConsolaMenu.cambiarEstado/asignarResponsable` | Llama a `servicio.obtener(id)` solo para que "no existe la solicitud" aparezca antes que "estado desconocido". | Acoplamiento al orden de errores. | Se preserva a propósito: el orden es parte del comportamiento externo (prueba `laExistenciaDeLaSolicitudSeValidaAntesQueElEstado`). | ◐ Aceptado |
| R-7 | Baja | proyecto | No hay *linter* ni formateador automático. | El estilo depende de la revisión manual. | Agregar Checkstyle/Spotless al CI. | ⏳ Pendiente |
| R-8 | Info | `SolicitudService.buscar`, `CsvSolicitudRepository` | Búsqueda lineal y reescritura completa del archivo en cada cambio. | Irrelevante para cientos de solicitudes; no escala a miles. | Cambiar el repositorio por una base de datos (solo se escribe otra implementación). | ◐ Aceptado |
| R-9 | Info | datos | CSV sin cifrar. | Sin riesgo con datos sintéticos. | Si hubiera datos reales: cifrado y control de acceso. | ◐ Aceptado |

**Reconocimientos (lo que está bien hecho):** red de seguridad escrita *antes* de refactorizar; pruebas de contrato que comprueban LSP; un commit
por refactoring con la suite en verde; decisiones deliberadas documentadas (ADR); defecto latente descubierto y corregido con evidencia.

### Ejemplo de comentario en el Pull Request (estructura profesional)
> **Lo bueno:** extraer `SolicitudRepository` dejó los casos de uso testeables sin tocar el disco; muy buena la prueba de contrato.
> **A mejorar (R-1):** `Solicitud.reconstruir` toma 8 parámetros y varios son del mismo tipo; es fácil intercambiarlos sin que el compilador avise.
> **Propuesta:** reutilizar `NuevaSolicitud`, que ya agrupa esos datos. Quedaría con 5 y sin tocar a los llamadores externos.
> **Criterio de aceptación:** las 85 pruebas en verde y la comparación con `estado-inicial` sin diferencias.

## 3. Resultado
Hallazgos: 9 · corregidos: 3 · aceptados con justificación: 4 · diferidos/pendientes: 2 (R-4, R-7). Veredicto: **aprobado con observaciones** (re-ejecutadas
las 85 pruebas y la comparación antes/después tras las correcciones).
