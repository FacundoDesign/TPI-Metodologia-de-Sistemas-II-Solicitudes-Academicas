# Gestión de Solicitudes Académicas — TPI Metodología de Sistemas II

Sistema de consola para **registrar solicitudes de estudiantes** (certificados, equivalencias, revisión de nota, baja de materia),
**asignarles responsable**, **cambiar su estado**, **buscarlas/filtrarlas** y **obtener un reporte**. Los datos se guardan en `solicitudes.csv` (datos sintéticos).

> Trabajo Práctico Integrador: *evolución profesional de un sistema existente — calidad, deuda técnica, refactoring y revisión técnica*.
> El repositorio contiene la **versión inicial** (con deuda técnica) y la **versión refactorizada**, con todo el recorrido documentado.

## Integrantes
| Integrante | Rol / contribución principal | Usuario de GitHub |
|---|---|---|
| **[COMPLETAR: nombre y apellido]** | **[COMPLETAR]** | **[COMPLETAR]** |
| **[COMPLETAR]** | **[COMPLETAR]** | **[COMPLETAR]** |

Materia: Metodología de Sistemas II · Tecnicatura Universitaria en Programación a Distancia · Comisión **[COMPLETAR]** · Docente **[COMPLETAR]**
Video de defensa: **[COMPLETAR: enlace]**

## Funcionalidades
1. Registrar solicitud (valida legajo de 5 dígitos, nombre, email y tipo; calcula prioridad y plazo; la urgencia sube un nivel de prioridad).
2. Cambiar estado (`PENDIENTE → EN_PROCESO → RESUELTA`, y `RECHAZADA` desde cualquier estado abierto; `EN_PROCESO` exige responsable).
3. Asignar responsable (`BEDELIA`, `ALUMNADO`, `SECRETARIA`; no se puede en solicitudes cerradas).
4. Buscar / filtrar por estado, tipo, prioridad, responsable y legajo (criterio vacío = todos).
5. Reporte: totales por estado y prioridad, solicitudes sin responsable y plazo promedio de respuesta.

## Tecnologías
Java 17+ (records, enums con comportamiento) · JUnit 4.13.2 · Maven + JaCoCo (CI) · GitHub Actions · scripts Bash/Python para evidencias.

## Instalación y uso
Requisito: **JDK 17 o superior**.
```bash
./scripts/build.sh                                   # compila (no requiere Maven)
cd /ruta/de/trabajo && java -cp /ruta/al/repo/build/main ar.edu.solicitudes.Main
./scripts/test.sh                                    # 85 pruebas -> OK (85 tests)
mvn -B verify                                        # alternativa con Maven + cobertura JaCoCo
./scripts/comparar-versiones.sh                      # evidencia antes/después contra el tag estado-inicial
python3 scripts/metricas.py                          # métricas de calidad interna
```
El programa guarda `solicitudes.csv` en el directorio desde el que se ejecuta.

## Arquitectura (versión refactorizada)
```
Main ──▶ ConsolaMenu ──▶ SolicitudService ──▶ «interface» SolicitudRepository ◀── CsvSolicitudRepository
                 └────▶ ReporteSolicitudes
Dominio: Solicitud · Estudiante · NuevaSolicitud · FiltroSolicitudes · Estado · Prioridad · TipoSolicitud · Responsable
Errores: SolicitudException · ArchivoDatosException
```

## Recorrido del TPI (tags de Git)
| Tag | Qué marca | Etapa |
|---|---|---|
| `estado-inicial` | Sistema base con deuda técnica (God Class, strings mágicos, códigos de error…) | 1 |
| `etapa-diagnostico` | Diagnóstico: calidad interna/externa, 13 code smells priorizados | 2, 3 |
| `etapa-pruebas-base` | Red de seguridad (*golden master*) escrita **antes** de refactorizar | 6 |
| `etapa-refactoring` | Los 6 refactorings aplicados (un commit cada uno) | 4, 5 |
| `etapa-pruebas` | Pruebas unitarias, de contrato y de consola (83 pruebas; 85 tras el code review) | 6 |
| `etapa-code-review` | Correcciones surgidas de la revisión técnica | 7 |
| `etapa-docs` | Documentación, CI y plantillas de GitHub completas | 7, 8 |
| `estado-refactorizado` (alias `estadorefactorizado`) | Estado final del código | — |

```bash
git checkout estado-inicial          # ver la versión inicial
git diff estado-inicial estado-refactorizado --stat
git log --oneline --decorate
```

## Documentación (`/docs`)
| Archivo | Contenido |
|---|---|
| [01-diagnostico.md](docs/01-diagnostico.md) | Calidad externa vs. interna, métricas y 13 code smells con evidencia, impacto, riesgo y prioridad |
| [02-solid-clean-code.md](docs/02-solid-clean-code.md) | Aplicación de SRP/OCP/LSP/ISP/DIP y de Clean Code, antes y después |
| [03-refactorings.md](docs/03-refactorings.md) | 6 refactorings con fragmentos antes/después, commits y resultados en números |
| [04-pruebas-y-verificacion.md](docs/04-pruebas-y-verificacion.md) | Estrategia de pruebas, casos de prueba y evidencia antes/después |
| [05-code-review.md](docs/05-code-review.md) | Checklist técnico, observaciones, feedback y criterios de aceptación |
| [06-decisiones-tecnicas.md](docs/06-decisiones-tecnicas.md) | Decisiones de diseño (ADR) |
| [07-uso-de-ia.md](docs/07-uso-de-ia.md) | Declaración de uso de IA y registro de prompts |
| [evidencias/](docs/evidencias) | Salidas reproducibles: métricas, pruebas, comparación antes/después, defecto latente |

## Flujo de trabajo
GitHub Flow: rama `main` protegida, una rama por cambio (`refactor/...`, `test/...`, `docs/...`), Pull Request con la plantilla de code review
(`.github/PULL_REQUEST_TEMPLATE.md`) y CI verde antes de integrar. Issues con etiquetas `bug`, `technical-debt`, `documentation`, `feature`.
