#!/usr/bin/env python3
"""Painterly 1536x1024 maps for Igloo / Seaside / Winter settlements."""
from __future__ import annotations

import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

W, H = 1536, 1024
OUT = Path("app/src/main/assets/custom")


def lerp(a, b, t):
    return int(a + (b - a) * t)


def lerp_color(c1, c2, t):
    return tuple(lerp(a, b, t) for a, b in zip(c1, c2))


def vertical_sky(draw, top, bottom):
    for y in range(H):
        t = y / (H - 1)
        draw.line([(0, y), (W, y)], fill=lerp_color(top, bottom, t))


def blob(draw, x, y, rx, ry, color, jitter=8):
    pts = []
    for i in range(10):
        ang = i / 10 * math.tau
        j = random.uniform(-jitter, jitter)
        pts.append((x + math.cos(ang) * (rx + j), y + math.sin(ang) * (ry + j)))
    draw.polygon(pts, fill=color)


def house(draw, cx, cy, w, h, roof, wall, snow=False):
    left, top = cx - w / 2, cy - h / 2
    draw.rectangle([left, top + h * 0.32, left + w, top + h], fill=wall)
    roof_pts = [
        (left - 8, top + h * 0.36),
        (cx, top - 8),
        (left + w + 8, top + h * 0.36),
    ]
    draw.polygon(roof_pts, fill=roof)
    # door
    dw, dh = w * 0.18, h * 0.28
    draw.rectangle([cx - dw / 2, top + h - dh, cx + dw / 2, top + h], fill=(40, 28, 18))
    if snow:
        draw.polygon(
            [(left - 8, top + h * 0.36), (cx, top - 8), (left + w + 8, top + h * 0.36),
             (left + w + 4, top + h * 0.42), (cx, top + 10), (left - 4, top + h * 0.42)],
            fill=(240, 246, 255),
        )


def igloo(draw, cx, cy, r, tint=(220, 232, 245)):
    draw.ellipse([cx - r, cy - r * 0.72, cx + r, cy + r * 0.55], fill=tint)
    draw.ellipse([cx - r * 0.28, cy - r * 0.05, cx + r * 0.28, cy + r * 0.42], fill=(40, 50, 62))
    draw.arc([cx - r, cy - r * 0.72, cx + r, cy + r * 0.55], 200, 340, fill=(255, 255, 255), width=4)


def church(draw, cx, cy, w, h, stone, roof, frozen=False):
    left, top = cx - w / 2, cy - h / 2
    draw.rectangle([left, top + h * 0.28, left + w, top + h], fill=stone)
    draw.polygon([(left - 6, top + h * 0.32), (cx, top), (left + w + 6, top + h * 0.32)], fill=roof)
    # tower
    tw = w * 0.28
    draw.rectangle([cx - tw / 2, top - h * 0.18, cx + tw / 2, top + h * 0.28], fill=stone)
    draw.polygon(
        [(cx - tw / 2 - 6, top - h * 0.14), (cx, top - h * 0.42), (cx + tw / 2 + 6, top - h * 0.14)],
        fill=roof,
    )
    if frozen:
        draw.rectangle([left, top + h * 0.7, left + w, top + h], fill=(180, 210, 230))


def cave(draw, cx, cy, w, h, dark=(28, 24, 22), glow=(90, 60, 140)):
    left, top = cx - w / 2, cy - h / 2
    draw.ellipse([left, top, left + w, top + h], fill=dark)
    draw.ellipse([cx - w * 0.22, cy - h * 0.08, cx + w * 0.22, cy + h * 0.42], fill=glow)


def path_stroke(draw, pts, color, width=28):
    draw.line(pts, fill=color, width=width, joint="curve")


def save(img: Image.Image, name: str):
    img = img.filter(ImageFilter.SMOOTH_MORE)
    dest = OUT / name
    img.save(dest, "PNG", optimize=True)
    print(f"wrote {dest} {img.size} {dest.stat().st_size}")


def igloo_frozen():
    random.seed(11)
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    vertical_sky(d, (120, 150, 190), (210, 226, 240))
    # snow ground
    d.rectangle([0, 420, W, H], fill=(226, 236, 246))
    for _ in range(40):
        blob(d, random.randint(0, W), random.randint(430, H), random.randint(40, 120),
             random.randint(16, 40), (240, 246, 255), 18)
    # ice star in the north sky
    d.ellipse([980, 40, 1120, 180], fill=(200, 230, 255))
    d.ellipse([1000, 60, 1100, 160], fill=(230, 244, 255))
    path_stroke(d, [(420, 900), (640, 640), (1080, 460), (1180, 380)], (200, 214, 226), 34)
    igloo(d, 420, 780, 92)
    church(d, 300, 300, 180, 170, (170, 186, 200), (140, 160, 180), frozen=True)
    cave(d, 1080, 340, 260, 200, (40, 70, 95), (90, 170, 210))
    house(d, 720, 560, 160, 140, (90, 110, 130), (130, 140, 150), snow=True)
    # drifting snow
    for _ in range(80):
        x, y = random.randint(0, W), random.randint(0, H)
        d.ellipse([x, y, x + 3, y + 3], fill=(255, 255, 255))
    save(img, "igloo_frozen.png")


