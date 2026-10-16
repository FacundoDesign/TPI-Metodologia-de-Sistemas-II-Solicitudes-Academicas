#!/usr/bin/env bash
# Compila codigo de produccion y de test sin necesitar Maven (solo JDK 17+ y los jars de lib/).
set -euo pipefail
cd "$(dirname "$0")/.."
CP="lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"
rm -rf build && mkdir -p build/main build/test
javac -encoding UTF-8 -d build/main $(find src/main/java -name '*.java')
if [ -n "$(find src/test/java -name '*.java' 2>/dev/null)" ]; then
  javac -encoding UTF-8 -cp "build/main:$CP" -d build/test $(find src/test/java -name '*.java')
fi
[ -d src/test/resources ] && cp -r src/test/resources/. build/test/ || true
echo "BUILD OK"
