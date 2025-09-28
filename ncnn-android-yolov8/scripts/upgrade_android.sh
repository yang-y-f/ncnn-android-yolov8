#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> Ensuring AndroidX flags"
grep -q "android.useAndroidX=true" gradle.properties || echo "android.useAndroidX=true" >> gradle.properties
grep -q "android.enableJetifier=true" gradle.properties || echo "android.enableJetifier=true" >> gradle.properties

echo "==> Showing Gradle wrapper and AGP versions"
grep distributionUrl gradle/wrapper/gradle-wrapper.properties | sed 's/^/   /'
grep gradle build.gradle | sed 's/^/   /' || true

echo "==> Done. Open in Android Studio and Sync Project."
