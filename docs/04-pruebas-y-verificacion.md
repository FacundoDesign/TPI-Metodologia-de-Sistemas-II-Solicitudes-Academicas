# Pruebas y verificación del comportamiento

**Pregunta que responde esta etapa:** ¿cómo sabemos que el refactoring *no alteró negativamente* el comportamiento externo?
La respuesta combina cuatro niveles de evidencia (Unidad 3: V&V, pruebas unitarias, de contrato y de regresión).

## Niveles de prueba

| Nivel | Qué verifica | Dónde | Pruebas |
|---|---|---|---|
| **Caracterización / regresión (golden master)** | Que la salida visible y el archivo de datos son los mismos que producía la versión inicial. Ejecuta el programa como proceso externo, como una persona usuaria. | `ComportamientoExternoTest` + `src/test/resources/golden/` (6 escenarios, 7 ejecuciones del programa) | 7 |
| **Unitarias** (patrón AAA) | Reglas de negocio aisladas del disco y de la consola, con un doble de prueba del repositorio (`RepositorioEnMemoria`). | `SolicitudServiceTest`, `SolicitudReglasTest`, `ReporteSolicitudesTest`, `EstadoTest`, `PrioridadTest`, `TipoSolicitudTest`, `EstudianteTest`, `FiltroSolicitudesTest`, `EnumsDominioTest`, `SolicitudTest` | 53 |
| **De contrato** (LSP) | Que toda implementación de `SolicitudRepository` se comporta igual. | `ContratoSolicitudRepository` ejecutada por `RepositorioEnMemoriaContratoTest` y `CsvSolicitudRepositoryTest` | 6 |
| **Componente / integración** | Formato CSV, escape de separadores y errores de datos dañados con archivos reales temporales; la consola completa dentro del mismo proceso. | `CsvSolicitudRepositoryTest`, `ArchivoDatosCorruptoTest`, `ConsolaMenuTest` | 19 |
| **Total** | | | **85** |

Resultado de la última ejecución: `docs/evidencias/tests-resultado.txt` → `OK (85 tests)`.

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
El script compila el tag `estado-inicial` en una carpeta temporal y ejecuta **las mismas entradas** contra las dos versiones; compara la salida de
pantalla y el contenido de `solicitudes.csv` byte a byte.

## Casos de prueba principales (trazabilidad con los casos de uso)

| ID | Caso de uso | Entrada / situación | Resultado esperado | Prueba |
|---|---|---|---|---|
| CP-01 | Registrar | CERTIFICADO, no urgente | ID 1, prioridad BAJA, plazo 10 días | escenario 01, `registrarMuestraIdPrioridadYPlazo` |
| CP-02 | Registrar | CERTIFICADO urgente | prioridad sube a MEDIA | `unaSolicitudUrgenteSubeUnNivelDePrioridad` |
| CP-03 | Registrar | legajo de 3 dígitos | `Error: legajo invalido (se esperan 5 digitos)` y no se crea nada | escenario 02, `unErrorDeValidacionSeMuestra...` |
| CP-04 | Registrar | tipo PASAPORTE | `Error: tipo de solicitud desconocido` | escenario 02 |
| CP-05 | Cambiar estado | PENDIENTE → RESUELTA | rechazado: `transicion invalida PENDIENTE -> RESUELTA`, nada se guarda | `unaTransicionInvalidaSeRechazaYNoPersiste` |
| CP-06 | Cambiar estado | PENDIENTE → EN_PROCESO sin responsable | `debe asignar responsable antes de pasar a EN_PROCESO` | `pasarAEnProcesoExigeResponsableAsignado` |
| CP-07 | Cambiar estado | id inexistente con estado inválido | informa que la solicitud no existe (orden de validación preservado) | `laExistenciaDeLaSolicitudSeValidaAntesQueElEstado` |
| CP-08 | Asignar | solicitud RECHAZADA | `la solicitud esta cerrada` | `noSePuedeAsignarResponsableAUnaSolicitudCerrada` |
| CP-09 | Buscar | filtro por tipo y/o estado | solo las que coinciden; sin resultados → `Sin resultados` | escenario 04, `buscarAplicaElFiltro...` |
| CP-10 | Reporte | sin datos / con datos | ceros y `0.0 dias`; conteos y promedio redondeado a 1 decimal | escenario 05, `ReporteSolicitudesTest` |
| CP-11 | Persistencia | cerrar y reabrir el programa | los datos siguen ahí; los IDs continúan | escenario 06, `losIdsContinuanDespuesDeCargarDatosExistentes` |
| CP-12 | Errores de datos | archivo con línea de 3 campos / estado desconocido | el programa avisa con archivo y línea y termina con código 1 | `ArchivoDatosCorruptoTest`, `CsvSolicitudRepositoryTest` |
| CP-13 | Defecto latente | descripción con `;` | se guarda y se recarga intacta (antes se corrompía) | `unPuntoYComaEnUnTextoNoCorrompeElRegistro`, `docs/evidencias/defecto-latente-separador.txt` |

## Cómo ejecutar
```bash
./scripts/test.sh                  # sin Maven: solo JDK 17+ y los jars de lib/ (resultado: OK (85 tests))
mvn -B verify                      # con Maven: ejecuta las pruebas y genera cobertura JaCoCo (target/site/jacoco)
./scripts/comparar-versiones.sh    # evidencia antes/después contra el tag estado-inicial
```

## Límites reconocidos
- La **cobertura de líneas** se mide con JaCoCo en el CI (`mvn verify`); el objetivo de la materia es ≥ 80 % (Definition of Done). No se registró un porcentaje local porque
  el entorno de desarrollo del borrador no tenía acceso a Maven Central. *El equipo debe correr `mvn verify` y copiar el porcentaje real al informe.*
- Los escenarios *golden master* fijan el comportamiento actual, incluidas reglas discutibles (p. ej. la validación débil del email): se preservan a propósito (ver ADR-6).
