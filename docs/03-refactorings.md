# Refactorings aplicados (antes / después)

Regla de oro de la materia (Fowler, Unidad 1): **refactorizar con pruebas**. Antes del primer refactoring se escribió una
red de seguridad (`1e51dd8`, tag `etapa-pruebas-base`): 6 escenarios de referencia (*golden master*) que ejecutan el programa
como lo haría una persona y comparan la salida con la de la versión inicial. **Cada refactoring es un commit y se hizo con la suite
completa en verde.** Se aplicaron **7** (el mínimo pedido es 4): los 6 primeros en la primera pasada y el 7.º tras la segunda ronda de code review.

| # | Técnica | Smell que ataca | Commit |
|---|---|---|---|
| 1 | **Extract Method** (+ Rename, eliminación de duplicación y código muerto) | #2 Long Method, #7 Duplicated Code, #12 nombres, #13 código muerto | `4e969b2` |
| 2 | **Replace Type Code with Enum** | #3 Primitive Obsession | `dc5e2f0` |
| 3 | **Move Method** | #9 Feature Envy / Data Class, #10 cadenas `if/else` | `fca39db` |
| 4 | **Introduce Parameter Object** | #8 Long Parameter List / Data Clump | `2ab801e` |
| 5 | **Replace Error Code with Exception** | #4 códigos de error, #5 errores silenciados | `438f472` |
| 6 | **Extract Class** + inversión de dependencias | #1 God Class, #6 estado global, #11 lógica mezclada con presentación | `0c11528` |
| 7 | **Introduce Parameter Object** (`Seguimiento`) | Long Parameter List residual (R-10 del code review): `Solicitud.reconstruir` seguía con 5 parámetros | `87fdc4c` |

## 1. Extract Method — `4e969b2`
`Main.main` (107 líneas, 24 decisiones) pasa a delegar cada opción del menú en un método con nombre propio.

```java
// ANTES (estado-inicial): todo dentro de main
if (op.equals("1")) {
    System.out.print("Legajo: ");
    String l = sc.nextLine().trim();
    ... 25 lineas mas de lectura, llamada y mensajes ...
} else if (op.equals("2")) { ... }

// DESPUES
private static boolean ejecutarOpcion(String opcion, Scanner teclado) {
    if (opcion.equals("1")) { registrarSolicitud(teclado); }
    else if (opcion.equals("2")) { cambiarEstado(teclado); }
    ...
}
```
También se extrajeron `leerTexto`, `leerTextoEnMayusculas` y `leerId`, y las tres copias de la búsqueda por id se unificaron en `buscarPorId` (más tarde, `obtener(id)` en el servicio).

## 2. Replace Type Code with Enum — `dc5e2f0`
```java
// ANTES: un error de tipeo compila y falla en ejecucion
if (s.estado.equals("PENDIENTE")) { ... } else if (s.estado.equals("EN_PROCESO")) { ... }

// DESPUES: el compilador valida los valores
public enum Estado { PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA; ... }
```
Las 49 comparaciones con literales bajaron a 1 (el indicador `"1"` del CSV).

## 3. Move Method — `fca39db`
Las reglas se mudan junto a los datos que usan.
```java
// ANTES: GestorSolicitudes.calcularPlazo(Solicitud s) leia solo campos de s (Feature Envy)
// DESPUES: Solicitud.calcularPlazo()
public int calcularPlazo() { return prioridad.plazoBaseDias() + tipo.plazoExtraDias(); }
```
La prioridad base por tipo pasa a `TipoSolicitud`; las transiciones permitidas a `Estado.puedePasarA`; la coincidencia con los filtros a `FiltroSolicitudes.acepta`.

## 4. Introduce Parameter Object — `2ab801e`
```java
// ANTES: 6 parametros String/boolean intercambiables
registrar(String legajo, String nombre, String email, String tipo, String descripcion, boolean urgente)
buscar(String estado, String tipo, String prioridad, String responsable, String legajo)

// DESPUES
registrar(NuevaSolicitud datos)          // record NuevaSolicitud(Estudiante, tipo, descripcion, urgente)
buscar(FiltroSolicitudes filtro)         // record FiltroSolicitudes(estado, tipo, prioridad, responsable, legajo)
```
`legajo/nombre/email` viajaban siempre juntos (*Data Clump*): ahora son `Estudiante`, un *value object* que se valida a sí mismo.

## 5. Replace Error Code with Exception — `438f472`
```java
// ANTES
int r = GestorSolicitudes.cambiarEstado(id, ne);
if (r == 0) ...; else if (r == -1) ...; else if (r == -2) ...; else if (r == -3) ...; else if (r == -4) ...

// DESPUES
try { ejecutarOpcion(opcion); }
catch (SolicitudException | ArchivoDatosException e) { salida.println("Error: " + e.getMessage()); }
```
Los `catch (Exception e) {}` vacíos desaparecen: un archivo dañado ahora se informa con archivo y línea (mejora deliberada, cubierta por `ArchivoDatosCorruptoTest`).

## 6. Extract Class + DIP — `0c11528`
`GestorSolicitudes` (209 líneas, 10 métodos, 80 decisiones, 3 campos `static` mutables) desaparece:

