# Decisiones técnicas (ADR breves)

Formato: contexto → decisión → consecuencias.

## ADR-1 — Red de seguridad *golden master* antes de refactorizar
**Contexto.** La versión inicial no tenía pruebas y su comportamiento visible es la única especificación disponible.
**Decisión.** Escribir primero escenarios de referencia que ejecutan el programa como proceso externo y comparan su salida con la de `estado-inicial` (tag `etapa-pruebas-base`).
**Consecuencias.** (+) Permite refactorizar profundo (incluso reemplazar la clase central) con confianza. (–) Fija también comportamientos discutibles (ver ADR-6).

## ADR-2 — Excepciones propias en lugar de códigos de error
**Contexto.** `-1..-4` se interpretaban en dos clases distintas; los `catch` vacíos ocultaban fallos.
**Decisión.** `SolicitudException` (error de negocio/validación, mensaje apto para la persona usuaria) y `ArchivoDatosException` (error de E/S o datos dañados). Un único punto de captura en la interfaz.
**Consecuencias.** (+) Flujo principal limpio; imposible “olvidar” un código. (–) Cambia el contrato interno (no el visible): las pruebas se adaptaron de forma mecánica.

## ADR-3 — Repositorio detrás de una interfaz (DIP) + composition root
**Contexto.** Estado global estático y archivo fijo impedían aislar pruebas y reemplazar el almacenamiento.
**Decisión.** `SolicitudRepository` (2 operaciones) con `CsvSolicitudRepository`; el servicio lo recibe por constructor; `Main` ensambla.
**Consecuencias.** (+) Pruebas unitarias sin disco; cambiar a BD = otra implementación. (–) Más clases; se compensa con una prueba de contrato común.

## ADR-4 — Enums con comportamiento (OCP) en lugar de *Strategy* con jerarquías
**Contexto.** Tipo, prioridad base y plazo extra eran cadenas `if/else`. El apunte muestra *Strategy* para descuentos.
**Decisión.** Enums que llevan sus datos y reglas (`TipoSolicitud(prioridadBase, plazoExtra)`, `Estado.puedePasarA`). Las diferencias entre tipos son **datos**, no algoritmos distintos, por lo que una jerarquía de clases sería sobre-diseño.
**Consecuencias.** Experimento ejecutado (`scripts/experimento-ocp.sh`, `docs/evidencias/experimento-ocp.txt`): agregar `CAMBIO_COMISION(Prioridad.MEDIA, 2)` es **una constante nueva en un solo archivo** y habilita el alta, el reporte, el plazo y el texto del menú; en la versión inicial había que editar 4 lugares en 2 archivos. Si en el futuro cada tipo necesitara lógica propia (p. ej. flujos distintos), se evolucionaría a *Strategy*.

## ADR-5 — Escapar `;` en el CSV (mejora deliberada)
**Contexto.** Un `;` en un texto corrompía el registro al recargar, en silencio (`docs/evidencias/defecto-latente-separador.txt`).
**Decisión.** Escapar `;` y la barra invertida con barra invertida; leer con un separador que respeta el escape. Los datos sin esos caracteres se guardan **idénticos** a antes (prueba `elFormatoDeLosDatosNormalesNoCambio...`).
**Consecuencias.** Corrige el defecto sin romper archivos existentes. Es la única diferencia observable en el archivo y está documentada.

## ADR-6 — Se preservan reglas discutibles para no mezclar refactoring con cambios funcionales
**Contexto.** La validación de email (`@` en posición ≥ 1 y un `.` después) es débil; el orden de validación (legajo → nombre → email → tipo) es peculiar.
**Decisión.** No tocarlas: un refactoring cambia la estructura, no el comportamiento. Quedan como mejora futura en su propio ticket, con su propia prueba.

## ADR-7 — JUnit 4 y scripts sin Maven además del `pom.xml`
**Contexto.** El borrador se desarrolló en un entorno sin acceso a Maven Central.
**Decisión.** Incluir `lib/` con JUnit 4.13.2 y `scripts/build.sh` / `test.sh` (solo JDK) **y** un `pom.xml` con JaCoCo para el CI. Ambas vías ejecutan las mismas pruebas.
**Consecuencias.** Cualquiera puede compilar y probar con solo un JDK; el CI genera la cobertura.

## ADR-8 — Pruebas de caracterización del archivo de datos corrupto como mejora, no como contrato inicial
La versión inicial ignoraba un archivo dañado y seguía con datos incompletos. Ahora el programa avisa (archivo y línea) y termina con código 1.
Es un cambio de comportamiento **deliberado y probado** (`ArchivoDatosCorruptoTest`), aprobado en el diagnóstico (hallazgo #5).

## ADR-9 — Regla para separar refactorings de cambios deliberados de comportamiento
**Contexto.** ADR-5 y ADR-8 cambian comportamiento mientras ADR-6 lo preserva; hace falta un criterio explícito.
**Decisión.** (1) Un refactoring **nunca** cambia el comportamiento externo y se verifica con la comparación 6/6. (2) Un cambio de comportamiento se admite solo si el comportamiento previo es un **defecto** que pierde o corrompe datos o esconde errores (escape de `;`, archivo dañado, R-4); no se admite para reglas de negocio discutibles (email, orden de validación), que van a un ticket propio. (3) Todo cambio deliberado lleva su prueba (idealmente en rojo antes), su ADR y se lista en `docs/03` («Cambios deliberados»).
**Consecuencias.** La comparación 6/6 sigue siendo válida como evidencia del refactoring; las diferencias aceptadas están acotadas y trazadas.

## ADR-10 — R-4: revertir en memoria en lugar de guardar antes de confirmar
**Contexto.** Si `guardar()` fallaba tras modificar una solicitud, memoria y archivo divergían.
**Alternativas.** (a) *Unit of work*: guardar un estado candidato y confirmarlo después; (b) snapshot y restauración de `Seguimiento` si falla el guardado.
**Decisión.** (b): cambio mínimo (2 métodos en `Solicitud`, un `try/catch` en el servicio), sin duplicar la lista de solicitudes y con el mismo comportamiento visible cuando todo funciona.
**Consecuencias.** El alta además no consume el próximo ID si falla. Límite: si el archivo queda parcialmente escrito por un corte de energía no hay recuperación (fuera de alcance; se resolvería con escritura a archivo temporal + renombrado atómico).

## ADR-11 — Dobles de prueba escritos a mano (sin Mockito)
**Contexto.** El entorno de desarrollo no tenía acceso a Maven Central (ADR-7).
**Decisión.** `RepositorioEnMemoria` (fake con interruptor de fallo = stub) y `RepositorioEspia` (mock manual que registra el protocolo). Con Maven se reemplazaría el espía por Mockito (`verify(...)`) sin cambiar el diseño.
