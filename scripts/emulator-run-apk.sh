#!/usr/bin/env bash
# Build (if needed), install, and launch the medieval village debug APK.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

"$ROOT/scripts/emulator-start.sh"

APK="${1:-$ROOT/app/build/outputs/apk/debug/app-debug.apk}"
if [[ ! -f "$APK" ]]; then
  echo "emulator-run-apk: building debug APK"
  (cd "$ROOT" && ./gradlew :app:assembleDebug)
fi

ADB="$ANDROID_SDK_ROOT/platform-tools/adb"
AAPT="$(ls -1 "$ANDROID_SDK_ROOT"/build-tools/*/aapt 2>/dev/null | tail -1 || true)"
SERIAL="$("$ADB" devices 2>/dev/null | awk '/^emulator-/{print $1; exit}')"
if [[ -z "$SERIAL" ]]; then
  SERIAL="$("$ADB" devices 2>/dev/null | awk '/\tdevice$/{print $1; exit}')"
fi
if [[ -z "$SERIAL" ]]; then
  echo "emulator-run-apk: no adb device" >&2
  exit 1
fi

"$ADB" -s "$SERIAL" install -r -t "$APK"

PKG=""
if [[ -n "$AAPT" ]]; then
  PKG="$("$AAPT" dump badging "$APK" 2>/dev/null | sed -n "s/package: name='\([^']*\)'.*/\1/p" | head -1)"
fi
if [[ -z "$PKG" ]]; then
  PKG="$(grep -oP 'applicationId\s*=\s*"\K[^"]+' "$ROOT/app/build.gradle.kts" | head -1)"
fi

"$ADB" -s "$SERIAL" shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null
echo "emulator-run-apk: launched $PKG from $APK on $SERIAL"
echo "emulator-run-apk: optional mirror: scripts/emulator-show.sh"