def igloo_thawed():
    random.seed(12)
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    vertical_sky(d, (118, 176, 220), (210, 230, 180))
    d.rectangle([0, 430, W, H], fill=(92, 140, 78))
    for _ in range(30):
        blob(d, random.randint(0, W), random.randint(450, H), random.randint(50, 130),
             random.randint(18, 36), (110, 158, 88), 16)
    path_stroke(d, [(780, 900), (780, 540), (480, 380), (320, 540)], (196, 168, 110), 36)
    path_stroke(d, [(780, 540), (1120, 500), (1280, 720)], (196, 168, 110), 30)
    house(d, 780, 780, 240, 180, (156, 74, 52), (232, 212, 172))
    church(d, 480, 300, 200, 190, (200, 198, 188), (90, 70, 80))
    house(d, 320, 520, 220, 180, (113, 59, 42), (208, 166, 110))
    house(d, 520, 560, 130, 100, (138, 90, 43), (227, 207, 164))
    house(d, 780, 540, 160, 120, (180, 87, 63), (235, 217, 180))
    house(d, 980, 520, 130, 110, (107, 58, 46), (216, 196, 155))
    house(d, 1120, 480, 160, 140, (90, 65, 50), (155, 130, 102))
    house(d, 300, 720, 160, 130, (176, 182, 196), (242, 240, 230))
    house(d, 1100, 780, 170, 130, (122, 82, 48), (201, 168, 124))
    house(d, 1280, 700, 170, 130, (78, 90, 58), (139, 150, 104))
    house(d, 980, 280, 140, 150, (75, 59, 143), (207, 199, 232))
    save(img, "igloo_thawed.png")


