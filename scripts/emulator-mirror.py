#!/usr/bin/env python3
"""Show the emulator screen on $DISPLAY and forward clicks/keys via adb.

scrcpy needs a hardware H.264 encoder. Software-accelerated AVDs often lack one,
so this fallback polls `adb exec-out screencap` instead.
"""
from __future__ import annotations

import io
import os
import shutil
import subprocess
import sys
import threading
import time
import tkinter as tk

from PIL import Image, ImageTk

SDK = os.environ.get("ANDROID_SDK_ROOT", os.path.expanduser("~/android-sdk"))
ADB = os.path.join(SDK, "platform-tools", "adb")
if not os.path.isfile(ADB):
    ADB = shutil.which("adb") or "adb"


def adb_serial() -> str:
    out = subprocess.check_output([ADB, "devices"], text=True)
    for line in out.splitlines():
        if line.startswith("emulator-") and "\tdevice" in line:
            return line.split()[0]
    for line in out.splitlines():
        if "\tdevice" in line:
            return line.split()[0]
    raise SystemExit("emulator-mirror: no adb device")


def adb(serial: str, *args: str, check: bool = False) -> subprocess.CompletedProcess:
    return subprocess.run([ADB, "-s", serial, *args], check=check, capture_output=True)


class Mirror:
    def __init__(self) -> None:
        self.serial = adb_serial()
        self.root = tk.Tk()
        self.root.title("Medieval Village emulator")
        self.root.configure(bg="#111")
        self.label = tk.Label(self.root, bg="#111")
        self.label.pack()
        self.photo: ImageTk.PhotoImage | None = None
        self.frame_w = 720
        self.frame_h = 1280
        self.lock = threading.Lock()
        self.pending: Image.Image | None = None
        self.alive = True
        self.label.bind("<Button-1>", self.on_click)
        self.root.bind("<Escape>", lambda _e: adb(self.serial, "shell", "input", "keyevent", "4"))
        self.root.bind("<BackSpace>", lambda _e: adb(self.serial, "shell", "input", "keyevent", "4"))
        self.root.protocol("WM_DELETE_WINDOW", self.close)
        threading.Thread(target=self.poll, daemon=True).start()
        self.root.after(200, self.paint)

    def poll(self) -> None:
        while self.alive:
            proc = subprocess.run(
                [ADB, "-s", self.serial, "exec-out", "screencap", "-p"],
                capture_output=True,
            )
            if proc.returncode == 0 and proc.stdout.startswith(b"\x89PNG"):
                try:
                    img = Image.open(io.BytesIO(proc.stdout)).convert("RGB")
                    with self.lock:
                        self.pending = img
                except Exception:
                    pass
            time.sleep(1.2)

    def paint(self) -> None:
        img = None
        with self.lock:
            if self.pending is not None:
                img = self.pending
                self.pending = None
        if img is not None:
            self.frame_w, self.frame_h = img.size
            max_h = max(400, self.root.winfo_screenheight() - 80)
            scale = min(1.0, max_h / self.frame_h)
            show = img if scale >= 0.99 else img.resize(
                (int(self.frame_w * scale), int(self.frame_h * scale)),
                Image.Resampling.BILINEAR,
            )
            self.photo = ImageTk.PhotoImage(show)
            self.label.configure(image=self.photo)
        if self.alive:
            self.root.after(200, self.paint)

    def on_click(self, event: tk.Event) -> None:
        w = max(1, self.label.winfo_width())
        h = max(1, self.label.winfo_height())
        x = int(event.x * self.frame_w / w)
        y = int(event.y * self.frame_h / h)
        adb(self.serial, "shell", "input", "tap", str(x), str(y))

    def close(self) -> None:
        self.alive = False
        self.root.destroy()

    def run(self) -> None:
        self.root.mainloop()


if __name__ == "__main__":
    if not os.environ.get("DISPLAY"):
        os.environ["DISPLAY"] = ":1"
    Mirror().run()
    sys.exit(0)
