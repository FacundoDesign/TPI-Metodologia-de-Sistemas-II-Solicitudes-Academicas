#!/usr/bin/env bash
# Regenera los archivos .out (salida esperada) ejecutando la version ACTUAL del programa.
# Uso: SOLO sobre la version inicial (estado-inicial). Despues, los .out son el contrato de comportamiento externo.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT" && ./scripts/build.sh >/dev/null
for dir in src/test/resources/golden/*/; do
  tmp=$(mktemp -d)
  for in in $(ls "$dir"step*.in | sort); do
    (cd "$tmp" && java -cp "$ROOT/build/main" ar.edu.solicitudes.Main < "$ROOT/$in" 2>/dev/null) > "${in%.in}.out"
  done
  rm -rf "$tmp"
  echo "grabado: $dir"
done
