#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ ! -f target/meteor-eyewear-1.0.0.jar ]; then
  sh ./mvnw --batch-mode --no-transfer-progress package
fi
printf '\nMeteor Eyewear local demo: http://localhost:8080\n'
exec java -jar target/meteor-eyewear-1.0.0.jar
