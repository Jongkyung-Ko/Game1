#!/usr/bin/env bash
# Start the medieval AVD if needed, then wait until adb reports boot completed.
#
# Nested KVM on some Cloud Agent kernels hits:
#   kernel BUG at arch/x86/kvm/x86.c → kvm_spurious_fault
# In that case QEMU listens on 5554/5555 but the guest never creates a vCPU.
# Default acceleration is therefore "auto": use KVM only when it looks healthy.
#
# Env:
#   EMULATOR_ACCEL=auto|on|off   (default auto)
#   EMULATOR_WINDOW=1            show the emulator Qt window on $DISPLAY
#   EMULATOR_AVD                 AVD name (default medieval)
set -euo pipefail

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/platform-tools:$PATH"
export DISPLAY="${DISPLAY:-:1}"

AVD="${EMULATOR_AVD:-medieval}"
ADB="$ANDROID_SDK_ROOT/platform-tools/adb"
EMU="$ANDROID_SDK_ROOT/emulator/emulator"
LOG="${EMULATOR_LOG:-$HOME/emulator.log}"

if [[ ! -x "$EMU" || ! -x "$ADB" ]]; then
  echo "Android emulator/adb not found. Run scripts/emulator-install.sh first." >&2
  exit 1
fi

choose_accel() {
  case "${EMULATOR_ACCEL:-auto}" in
    on|off)
      echo "$EMULATOR_ACCEL"
      return
      ;;
  esac
  # A previous nested-KVM crash leaves this signature in dmesg.
  if dmesg 2>/dev/null | grep -qE 'kvm_spurious_fault|kernel BUG at arch/x86/kvm'; then
    echo off
    return
  fi
  if "$EMU" -accel-check 2>/dev/null | grep -qi 'usable'; then
    echo on
    return
  fi
  echo off
}

adb_serial() {
  # Prefer the emulator console device; ignore a duplicate tcp:5555 alias.
  local serial
  serial="$("$ADB" devices 2>/dev/null | awk '/^emulator-/{print $1; exit}')"
  if [[ -z "$serial" ]]; then
    serial="$("$ADB" devices 2>/dev/null | awk '/\tdevice$|\toffline$/{print $1; exit}')"
  fi
  echo "$serial"
}

boot_completed() {
  local serial
  serial="$(adb_serial)"
  [[ -n "$serial" ]] || return 1
  "$ADB" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | grep -q 1
}

"$ADB" start-server >/dev/null
if boot_completed; then
  echo "emulator-start: already booted"
  "$ADB" devices
  exit 0
fi

ACCEL="$(choose_accel)"
if ! pgrep -f "qemu-system-x86_64.*-avd ${AVD}|qemu-system-x86_64.*${AVD}" >/dev/null; then
  ARGS=(
    -avd "$AVD"
    -no-audio
    -no-boot-anim
    -gpu swiftshader_indirect
    -accel "$ACCEL"
    -no-snapshot-load
    -no-snapshot-save
    -memory 1536
    -cores 2
    -camera-back none
    -camera-front none
    -netdelay none
    -netspeed full
  )
  if [[ "${EMULATOR_WINDOW:-0}" != "1" ]]; then
    ARGS+=(-no-window)
  else
    if ! xdpyinfo -display "$DISPLAY" >/dev/null 2>&1; then
      echo "emulator-start: DISPLAY=$DISPLAY is not available (needed for EMULATOR_WINDOW=1)" >&2
      exit 1
    fi
  fi

  echo "emulator-start: launching AVD=$AVD accel=$ACCEL window=${EMULATOR_WINDOW:-0}"
  nohup "$EMU" "${ARGS[@]}" >"$LOG" 2>&1 &
  echo $! >"$HOME/emulator.pid"
else
  echo "emulator-start: qemu already running; waiting for boot (accel would be $ACCEL)"
fi

# Software (TCG) Android 14 boots are slow. KVM is faster when it works.
TIMEOUT_SEC=1800
if [[ "$ACCEL" == "on" ]]; then
  TIMEOUT_SEC=240
fi

"$ADB" wait-for-device || true
elapsed=0
while (( elapsed < TIMEOUT_SEC )); do
  if boot_completed; then
    echo "emulator-start: boot complete after ${elapsed}s (accel=$ACCEL)"
    "$ADB" devices
    exit 0
  fi
  # Detect the "ports open, guest dead" nested-KVM failure early.
  if [[ "$ACCEL" == "on" ]] && (( elapsed >= 45 )); then
    if "$ADB" devices 2>/dev/null | grep -q offline; then
      echo "emulator-start: KVM guest stayed offline; nested KVM is likely broken." >&2
      echo "emulator-start: kill this emulator and rerun with EMULATOR_ACCEL=off" >&2
      exit 2
    fi
  fi
  sleep 5
  elapsed=$((elapsed + 5))
  if (( elapsed % 60 == 0 )); then
    echo "emulator-start: still waiting (${elapsed}s / ${TIMEOUT_SEC}s)"
  fi
done

echo "emulator-start: timed out after ${TIMEOUT_SEC}s. See $LOG" >&2
tail -n 40 "$LOG" >&2 || true
"$ADB" devices >&2 || true
exit 1
