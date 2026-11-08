# Diagnóstico de calidad — versión inicial (`estado-inicial`)

Sistema analizado: **Gestión de Solicitudes Académicas** (consola, Java, persistencia en CSV).
Alcance del análisis: todo el código de producción (`src/main/java`, 3 archivos, 310 LOC).

> **Origen del sistema base (transparencia).** La consigna admite "un prototipo construido por el equipo". `estado-inicial` es un prototipo
> escrito para este trabajo **con defectos de diseño típicos introducidos a propósito** (God Class, *strings* mágicos, códigos de error,
> `catch` vacíos, estado global). No es un sistema heredado de producción. Su comportamiento visible es real y verificable (6 escenarios).
> Los números de línea citados abajo corresponden a ese tag: se verifican con `git show estado-inicial:src/main/java/ar/edu/solicitudes/<Archivo>.java`.

## 1. Calidad externa vs. calidad interna

| Dimensión | Qué se observa | Evidencia |
|---|---|---|
| **Calidad externa** (lo que percibe el usuario) | Las 5 operaciones funcionan: registrar, cambiar estado, asignar responsable, buscar/filtrar y reporte. Los mensajes de error son claros y los datos persisten entre ejecuciones. | 7 pruebas de caracterización (`ComportamientoExternoTest`) pasan sobre la versión inicial: 6 escenarios de uso, incluyendo errores y persistencia. |
| **Calidad interna** (lo que percibe quien mantiene el código) | Un único objeto (`GestorSolicitudes`) hace de todo; un único método (`Main.main`) de 107 líneas maneja la interfaz; las reglas viven repetidas en cadenas de `if` sobre *strings*; los errores se comunican con códigos numéricos; hay estado global estático y errores silenciados. | `python3 scripts/metricas.py` (ver §2). |

**Idea central (Unidad 1):** calidad externa y calidad interna son dimensiones independientes. Este sistema tiene
buena calidad externa y mala calidad interna: el usuario no ve el problema, pero cada cambio futuro cuesta más
y es más riesgoso. Un ejemplo concreto: agregar el tipo de solicitud `CAMBIO_COMISION` obliga a editar **hasta 4 lugares**
(validación del tipo, prioridad base, plazo y texto del prompt del menú) sin que el compilador avise si
se olvida alguno.

### Mantenibilidad (¿se puede entender, modificar, extender, corregir?)
- **Entender:** nombres de una letra (`l`, `n`, `e`, `t`, `d`, `u`, `r`, `fe`, `ft`…) y un `main` de 107 líneas.
- **Modificar:** una regla de negocio (p. ej. el plazo) vive en `calcularPlazo`, pero su resultado también se
  usa en el reporte y en el mensaje del alta; cambiar los códigos de error obliga a tocar `GestorSolicitudes` **y** `Main`.
- **Extender:** no hay puntos de extensión; todo se resuelve con `if/else`.
- **Corregir:** los errores de lectura/escritura del archivo se tragan (`catch` vacío), así que un fallo no deja rastro.

## 2. Métricas de la versión inicial (reproducibles)

```
$ python3 scripts/metricas.py src/main/java    # sobre el tag estado-inicial
archivo                                LOC  metod  decis  liter  stat catchV
GestorSolicitudes.java                 186     10     80     43     3      2
Main.java                              111      1     24      6     0      0
Solicitud.java                          13      0      0      0     0      0
----------------------------------------------------------------------------
archivos=3  LOC=310  metodos=11  decisiones=104
comparaciones con literal=49  campos static mutables=3  catch vacios=2
metodo mas largo: Main.main (107 lineas)   max parametros: GestorSolicitudes.registrar (6)
LOC promedio por archivo=103.3  decisiones por archivo=34.7
complejidad ciclomatica por metodo (McCabe aprox.): maxima=27 en Main.main   metodos con CC>10: 5 de 11
top 3 CC: Main.main=27, GestorSolicitudes.registrar=21, GestorSolicitudes.cambiarEstado=17
```

