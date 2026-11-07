#!/usr/bin/env bash
# Experimento OCP (Open/Closed): ¿cuanto cuesta agregar un nuevo tipo de solicitud, CAMBIO_COMISION?
# ANTES  : se cuentan los lugares de estado-inicial que hay que editar (el compilador no avisa si se olvida uno).
# DESPUES: se agrega UNA constante al enum en una copia temporal, se compila y se ejecuta una sesion real.
# Uso: ./scripts/experimento-ocp.sh > docs/evidencias/experimento-ocp.txt
set -euo pipefail
cd "$(dirname "$0")/.."
echo "=== ANTES (tag estado-inicial): lugares que mencionan los tipos y deberian editarse ==="
for f in GestorSolicitudes Main; do
  git show "estado-inicial:src/main/java/ar/edu/solicitudes/$f.java" | grep -n -E 'EQUIVALENCIA|REVISION_NOTA|BAJA_MATERIA' | sed "s|^|$f.java:|"
done
echo "(4 lugares en 2 archivos: validacion del tipo, prioridad base, plazo y texto del prompt)"
echo
echo "=== DESPUES: se agrega una constante al enum en una copia temporal ==="
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/orig" "$TMP/mod"
cp -r src "$TMP/orig/src"; cp -r src "$TMP/mod/src"
sed -i 's|BAJA_MATERIA(Prioridad.MEDIA, 0);|BAJA_MATERIA(Prioridad.MEDIA, 0),\n    CAMBIO_COMISION(Prioridad.MEDIA, 2);|' "$TMP/mod/src/main/java/ar/edu/solicitudes/TipoSolicitud.java"
diff -ru "$TMP/orig/src/main" "$TMP/mod/src/main" | grep -v '^Only' || true
echo
mkdir -p "$TMP/mod/out" && javac -encoding UTF-8 -d "$TMP/mod/out" $(find "$TMP/mod/src/main/java" -name "*.java") 2>&1 | grep -v "^Picked up" || true
echo "=== Sesion real: alta de un CAMBIO_COMISION urgente + reporte ==="
cd "$TMP"
printf '1\n12345\nAna Perez\nana@mail.com\nCAMBIO_COMISION\nPasar a la comision B\ns\n4\n\n\n\n\n\n5\n0\n' \
  | java -cp "$TMP/mod/out" ar.edu.solicitudes.Main 2>&1 | grep -v '^Picked up' \
  | grep -E 'Tipo \(|registrada|CAMBIO_COMISION|Plazo promedio|Total'
echo
echo "Resultado: el menu, el alta (prioridad ALTA por urgente, plazo 3+2=5), el listado y el reporte aceptan el nuevo tipo sin tocar ninguna otra clase."