def seaside_ruins():
    random.seed(21)
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    vertical_sky(d, (70, 90, 110), (40, 70, 90))
    # sea
    d.rectangle([0, 360, W, H], fill=(28, 70, 92))
    for y in range(360, H, 10):
        shade = 28 + (y - 360) // 8
        d.line([(0, y), (W, y)], fill=(shade, 80 + shade // 4, 100 + shade // 3))
    # wrecked land patches
    blob(d, 760, 820, 220, 90, (70, 78, 70), 20)
    blob(d, 380, 360, 180, 80, (80, 84, 78), 16)
    blob(d, 920, 640, 160, 70, (74, 80, 72), 14)
    blob(d, 1180, 500, 200, 90, (60, 72, 68), 18)
    path_stroke(d, [(760, 900), (920, 680), (1180, 540)], (50, 80, 88), 26)
    house(d, 760, 820, 200, 150, (70, 80, 90), (120, 140, 150), snow=False)
    church(d, 380, 340, 180, 170, (140, 148, 150), (70, 80, 90), frozen=True)
    cave(d, 1180, 460, 260, 210, (18, 30, 40), (30, 90, 110))
    house(d, 920, 640, 160, 140, (70, 60, 50), (90, 80, 70))
    # wreckage
    for _ in range(18):
        x, y = random.randint(80, W - 80), random.randint(500, H - 40)
        d.rectangle([x, y, x + random.randint(20, 70), y + 8], fill=(90, 70, 50))
    save(img, "seaside_ruins.png")


def seaside_restored():
    random.seed(22)
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    vertical_sky(d, (90, 160, 210), (230, 210, 160))
    d.rectangle([0, 620, W, H], fill=(40, 110, 150))
    d.rectangle([0, 430, W, 640], fill=(210, 186, 130))
    d.rectangle([0, 500, W, 560], fill=(70, 140, 170))  # tide line
    path_stroke(d, [(780, 900), (780, 540), (480, 380), (320, 540)], (196, 168, 110), 36)
    path_stroke(d, [(780, 540), (1120, 500), (1280, 720)], (196, 168, 110), 30)
    # docks
    d.rectangle([180, 600, 520, 640], fill=(120, 86, 52))
    for x in range(200, 500, 40):
        d.rectangle([x, 640, x + 10, 700], fill=(90, 64, 40))
    house(d, 780, 780, 240, 180, (156, 74, 52), (232, 212, 172))
    church(d, 480, 300, 200, 190, (200, 198, 188), (50, 90, 130))
    house(d, 320, 520, 220, 180, (113, 59, 42), (208, 166, 110))
    house(d, 520, 560, 130, 100, (138, 90, 43), (227, 207, 164))
    house(d, 780, 540, 160, 120, (180, 87, 63), (235, 217, 180))
    house(d, 980, 520, 130, 110, (107, 58, 46), (216, 196, 155))
    house(d, 1120, 480, 160, 140, (90, 65, 50), (155, 130, 102))
    house(d, 300, 720, 160, 130, (176, 182, 196), (242, 240, 230))
    house(d, 1100, 780, 170, 130, (122, 82, 48), (201, 168, 124))
    house(d, 1280, 700, 170, 130, (78, 90, 58), (139, 150, 104))
    house(d, 980, 280, 140, 150, (75, 59, 143), (207, 199, 232))
    save(img, "seaside_restored.png")


def winter_cursed():
    random.seed(31)
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    vertical_sky(d, (40, 48, 70), (150, 166, 186))
    d.rectangle([0, 380, W, H], fill=(190, 204, 220))
    for _ in range(28):
        blob(d, random.randint(0, W), random.randint(400, H), random.randint(40, 140),
             random.randint(14, 36), (220, 230, 240), 16)
    # keep mass
    d.rectangle([620, 220, 940, 560], fill=(90, 96, 108))
    d.polygon([(600, 250), (780, 120), (960, 250)], fill=(70, 76, 88))
    d.rectangle([640, 180, 720, 260], fill=(80, 86, 98))
    d.rectangle([840, 160, 920, 260], fill=(80, 86, 98))
    path_stroke(d, [(760, 960), (760, 700), (780, 540)], (170, 180, 190), 36)
    house(d, 760, 880, 200, 140, (78, 90, 58), (139, 150, 104), snow=True)
    church(d, 420, 320, 180, 170, (160, 168, 176), (90, 80, 90), frozen=True)
    cave(d, 780, 420, 280, 240, (28, 26, 30), (90, 50, 130))
    house(d, 1100, 560, 160, 140, (90, 65, 50), (155, 130, 102), snow=True)
    for _ in range(70):
        x, y = random.randint(0, W), random.randint(0, H)
        d.ellipse([x, y, x + 2, y + 2], fill=(255, 255, 255))
    save(img, "winter_cursed.png")


def winter_restored():
    random.seed(32)
    img = Image.new("RGB", (W, H))
    d = ImageDraw.Draw(img)
    vertical_sky(d, (150, 190, 230), (255, 236, 200))
    d.rectangle([0, 430, W, H], fill=(110, 150, 86))
    for _ in range(24):
        blob(d, random.randint(0, W), random.randint(450, H), random.randint(50, 130),
             random.randint(16, 32), (130, 168, 96), 14)
    # bright keep
    d.rectangle([620, 200, 940, 500], fill=(230, 228, 220))
    d.polygon([(600, 230), (780, 90), (960, 230)], fill=(240, 236, 220))
    d.rectangle([640, 150, 720, 240], fill=(220, 218, 210))
    d.rectangle([840, 130, 920, 240], fill=(220, 218, 210))
    path_stroke(d, [(780, 900), (780, 540), (480, 380), (320, 540)], (210, 186, 130), 36)
    path_stroke(d, [(780, 540), (1120, 500), (1280, 720)], (210, 186, 130), 30)
    house(d, 780, 780, 240, 180, (156, 74, 52), (232, 212, 172))
    church(d, 480, 300, 200, 190, (220, 218, 210), (180, 70, 70))
    house(d, 320, 520, 220, 180, (113, 59, 42), (208, 166, 110))
    house(d, 520, 560, 130, 100, (138, 90, 43), (227, 207, 164))
    house(d, 780, 540, 160, 120, (180, 87, 63), (235, 217, 180))
    house(d, 980, 520, 130, 110, (107, 58, 46), (216, 196, 155))
    house(d, 1120, 480, 160, 140, (90, 65, 50), (155, 130, 102))
    house(d, 300, 720, 160, 130, (176, 182, 196), (242, 240, 230))
    house(d, 1100, 780, 170, 130, (122, 82, 48), (201, 168, 124))
    house(d, 1280, 700, 170, 130, (78, 90, 58), (139, 150, 104))
    house(d, 980, 280, 140, 150, (75, 59, 143), (207, 199, 232))
    save(img, "winter_restored.png")


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    igloo_frozen()
    igloo_thawed()
    seaside_ruins()
    seaside_restored()
    winter_cursed()
    winter_restored()
