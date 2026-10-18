# Aplicación de SOLID y Clean Code

Cada principio se justifica con **dónde estaba el problema** (tag `estado-inicial`), **qué se cambió** y **cómo se comprueba**.
Los commits se citan con su hash (`git show <hash>`).

## SOLID

| Principio | Problema en la versión inicial | Mejora aplicada | Evidencia |
|---|---|---|---|
| **SRP** (una razón para cambiar) | `GestorSolicitudes` tenía 6 razones para cambiar: formato del archivo, validación de datos, reglas de negocio, búsqueda, cálculo/impresión del reporte y mensajes. `Main.main` mezclaba menú, lectura y formato. | Extract Class (`0c11528`): `SolicitudService` (casos de uso), `CsvSolicitudRepository` (formato), `ReporteSolicitudes` (cálculo), `ConsolaMenu` (interfaz). `Estudiante` valida sus propios datos; `Solicitud` impone su máquina de estados. | La clase de producción con más decisiones pasó de 80 a 13 (`docs/evidencias/metricas-despues.txt`). Las clases con lógica tienen su propia prueba unitaria. |
| **OCP** (abierto a extensión, cerrado a modificación) | Tipos, estados y prioridades eran *strings* comparados con `if/else` en 3 lugares (validación, prioridad base y reporte). Agregar un tipo obligaba a editar código que ya funcionaba. | `TipoSolicitud`, `Estado`, `Prioridad` y `Responsable` son enums que **llevan su propio comportamiento** (prioridad base, plazo extra, transiciones permitidas). `ReporteSolicitudes` y los *prompts* del menú recorren `values()`. | Experimento reproducible: agregar `CAMBIO_COMISION(Prioridad.MEDIA, 2)` es **una línea** en `TipoSolicitud`; el alta, el reporte, el plazo y el texto del menú se adaptan solos (ver `docs/06-decisiones-tecnicas.md`, ADR-4). |
| **LSP** (sustitución) | No había abstracciones que sustituir. | Al introducir `SolicitudRepository` se escribió una **prueba de contrato** (`ContratoSolicitudRepository`) que ejecutan la implementación CSV y la implementación en memoria: ambas deben comportarse igual. | `RepositorioEnMemoriaContratoTest` y `CsvSolicitudRepositoryTest` pasan las mismas 3 pruebas heredadas. |
| **ISP** (interfaces pequeñas) | No existían interfaces. | `SolicitudRepository` tiene solo 2 operaciones y el servicio usa las 2. `ReporteSolicitudes.generar` recibe un `Collection<Solicitud>` en lugar de depender de todo el servicio. | Revisión de `SolicitudRepository.java` y `ReporteSolicitudes.java`. |
| **DIP** (depender de abstracciones) | Estado global estático (`public static List db`, `static String ARCH`), archivo fijo dentro del gestor: imposible de aislar o de reemplazar. | `SolicitudService` recibe un `SolicitudRepository` por constructor. `Main` es el único *composition root* que elige las implementaciones concretas. | `SolicitudServiceTest` usa `RepositorioEnMemoria` (doble de prueba): 13 pruebas que no tocan el disco. |

## Clean Code

| Práctica (Unidad 1) | Antes | Después | Commit |
|---|---|---|---|
| **Nombres que revelan intención** | `db`, `sig`, `ARCH`, `p`, `l`, `n`, `e`, `t`, `d`, `u`, `r`, `ne`, `fe`, `ft`… | `solicitudes`, `proximoId`, `ARCHIVO_DATOS`, `legajo`, `nombre`, `tipo`, `descripcion`, `nuevoEstado`… Nombres del dominio (`pasarA`, `asignarA`, `estaCerrada`). | `4e969b2` |
| **Funciones pequeñas, una sola cosa** | `Main.main` de 107 líneas; `imprimirReporte` calculaba e imprimía. | Método más largo: 21 líneas. `Main.main` tiene 11 líneas y solo conecta objetos. | `4e969b2`, `0c11528` |
| **Excepciones en lugar de códigos de error** | `registrar` devolvía `-1..-4`; `cambiarEstado` y `asignarResponsable` también. El significado de cada código estaba duplicado en `Main`. | `SolicitudException` con el mensaje para la persona usuaria; un único `catch` en `ConsolaMenu`. | `438f472` |
| **No silenciar errores** | `catch (Exception e) {}` en `cargar` y `guardar`: un fallo de disco no dejaba rastro. | `ArchivoDatosException` con archivo y número de línea; el programa avisa y termina con código 1 si el archivo está dañado. | `438f472` |
| **Alta cohesión** | `Solicitud` era una bolsa de campos públicos; las reglas vivían en otra clase (*Feature Envy*). | `Solicitud` encapsula sus campos y contiene `calcularPlazo`, `pasarA`, `asignarA`. Cada enum contiene sus reglas. | `fca39db`, `0c11528` |
| **Bajo acoplamiento** | `Main` y `GestorSolicitudes` se conocían por detalles (códigos numéricos, `static`). | Dependencias por interfaz y por constructor; las capas solo se hablan a través de tipos del dominio. | `0c11528` |
| **Límites con el exterior (wrappers)** | El formato CSV estaba disperso en la lógica de negocio. | Todo lo relativo al archivo vive en `CsvSolicitudRepository`; cambiar a otra base es escribir otra implementación. | `0c11528` |
| **DRY** | Búsqueda por id copiada 3 veces; `trim().toUpperCase()` en 7 lugares; bucle de búsqueda por nombre repetido en cada enum. | `buscarPorId` / `obtener(id)`, `leerTextoEnMayusculas`, utilidad `Enums.buscar`. | `4e969b2`, `dc5e2f0` |
| **Sin código muerto ni comentarios que mienten** | `exportarTxt` (sin uso, con `int x = 0;` y un `TODO`). | Eliminado. Los comentarios explican decisiones (por qué), no repiten el código. | `4e969b2` |
