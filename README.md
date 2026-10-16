# Gestión de Solicitudes Académicas (versión inicial)

Sistema de consola para registrar solicitudes de estudiantes (certificados, equivalencias, revisión de nota,
baja de materia), asignarles responsable, cambiar su estado, buscarlas y obtener un reporte.
Los datos se guardan en `solicitudes.csv` (datos sintéticos).

> Esta es la **versión inicial** (`estado-inicial`) del TPI de Metodología de Sistemas II: funciona,
> pero tiene problemas de diseño interno que se diagnostican y corrigen en las etapas siguientes.

## Uso
```bash
./scripts/build.sh                      # compila (solo requiere JDK 17+)
java -cp build/main ar.edu.solicitudes.Main
```