```
Main ──crea──▶ SolicitudService ──usa──▶ «interface» SolicitudRepository ◀──implementa── CsvSolicitudRepository
  └─crea──▶ ConsolaMenu ──usa──▶ SolicitudService, ReporteSolicitudes
Solicitud · Estudiante · NuevaSolicitud · FiltroSolicitudes · Estado · Prioridad · TipoSolicitud · Responsable   (dominio)
```

## 7. Introduce Parameter Object (`Seguimiento`) — `87fdc4c`
El apunte define *Long Parameter List* como 5 o más parámetros. Tras el refactoring 4 todavía quedaba `Solicitud.reconstruir(id, datos, prioridad, estado, responsable)` con 5 (y su constructor privado).
`estado` y `responsable` cambian siempre juntos (*Data Clump*), así que se agrupan:
```java
// ANTES
Solicitud.reconstruir(int id, NuevaSolicitud datos, Prioridad prioridad, Estado estado, Responsable responsable)
// DESPUES
Solicitud.reconstruir(int id, NuevaSolicitud datos, Prioridad prioridad, Seguimiento seguimiento)   // record Seguimiento(Estado, Responsable)
```
Verificación: 92 pruebas y comparación 6/6 sin cambios; máximo de parámetros del proyecto = 4.

## Cambios deliberados que NO son refactorings (se registran aparte)
Regla (ADR-9): un refactoring no cambia el comportamiento externo. Solo se hicieron cambios de comportamiento cuando el comportamiento previo era **un defecto que corrompía o perdía datos, o que ocultaba errores**; nunca para "mejorar" una regla de negocio discutible.

| Cambio | Por qué es un defecto y no una regla | Prueba y evidencia | Commit |
|---|---|---|---|
| Escapar `;` en el CSV | corrompía el registro al recargar, en silencio | `unPuntoYComaEnUnTextoNoCorrompeElRegistro`, `evidencias/defecto-latente-separador.txt` | `0c11528` |
| Archivo dañado: avisar (archivo y línea) y terminar con código 1 | seguía con datos incompletos sin avisar | `ArchivoDatosCorruptoTest` | `438f472` |
| Revertir la memoria si no se pudo guardar (R-4) | memoria y disco quedaban distintos tras un error de disco | 4 pruebas con *stub* (en rojo antes de corregir: `evidencias/r4-fase-roja.txt`) | `39ea3e0` |

**No se tocaron** (reglas discutibles, ADR-6): validación débil del email y el orden de validación legajo → nombre → email → tipo.

## Atomicidad de los commits (limitación reconocida)
Fowler recomienda pasos pequeños. Los commits `4e969b2` (1) y `0c11528` (6) agrupan varias transformaciones relacionadas:
- (1) combina Extract Method, Rename y la eliminación de duplicación/código muerto; son pasos mecánicos sobre las mismas líneas de `Main`/`GestorSolicitudes`.
- (6) combina Extract Class (servicio, repositorio, reporte, consola) con la inversión de dependencias: eliminar el estado `static` y la clase `GestorSolicitudes` obliga a cambiar a la vez todos sus llamadores; un paso intermedio compilable habría requerido adaptadores temporales.
Con más tiempo se dividiría en: (a) extraer `CsvSolicitudRepository`, (b) extraer `ReporteSolicitudes`, (c) extraer `SolicitudService`, (d) inyectar el repositorio. **Ambos commits se verificaron con la suite completa y la comparación 6/6**, y los refactorings 2 a 5 y 7 son atómicos (una técnica por commit).

## Verificación del comportamiento externo
- `./scripts/comparar-versiones.sh` compila el tag `estado-inicial`, ejecuta las **mismas entradas** contra la versión inicial y la final y compara
  la salida y el archivo `solicitudes.csv` byte a byte → **6 de 6 escenarios idénticos** (`docs/evidencias/comparacion-antes-despues.txt`).
- Única diferencia visible intencional: el archivo escapa el `;` dentro de un texto (corrige el defecto de `docs/evidencias/defecto-latente-separador.txt`)
  y un archivo de datos dañado se informa en lugar de ignorarse.

## Resultado en números
Fuente: `docs/evidencias/metricas-antes.txt` y `metricas-despues.txt` (`scripts/metricas.py`, definiciones en la cabecera del script).

| Métrica | Antes | Después |
|---|---|---|
| Archivos / LOC de producción | 3 / 310 | 18 / 585 |
| LOC promedio por archivo | 103,3 | 32,5 |
| Puntos de decisión por archivo (promedio) | 34,7 | 3,9 |
| **Complejidad ciclomática máxima por método (McCabe aprox.)** | **27** (`Main.main`) | **7** (`ConsolaMenu.ejecutarOpcion`) |
| **Métodos con CC > 10** | **5 de 11** | **0 de 74** |
| Método más largo | `Main.main`: 107 líneas | `parsearLinea`: 21 líneas |
| Máximo de parámetros | 6 | 4 |
| Comparaciones contra literales | 49 | 1 |
| Campos `static` mutables | 3 | 0 |
| `catch` vacíos | 2 | 0 |
| Pruebas automáticas | 0 | 92 |
| Puntaje de mutación (14 mutantes manuales) | — | 14 de 14 matados |

> El código total **creció** (más clases y declaraciones): es esperable y no es el objetivo. Lo que baja es el costo y el riesgo de cambio por clase y por método. El promedio por archivo mejora en parte porque hay más archivos; por eso se reporta también la CC **por método**, que no depende de cuántos archivos haya.
