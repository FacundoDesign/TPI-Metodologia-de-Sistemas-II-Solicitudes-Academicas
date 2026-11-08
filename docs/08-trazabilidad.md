# Trazabilidad

## 1. Smell → principio → refactoring → commit → pruebas → evidencia
| # | Smell (docs/01) | Principio | Refactoring (Fowler) | Commit | Pruebas que lo protegen | Evidencia |
|---|---|---|---|---|---|---|
| 1 | God Class | SRP | Extract Class | `1bc528a` | `SolicitudServiceTest`, `ReporteSolicitudesTest`, `CsvSolicitudRepositoryTest` | `metricas-despues.txt` |
| 2 | Long Method (`Main.main`) | Clean Code (funciones chicas) | Extract Method | `f6160df` | `ComportamientoExternoTest`, `ConsolaMenuTest` | `metricas-*.txt`, `comparacion-antes-despues.txt` |
| 3 | Strings mágicos | Tipado / OCP | Replace Type Code with Enum | `2d038f8` | `EstadoTest`, `TipoSolicitudTest`, `PrioridadTest` | `experimento-ocp.txt` |
| 4 | Códigos de error | Clean Code (excepciones) | Replace Error Code with Exception | `7503256` | `SolicitudServiceTest` | `tests-por-clase.txt` |
| 5 | `catch` vacío | Clean Code | Replace Error Code with Exception | `7503256` | `CsvSolicitudRepositoryTest` | `defecto-latente-separador.txt` |
| 6 | Estado global | DIP | Extract Class + inversión | `1bc528a` | `SolicitudServiceTest` (repositorio en memoria) | `tests-resultado.txt` |
| 7 | Duplicación | DRY | Extract Method | `f6160df` | golden master | `comparacion-antes-despues.txt` |
| 8 | Long Parameter List | Clean Code | Introduce Parameter Object | `1a55ea3`, `499c5ab` | `FiltroSolicitudesTest`, `SolicitudTest` | `metricas-despues.txt` (máx. 4 parámetros) |
| 9 | Feature Envy / Data Class | Encapsulamiento | Move Method | `d3e5cb8` | `SolicitudTest` | `tests-por-clase.txt` |
| 10 | if/else sobre tipos | OCP | Replace Type Code with Enum | `2d038f8` | `TipoSolicitudTest` | `experimento-ocp.txt` |
| 11 | Lógica + presentación | SRP | Extract Class | `1bc528a` | `ReporteSolicitudesTest` | `tests-por-clase.txt` |
| 12 | Nombres pobres | Clean Code | Rename | `f6160df` | golden master | revisión de código |
| 13 | Código muerto | Clean Code | Eliminar | `f6160df` | golden master | `git diff estado-inicial` |
| R-4 | Inconsistencia memoria/disco | Robustez | Corrección TDD (ADR-10) | `bd1b3f5` | `SolicitudServiceConsistenciaTest` (7) | `r4-fase-roja.txt` |

## 2. Consigna → etapa → dónde se cumple
| Etapa de la consigna | Documento / sección | Evidencia |
|---|---|---|
| 1. Sistema base | `docs/01` (origen), tag `estado-inicial` | `git show estado-inicial` |
| 2. Diagnóstico calidad | `docs/01` §1–2 | `metricas-antes.txt` |
| 3. Deuda técnica / smells | `docs/01` §3 | tabla con prioridad |
| 4. SOLID y Clean Code | `docs/02` | `experimento-ocp.txt` |
| 5. Refactoring | `docs/03` | commits 1–7, `comparacion-antes-despues.txt` |
| 6. Pruebas / V&V | `docs/04` | `tests-resultado.txt`, `mutacion.txt` |
| 7. Code review | `docs/05` | checklist, 15 hallazgos |
| 8. Documentación y conclusiones | `README`, `docs/06–08` | ADR 1–11, declaración de IA |

Pendiente del equipo: PR real con revisión humana, cobertura medida con JaCoCo, datos personales y video (ver `PENDIENTES_PARA_EL_EQUIPO.md`).
