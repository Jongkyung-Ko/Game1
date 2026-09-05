#!/usr/bin/env bash
# Idempotent Android SDK + medieval AVD setup.
set -euo pipefail

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
SDK="$ANDROID_SDK_ROOT"
CMDLINE="$SDK/cmdline-tools/latest/bin"

if [[ ! -x "$CMDLINE/sdkmanager" ]]; then
  echo "Android SDK cmdline-tools missing at $CMDLINE" >&2
  echo "Install the command-line tools into \$ANDROID_SDK_ROOT first." >&2
  exit 1
fi

yes | "$CMDLINE/sdkmanager" --licenses >/dev/null || true
"$CMDLINE/sdkmanager" \
  "platform-tools" \
  "emulator" \
  "platforms;android-34" \
  "system-images;android-34;google_apis;x86_64" \
  "build-tools;34.0.0"

mkdir -p "$HOME/.android/avd"
if [[ ! -f "$HOME/.android/avd/medieval.ini" ]]; then
  echo "no" | "$CMDLINE/avdmanager" create avd \
    --name medieval \
    --package "system-images;android-34;google_apis;x86_64" \
    --device pixel \
    --force
fi

# Nested KVM on some Cloud Agent kernels faults in vmx_vcpu_create.
# Keep a software-renderer GPU so the guest can boot without a host GPU.
CFG="$HOME/.android/avd/medieval.avd/config.ini"
if [[ -f "$CFG" ]]; then
  sed -i \
    -e 's/^hw.gpu.mode=.*/hw.gpu.mode=swiftshader_indirect/' \
    -e 's/^hw.ramSize=.*/hw.ramSize=2048/' \
    -e 's/^hw.cpu.ncore=.*/hw.cpu.ncore=2/' \
    "$CFG"
  grep -q '^hw.gpu.mode=' "$CFG" || echo 'hw.gpu.mode=swiftshader_indirect' >>"$CFG"
fi

echo "emulator-install: SDK=$SDK AVD=medieval"
