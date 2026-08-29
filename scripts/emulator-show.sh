#!/usr/bin/env bash
# Mirror the running emulator onto $DISPLAY (TigerVNC :1 in Cloud Agent VMs).
set -euo pipefail

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"
export DISPLAY="${DISPLAY:-:1}"

ADB="$ANDROID_SDK_ROOT/platform-tools/adb"

SERIAL="$("$ADB" devices 2>/dev/null | awk '/^emulator-/{print $1; exit}')"
if [[ -z "$SERIAL" ]]; then
  SERIAL="$("$ADB" devices 2>/dev/null | awk '/\tdevice$/{print $1; exit}')"
fi
if [[ -z "$SERIAL" ]] || ! "$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | grep -q 1; then
  echo "emulator-show: emulator is not booted. Run scripts/emulator-start.sh first." >&2
  exit 1
fi

if ! xdpyinfo -display "$DISPLAY" >/dev/null 2>&1; then
  echo "emulator-show: DISPLAY=$DISPLAY is not available" >&2
  exit 1
fi

if ! command -v scrcpy >/dev/null 2>&1; then
  echo "emulator-show: installing scrcpy"
  sudo apt-get update -qq
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq scrcpy
fi

exec scrcpy -s "$SERIAL" --stay-awake --turn-screen-on --window-title "Medieval Village emulator"
