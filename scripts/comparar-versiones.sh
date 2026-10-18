#!/usr/bin/env bash
# Evidencia antes/despues: compila el tag estado-inicial en una carpeta temporal, ejecuta las MISMAS entradas
# (src/test/resources/golden/*/step*.in) contra la version inicial y la actual, y compara salida y archivo de datos.
# Uso: ./scripts/comparar-versiones.sh [carpeta_de_evidencias]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${1:-$ROOT/docs/evidencias}"
TMP="$(mktemp -d)"
trap 'git -C "$ROOT" worktree remove --force "$TMP/inicial" >/dev/null 2>&1 || true; rm -rf "$TMP"' EXIT

git -C "$ROOT" worktree add -q "$TMP/inicial" estado-inicial
(cd "$TMP/inicial" && mkdir -p build && javac -encoding UTF-8 -d build $(find src/main/java -name '*.java'))
(cd "$ROOT" && ./scripts/build.sh >/dev/null)

mkdir -p "$OUT"
: > "$OUT/comparacion-antes-despues.txt"
total=0; iguales=0
for dir in "$ROOT"/src/test/resources/golden/*/; do
  nombre="$(basename "$dir")"
  w1="$TMP/w1-$nombre"; w2="$TMP/w2-$nombre"; mkdir -p "$w1" "$w2"
  ok=SI
  for entrada in $(ls "$dir"step*.in | sort); do
    (cd "$w1" && java -cp "$TMP/inicial/build" ar.edu.solicitudes.Main < "$entrada" > "$w1/salida.txt" 2>/dev/null)
    (cd "$w2" && java -cp "$ROOT/build/main" ar.edu.solicitudes.Main < "$entrada" > "$w2/salida.txt" 2>/dev/null)
    cmp -s "$w1/salida.txt" "$w2/salida.txt" || ok=NO
  done
  cmp -s "$w1/solicitudes.csv" "$w2/solicitudes.csv" 2>/dev/null || { [ ! -e "$w1/solicitudes.csv" ] && [ ! -e "$w2/solicitudes.csv" ]; } || ok=NO
  total=$((total+1)); [ "$ok" = SI ] && iguales=$((iguales+1))
  printf '%-32s salida y archivo de datos identicos: %s\n' "$nombre" "$ok" | tee -a "$OUT/comparacion-antes-despues.txt"
done
printf '\nEscenarios identicos: %d de %d\n' "$iguales" "$total" | tee -a "$OUT/comparacion-antes-despues.txt"
[ "$iguales" -eq "$total" ]
