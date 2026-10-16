#!/usr/bin/env bash
# Compila y ejecuta todas las clases *Test con JUnit 4 (JUnitCore).
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/build.sh
CP="build/main:build/test:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"
CLASES=$(cd build/test && find . -name '*Test.class' | sed 's#^\./##; s#\.class$##; s#/#.#g' | sort)
if [ -z "$CLASES" ]; then echo "No hay tests"; exit 0; fi
java -cp "$CP" org.junit.runner.JUnitCore $CLASES