**Cómo leer las cifras:** *209 líneas* es el total de líneas del archivo (`wc -l`, con blancos y comentarios); *186 LOC* son líneas de código (sin blancos ni comentarios). La **complejidad ciclomática (CC) de McCabe** por método se aproxima como 1 + cantidad de `if`/`for`/`while`/`case`/`catch`/`&&`/`||`/`?` del método; el umbral habitual de revisión es CC > 10 (SonarQube, Unidad 3, usa el mismo concepto). En la versión inicial **5 de 11 métodos superan el umbral** y `Main.main` llega a CC 27.

## 3. Deuda técnica y code smells (13 hallazgos)

Prioridad = riesgo × costo de no corregirlo. **A** alta, **M** media, **B** baja.

| # | Smell / debilidad | Ubicación (evidencia) | Impacto | Riesgo de mantenimiento | Prio. | Tratamiento previsto |
|---|---|---|---|---|---|---|
| 1 | **God Class** — viola SRP: persistencia, validación, reglas, búsqueda, reporte e impresión en una sola clase | `GestorSolicitudes` (209 líneas, 10 métodos, 80 puntos de decisión) | Todo cambio toca la misma clase; no se puede probar una responsabilidad aislada | Alto: cualquier edición puede romper algo no relacionado (*fragilidad*) | **A** | Extract Class: `SolicitudService`, `ReporteService`, `CsvSolicitudRepository`, `ConsolaMenu` |
| 2 | **Long Method** | `Main.main` — 107 líneas, 24 decisiones, 5 opciones de menú en un solo bloque | Imposible de leer y de probar por partes | Alto | **A** | Extract Method (un método por caso de uso) |
| 3 | **Primitive Obsession / strings mágicos** para estado, tipo, prioridad y responsable | 49 comparaciones `equals("…")` en toda la base (43 en `GestorSolicitudes`) | Un error de tipeo (`"PENDIENTE "`) compila y falla en ejecución | Alto | **A** | Replace Type Code with Enum |
| 4 | **Errores como códigos numéricos** (`-1`, `-2`, `-3`, `-4`) | `registrar`, `cambiarEstado`, `asignarResponsable` y su interpretación en `Main` (líneas 35-38, 58-63, 78-80) | El significado del código está duplicado en dos clases; olvidar un caso falla en silencio | Alto | **A** | Replace Error Code with Exception |
| 5 | **Errores silenciados** (`catch (Exception e) {}`) | `GestorSolicitudes.cargar` (l.36) y `guardar` (l.51) | Si el archivo no se puede leer o escribir, el usuario cree que todo salió bien | Alto: pérdida silenciosa de datos | **A** | Excepciones propias + mensaje claro |
| 6 | **Estado global mutable** (`public static List db`, `static int sig`, `static String ARCH`) | `GestorSolicitudes` l.9-11 | Acoplamiento oculto; no hay forma de aislar pruebas ni de cambiar el archivo; viola DIP | Alto | **A** | Repositorio inyectado (DIP); eliminar `static` |
| 7 | **Código duplicado (viola DRY)** | Búsqueda por id copiada 3 veces (`cambiarEstado` l.96-100, `asignarResponsable` l.119-123, `buscarPorId` l.134-138); `trim().toUpperCase()` repetido en 7 lugares de `Main`; el parseo del ID repetido en las opciones 2 y 3 | Corregir un bug en un lugar deja los otros | Medio-alto | **M** | Extract Method (`buscarObligatorio`, `leerId`) |
| 8 | **Long Parameter List / Data Clump** | `registrar(legajo, nombre, email, tipo, descripcion, urgente)` — 6 parámetros; `buscar(estado, tipo, prioridad, responsable, legajo)` — 5; `legajo/nombre/email` viajan siempre juntos | Firmas frágiles (parámetros `String` intercambiables sin que el compilador avise) | Medio | **M** | Introduce Parameter Object (`NuevaSolicitud`, `Estudiante`, `FiltroSolicitudes`) |
| 9 | **Feature Envy + Data Class** (modelo anémico) | `calcularPlazo(Solicitud s)` y las reglas de prioridad solo leen datos de `Solicitud`; `Solicitud` es un contenedor de campos `public` sin comportamiento | Las reglas del dominio están lejos de sus datos; cualquiera puede dejar una solicitud en estado inconsistente | Medio | **M** | Move Method hacia `Solicitud`, `Estado`, `TipoSolicitud`; encapsular campos |
| 10 | **Cadenas `if/else` sobre códigos de tipo** (viola OCP) | Prioridad base por tipo (l.67-70), transiciones por estado (l.104-109), conteos del reporte (l.176-182) | Agregar un tipo/estado exige editar varios lugares | Medio-alto | **M** | Comportamiento en enums (`TipoSolicitud`, `Estado`) |
| 11 | **Lógica mezclada con presentación** | `imprimirReporte()` imprime con `System.out` dentro del gestor; `Main` consulta de nuevo la solicitud para armar mensajes | No se puede probar el reporte sin capturar la consola | Medio | **M** | `ReporteService` devuelve un objeto; `ConsolaMenu` formatea |
| 12 | **Nombres poco expresivos** | `db`, `sig`, `ARCH`, `p`, `l`, `n`, `e`, `t`, `d`, `u`, `r`, `ne`, `fe`, `ft`, `fp`, `fr`, `fl` | Hay que deducir el significado leyendo el código | Medio | **B** | Rename (Clean Code) |
| 13 | **Código muerto y comentado** | `exportarTxt` (nunca se invoca; `int x = 0;`, bucle comentado, `TODO`) | Ruido y falsa sensación de funcionalidad | Bajo | **B** | Eliminar |

