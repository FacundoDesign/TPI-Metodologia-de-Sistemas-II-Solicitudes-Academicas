# Pruebas y verificación del comportamiento

**Pregunta que responde esta etapa:** ¿cómo sabemos que el refactoring *no alteró negativamente* el comportamiento externo?
La respuesta combina cinco niveles de evidencia (Unidad 3: V&V, pruebas unitarias, de contrato, de regresión y mutación).

## Vocabulario de dobles de prueba (Unidad 3) y qué usa este proyecto
| Término del apunte | Definición | En este proyecto |
|---|---|---|
| **Stub** | objeto estático que devuelve respuestas preconfiguradas | `RepositorioEnMemoria` con `fallarAlGuardar = true` actúa como stub de un disco que falla |
| **Mock** | doble que audita el protocolo de interacción: qué métodos se llamaron, cuántas veces y con qué contenido | `RepositorioEspia` (escrito a mano, sin librerías): registra cada `guardarTodas` y verifica el contenido guardado |
| **Fake** | implementación funcional simplificada (no es stub ni mock) | `RepositorioEnMemoria` cuando guarda y devuelve datos en una lista |

*Nota:* en un proyecto con acceso a Maven Central se usaría Mockito; aquí los dobles son manuales (ADR-11).

## Niveles de prueba
| Nivel | Qué verifica | Dónde | Pruebas |
|---|---|---|---|
| **Caracterización / regresión (golden master)** | Que la salida visible y el archivo de datos son los mismos que producía la versión inicial. Ejecuta el programa como proceso externo. | `ComportamientoExternoTest` + `src/test/resources/golden/` (6 escenarios, 7 ejecuciones) | 7 |
| **Unitarias** (estructura AAA: *Arrange*, *Act*, *Assert* separados por líneas en blanco; marcadas con comentarios `// Arrange`, `// Act`, `// Assert` en la primera prueba de `SolicitudServiceConsistenciaTest` como modelo) | Reglas de negocio aisladas del disco y de la consola, con fake, stub y mock del repositorio. | `SolicitudServiceTest`, `SolicitudServiceConsistenciaTest`, `SolicitudReglasTest`, `ReporteSolicitudesTest`, `EstadoTest`, `PrioridadTest`, `TipoSolicitudTest`, `EstudianteTest`, `FiltroSolicitudesTest`, `EnumsDominioTest`, `SolicitudTest` | 60 |
| **De contrato de interfaz (LSP)** | Que toda implementación de `SolicitudRepository` se comporta igual (misma precondición/postcondición). *No es* el "contract testing consumer/provider" entre servicios de la Unidad 3: aquí es una clase abstracta de pruebas ejecutada por cada implementación. | `ContratoSolicitudRepository` ejecutada por `RepositorioEnMemoriaContratoTest` y `CsvSolicitudRepositoryTest` | 6 |
| **Componente / integración** | Formato CSV, escape de separadores y datos dañados con archivos reales temporales; la consola completa en el mismo proceso. | `CsvSolicitudRepositoryTest` (propias), `ArchivoDatosCorruptoTest`, `ConsolaMenuTest` | 19 |
| **Total** | | | **92** |
| **Mutación manual** | Que las pruebas realmente *detectan* defectos (una prueba que nunca falla no prueba nada). | `scripts/mutacion.py` → `docs/evidencias/mutacion.txt` | 14 mutantes, **14 matados** |

Resultado de la última ejecución: `docs/evidencias/tests-resultado.txt` → `OK (92 tests)`.

## Evidencia antes / después (comparación directa con la versión inicial)
```
$ ./scripts/comparar-versiones.sh
01-registro-y-prioridades        salida y archivo de datos identicos: SI
02-validaciones-registro         salida y archivo de datos identicos: SI
03-flujo-estados                 salida y archivo de datos identicos: SI
04-busqueda-filtros              salida y archivo de datos identicos: SI
05-reporte-vacio                 salida y archivo de datos identicos: SI
06-persistencia                  salida y archivo de datos identicos: SI
Escenarios identicos: 6 de 6
```
El script compila el tag `estado-inicial` en una carpeta temporal y ejecuta **las mismas entradas** contra las dos versiones; compara la salida de pantalla y `solicitudes.csv` byte a byte.

## Metodología aplicada
- **Antes de refactorizar:** red de seguridad *golden master* (tag `etapa-pruebas-base`). Se prueba el comportamiento, no la estructura, para poder cambiar la estructura sin tocar la prueba.
- **R-4 con TDD (Rojo → Verde → Refactor):** primero se escribieron las pruebas que fallaban (`docs/evidencias/r4-fase-roja.txt`: 4 fallos con el código previo), después la corrección mínima, y la suite quedó en verde (92).
- **FIRST:** las pruebas son rápidas (~1,5 s para 92), independientes (cada una crea su propio repositorio/archivo temporal), repetibles y se auto-validan (`OK`/`FAILURES`). *Limitación:* las pruebas unitarias del refactoring se escribieron después del código; la excepción es R-4 (TDD).

