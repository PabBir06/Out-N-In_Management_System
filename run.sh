#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p build
javac -encoding UTF-8 -d build src/*.java
case "${1:---demo}" in
  --test) java -ea -cp build ProjectTests ;;
  --demo) java -cp build MainDB --demo ;;
  --mysql) java -cp "build:.:lib/*" MainDB ;;
  *) echo 'Usage: ./run.sh [--demo|--mysql|--test]' >&2;exit 2 ;;
esac