### Criterio de priorización (cómo se obtuvo A / M / B)
**Puntaje = Impacto (1-3) × Probabilidad (1-3).** *Impacto:* 3 = puede perder o corromper datos, ocultar errores o bloquear cambios; 2 = encarece cada cambio; 1 = molestia de lectura.
*Probabilidad* de que cause problemas en el próximo cambio: 3 = en cada cambio; 2 = en cambios frecuentes; 1 = rara vez. **A ≥ 6 · M 3-5 · B ≤ 2.**

| # | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Impacto | 3 | 3 | 3 | 3 | 3 | 3 | 2 | 2 | 2 | 2 | 2 | 1 | 1 |
| Probabilidad | 3 | 3 | 3 | 2 | 2 | 2 | 2 | 2 | 2 | 2 | 2 | 2 | 1 |
| **Puntaje** | **9** | **9** | **9** | **6** | **6** | **6** | 4 | 4 | 4 | 4 | 4 | 2 | 1 |
| Prioridad | A | A | A | A | A | A | M | M | M | M | M | B | B |

### Defecto latente descubierto durante el diagnóstico (no cubierto por las pruebas)
El formato CSV usa `;` como separador sin escapar. Una descripción con `;` **corrompe el registro al recargarlo
en silencio** (consecuencia directa de los hallazgos 5 y 6). Reproducción sobre `estado-inicial`:

```
Descripcion: Nota; urgente para beca        # registrar
→ archivo: 1;12345;Ana Perez;ana@mail.com;CERTIFICADO;Nota; urgente para beca;BAJA;PENDIENTE;-;0
→ al reiniciar y listar:
  #1 | 12345 | Ana Perez | CERTIFICADO |  urgente para beca | BAJA | PENDIENTE   ← prioridad en lugar de estado, estado en lugar de responsable
```

Decisión: se **documenta y se corrige** en la versión refactorizada como mejora de manejo de errores (el repositorio
ahora rechaza o escapa el separador), manteniendo el contrato externo cubierto por los escenarios de referencia.
Ver `docs/03-refactorings.md`.

## 4. Priorización y plan

1. **Primero la red de seguridad:** escribir pruebas de caracterización (golden master) sobre el comportamiento visible,
   porque *refactorizar sin pruebas es reescribir a ciegas* (Unidad 1, regla fundamental de Fowler).
2. Atacar en orden: duplicación y métodos largos (#2, #7, #12, #13) → tipos como enum (#3, #10) → reglas junto a los datos (#9)
   → objetos de parámetros (#8) → excepciones (#4, #5) → separación de responsabilidades e inversión de dependencias (#1, #6, #11).
3. Un commit por refactoring, todos con la suite en verde.