## Casos de prueba principales (trazabilidad con los casos de uso)
| ID | Caso de uso | Entrada / situación | Resultado esperado | Prueba |
|---|---|---|---|---|
| CP-01 | Registrar | CERTIFICADO, no urgente | ID 1, prioridad BAJA, plazo 10 días | escenario 01, `registrarMuestraIdPrioridadYPlazo` |
| CP-02 | Registrar | CERTIFICADO urgente | prioridad sube a MEDIA | `unaSolicitudUrgenteSubeUnNivelDePrioridad` |
| CP-03 | Registrar | legajo de 3 dígitos | `Error: legajo invalido (se esperan 5 digitos)` y no se crea nada | escenario 02 |
| CP-04 | Registrar | tipo PASAPORTE | `Error: tipo de solicitud desconocido` | escenario 02 |
| CP-05 | Cambiar estado | PENDIENTE → RESUELTA | rechazado: `transicion invalida PENDIENTE -> RESUELTA`, nada se guarda | `unaTransicionInvalidaSeRechazaYNoPersiste`, `unaTransicionInvalidaNoInvocaAlRepositorio` (mock) |
| CP-06 | Cambiar estado | PENDIENTE → EN_PROCESO sin responsable | `debe asignar responsable antes de pasar a EN_PROCESO` | `pasarAEnProcesoExigeResponsableAsignado` |
| CP-07 | Cambiar estado | id inexistente con estado inválido | informa que la solicitud no existe (orden de validación preservado) | `laExistenciaDeLaSolicitudSeValidaAntesQueElEstado` |
| CP-08 | Asignar | solicitud RECHAZADA | `la solicitud esta cerrada` | `noSePuedeAsignarResponsableAUnaSolicitudCerrada` |
| CP-09 | Buscar | filtro por tipo y/o estado | solo las que coinciden; sin resultados → `Sin resultados` | escenario 04, `FiltroSolicitudesTest` |
| CP-10 | Reporte | sin datos / con datos | ceros y `0.0 dias`; conteos y promedio a 1 decimal | escenario 05, `ReporteSolicitudesTest` |
| CP-11 | Persistencia | cerrar y reabrir | los datos siguen ahí; los IDs continúan | escenario 06, `losIdsContinuanDespuesDeCargarDatosExistentes` |
| CP-12 | Errores de datos | archivo con línea de 3 campos / estado desconocido | avisa con archivo y línea y termina con código 1 | `ArchivoDatosCorruptoTest` |
| CP-13 | Defecto latente | descripción con `;` | se guarda y se recarga intacta | `unPuntoYComaEnUnTextoNoCorrompeElRegistro` |
| CP-14 | Consistencia (R-4) | el disco falla al guardar | la memoria vuelve al estado previo y el error se informa | 4 pruebas con stub en `SolicitudServiceConsistenciaTest` |
| CP-15 | Interacción | cambio de estado válido | exactamente 1 guardado con el contenido esperado | `cambiarEstadoValidoGuardaUnaVezConElEstadoNuevo` (mock) |
| CP-16 | Extensibilidad (OCP) | agregar el tipo CAMBIO_COMISION | el sistema lo acepta con 1 línea nueva | `scripts/experimento-ocp.sh` |

## Cómo ejecutar
```bash
./scripts/test.sh                  # sin Maven: solo JDK 17+ y los jars de lib/ (resultado: OK (92 tests))
mvn -B verify                      # con Maven: ejecuta las pruebas y genera cobertura JaCoCo (target/site/jacoco)
./scripts/comparar-versiones.sh    # evidencia antes/después contra el tag estado-inicial
python3 scripts/mutacion.py        # mutación manual (14 mutantes)
./scripts/experimento-ocp.sh       # costo de agregar un tipo, antes vs. después
```

## Qué NO cubre esta red de seguridad (límites reconocidos)
- **Cobertura porcentual:** se mide con JaCoCo en el CI (`mvn verify`); el objetivo de la Definition of Done es ≥ 80 %. **[COMPLETAR: copiar el porcentaje real de líneas y de ramas del reporte JaCoCo.]** El entorno donde se desarrolló el borrador no tenía acceso a Maven Central, por eso no se pudo calcular acá. La mutación (14/14) y la trazabilidad de `docs/08` son evidencia complementaria, no un reemplazo.
- Los escenarios *golden master* fijan el comportamiento actual, incluidas reglas discutibles (validación débil del email): se preservan a propósito (ADR-6).
- No se prueban entradas masivas ni concurrencia (sistema de un solo usuario en consola) ni la interfaz con caracteres especiales del terminal.
- Se usaron 14 mutantes manuales, no una herramienta de mutación automática (p. ej. PIT).
