#!/usr/bin/env python3
"""Higher-quality procedural dungeon map previews (painted, not tiny 16px sheet).

This is Style B for the upcoming app change: richer stone walls, warm torch light,
checker floors, clear rooms/corridors — generated entirely by code (Pillow).
"""

from __future__ import annotations

import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageEnhance

OUT_DIR = Path("/opt/cursor/artifacts/dungeon-preview")


def generate_rooms(floor: int, cols=32, rows=22, seed=None):
    rng = random.Random(seed if seed is not None else 1000 + floor * 17)
    grid = [[1] * cols for _ in range(rows)]  # 1=wall 0=floor
    rooms = []
    for _ in range(7):
        w, h = rng.randint(5, 9), rng.randint(4, 7)
        x, y = rng.randint(1, cols - w - 2), rng.randint(1, rows - h - 2)
        # padding gap
        ok = True
        for rr in range(y - 1, y + h + 1):
            for cc in range(x - 1, x + w + 1):
                if 0 <= rr < rows and 0 <= cc < cols and grid[rr][cc] == 0:
                    ok = False
                    break
            if not ok:
                break
        if not ok and rooms:
            continue
        for rr in range(y, y + h):
            for cc in range(x, x + w):
                grid[rr][cc] = 0
        rooms.append((x, y, w, h))

    if len(rooms) < 4:
        rooms = [(2, 14, 7, 5), (11, 10, 8, 5), (21, 3, 8, 6), (4, 3, 6, 5), (14, 2, 5, 4)]
        grid = [[1] * cols for _ in range(rows)]
        for x, y, w, h in rooms:
            for rr in range(y, y + h):
                for cc in range(x, x + w):
                    if 0 <= rr < rows and 0 <= cc < cols:
                        grid[rr][cc] = 0

    def carve_line(x0, y0, x1, y1):
        x, y = x0, y0
        while x != x1:
            for dy in (0, 1):
                if 0 <= y + dy < rows:
                    grid[y + dy][x] = 0
            x += 1 if x1 > x else -1
        while y != y1:
            for dx in (0, 1):
                if 0 <= x + dx < cols:
                    grid[y][x + dx] = 0
            y += 1 if y1 > y else -1

    centers = [(x + w // 2, y + h // 2) for x, y, w, h in rooms]
    for i in range(len(centers) - 1):
        carve_line(*centers[i], *centers[i + 1])

    start = centers[0]
    end = centers[-1]
    return grid, rooms, start, end, rng


def render(floor: int, cell=28, seed=None) -> Image.Image:
    grid, rooms, start, end, rng = generate_rooms(floor, seed=seed)
    rows, cols = len(grid), len(grid[0])
    w, h = cols * cell, rows * cell

    img = Image.new("RGBA", (w, h), (22, 16, 12, 255))
    draw = ImageDraw.Draw(img)

    # floor
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            x0, y0 = c * cell, r * cell
            base = (201, 168, 118) if (c + r) % 2 == 0 else (184, 149, 95)
            # subtle noise
            n = rng.randint(-10, 10)
            col = tuple(max(0, min(255, ch + n)) for ch in base)
            draw.rounded_rectangle((x0 + 1, y0 + 1, x0 + cell - 2, y0 + cell - 2), radius=4, fill=col)
            # edge shade
            draw.rectangle((x0 + 2, y0 + cell - 7, x0 + cell - 3, y0 + cell - 3), fill=(35, 24, 16, 35))

    # walls with bevel
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 1:
                continue
            # only draw walls that touch floor (visible shell) OR all walls for density
            neighbors = [
                (c, r - 1), (c, r + 1), (c - 1, r), (c + 1, r),
            ]
            touches = any(
                0 <= nc < cols and 0 <= nr < rows and grid[nr][nc] == 0
                for nc, nr in neighbors
            )
            if not touches and not (1 <= c < cols - 1 and 1 <= r < rows - 1):
                continue
            if not touches:
                # deep fill
                x0, y0 = c * cell, r * cell
                draw.rectangle((x0, y0, x0 + cell - 1, y0 + cell - 1), fill=(40, 34, 30))
                continue

            x0, y0 = c * cell, r * cell
            # rock blob-ish rounded block
            draw.rounded_rectangle(
                (x0 + 1, y0 + 1, x0 + cell - 2, y0 + cell - 2),
                radius=7,
                fill=(90, 84, 76),
                outline=(35, 30, 25),
                width=2,
            )
            # highlight + pit
            draw.ellipse((x0 + 6, y0 + 6, x0 + 13, y0 + 12), fill=(130, 122, 112, 160))
            draw.ellipse((x0 + 15, y0 + 14, x0 + 22, y0 + 20), fill=(55, 50, 45, 140))

    # light layer
    light = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ld = ImageDraw.Draw(light)

    def add_torch(cx, cy, strength=1.0):
        rad = int(cell * 3.2 * strength)
        for i in range(rad, 0, -2):
            a = int(55 * strength * (1 - i / rad) ** 1.4)
            ld.ellipse((cx - i, cy - i, cx + i, cy + i), fill=(232, 132, 58, a))
        # flame
        draw.rectangle((cx - 2, cy - 2, cx + 2, cy + cell // 3), fill=(90, 58, 34))
        draw.ellipse((cx - 6, cy - 10, cx + 6, cy + 2), fill=(232, 132, 58))
        draw.ellipse((cx - 3, cy - 12, cx + 3, cy - 2), fill=(249, 222, 133))

    # place torches along floor near walls
    torch_spots = []
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            wall_near = any(
                not (0 <= c + dc < cols and 0 <= r + dr < rows) or grid[r + dr][c + dc] == 1
                for dc, dr in ((0, -1), (0, 1), (-1, 0), (1, 0))
            )
            if wall_near and (c * 19 + r * 7 + floor) % 17 == 0:
                torch_spots.append((c * cell + cell // 2, r * cell + cell // 3))

    for tx, ty in torch_spots[:14]:
        add_torch(tx, ty, 1.0)

    img = Image.alpha_composite(img, light)

    # props
    def draw_chest(c, r):
        x, y = c * cell + 4, r * cell + 8
        draw.rounded_rectangle((x, y + 6, x + cell - 8, y + cell - 10), radius=3, fill=(107, 75, 46), outline=(35, 30, 25), width=2)
        draw.rounded_rectangle((x, y, x + cell - 8, y + 10), radius=3, fill=(138, 90, 43), outline=(35, 30, 25), width=2)
        draw.rectangle((x, y + 8, x + cell - 8, y + 11), fill=(217, 164, 65))
        draw.ellipse((x + cell // 2 - 6, y + 10, x + cell // 2 - 1, y + 15), fill=(249, 222, 133))

    def draw_stairs_up(c, r):
        x, y = c * cell, r * cell
        for i in range(4):
            yy = y + 4 + i * 6
            xx = x + 4 + i * 2
            ww = cell - 8 - i * 4
            draw.rounded_rectangle((xx, yy, xx + ww, yy + 5), radius=2, fill=(138, 115, 80), outline=(35, 30, 25))

    def draw_stairs_down(c, r):
        x, y = c * cell + cell // 2, r * cell + cell // 2
        draw.ellipse((x - 11, y - 11, x + 11, y + 11), fill=(30, 24, 18), outline=(35, 30, 25), width=3)
        draw.arc((x - 8, y - 8, x + 8, y + 8), 20, 280, fill=(217, 164, 65), width=3)

    def draw_slime(c, r):
        x, y = c * cell + cell // 2, r * cell + cell // 2 + 2
        draw.ellipse((x - 12, y - 4, x + 12, y + 8), fill=(0, 0, 0, 40))
        draw.ellipse((x - 11, y - 12, x + 11, y + 6), fill=(111, 191, 90), outline=(47, 90, 40), width=2)
        draw.ellipse((x - 7, y - 10, x - 2, y - 6), fill=(216, 255, 200, 180))
        draw.ellipse((x - 5, y - 5, x - 2, y - 1), fill=(30, 42, 24))
        draw.ellipse((x + 3, y - 5, x + 6, y - 1), fill=(30, 42, 24))

    def draw_hero(c, r):
        x, y = c * cell + cell // 2, r * cell + cell - 4
        draw.ellipse((x - 10, y - 4, x + 10, y + 4), fill=(0, 0, 0, 50))
        draw.rectangle((x - 7, y - 28, x + 7, y - 10), fill=(62, 107, 138))
        draw.polygon([(x - 10, y - 26), (x + 10, y - 26), (x + 12, y - 8), (x - 12, y - 8)], fill=(140, 47, 40))
        draw.ellipse((x - 7, y - 40, x + 7, y - 26), fill=(231, 185, 143))
        draw.rectangle((x - 8, y - 42, x + 8, y - 36), fill=(90, 58, 34))

    # decorate rooms
    props = []
    for i, (x, y, rw, rh) in enumerate(rooms):
        if i == 0:
            draw_stairs_up(*start)
        if i == len(rooms) - 1:
            draw_stairs_down(*end)
        # chest in room
        cx, cy = x + rw // 2, y + 1
        if grid[cy][cx] == 0:
            draw_chest(cx, cy)
            props.append((cx, cy))
        # slime
        sx, sy = x + rw - 2, y + rh - 2
        if grid[sy][sx] == 0 and (sx, sy) not in props:
            draw_slime(sx, sy)

    draw_hero(*start)

    # outline corridors edges
    edge = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ed = ImageDraw.Draw(edge)
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            x0, y0 = c * cell, r * cell
            if r == 0 or grid[r - 1][c] == 1:
                ed.line((x0 + 2, y0 + 2, x0 + cell - 3, y0 + 2), fill=(35, 30, 25, 220), width=3)
            if r == rows - 1 or grid[r + 1][c] == 1:
                ed.line((x0 + 2, y0 + cell - 3, x0 + cell - 3, y0 + cell - 3), fill=(35, 30, 25, 220), width=3)
            if c == 0 or grid[r][c - 1] == 1:
                ed.line((x0 + 2, y0 + 2, x0 + 2, y0 + cell - 3), fill=(35, 30, 25, 220), width=3)
            if c == cols - 1 or grid[r][c + 1] == 1:
                ed.line((x0 + cell - 3, y0 + 2, x0 + cell - 3, y0 + cell - 3), fill=(35, 30, 25, 220), width=3)
    img = Image.alpha_composite(img, edge)

    # frame
    pad, banner = 28, 54
    canvas = Image.new("RGBA", (w + pad * 2, h + pad * 2 + banner), (58, 40, 28, 255))
    d = ImageDraw.Draw(canvas)
    d.rounded_rectangle((6, 6, canvas.width - 7, canvas.height - 7), radius=18, fill=(236, 220, 188, 255), outline=(28, 22, 16, 255), width=5)
    d.rounded_rectangle((16, 14, canvas.width - 17, 14 + banner - 10), radius=12, fill=(45, 32, 20, 255))
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 24)
        small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 14)
    except Exception:
        font = ImageFont.load_default()
        small = font
    d.text((32, 24), f"Floor {floor}  ·  HQ Procedural (Painted)", fill=(217, 164, 65, 255), font=font)
    d.text((32, canvas.height - 24), "code-generated preview — torch light, beveled stone, cartoon props", fill=(90, 66, 49, 255), font=small)
    canvas.paste(img, (pad, pad + banner - 6), img)
    return canvas


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    paths = []
    for floor, seed in [(1, 7), (2, 21), (3, 88)]:
        img = render(floor, cell=30, seed=seed)
        path = OUT_DIR / f"dungeon-floor-{floor}-hq-procedural.png"
        img.save(path, optimize=True)
        paths.append(path)
        print("wrote", path, img.size)

    # mobile crop
    full = Image.open(paths[0])
    crop = full.crop((40, 70, 40 + 720, 70 + 1100))
    crop_path = OUT_DIR / "dungeon-floor-1-hq-mobile.png"
    crop.save(crop_path, optimize=True)
    print("wrote", crop_path)

    # compare Kenney vs HQ if kenney exists
    k = OUT_DIR / "dungeon-floor-1-kenney.png"
    if k.exists():
        a = Image.open(k).convert("RGBA").resize((900, 660), Image.NEAREST)
        b = Image.open(paths[0]).convert("RGBA").resize((900, 660), Image.Resampling.LANCZOS)
        cmp = Image.new("RGBA", (a.width + b.width + 30, 700), (40, 30, 22, 255))
        d = ImageDraw.Draw(cmp)
        try:
            font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 20)
        except Exception:
            font = ImageFont.load_default()
        d.text((20, 8), "A) Kenney Tiny Dungeon tiles", fill=(217, 164, 65, 255), font=font)
        d.text((a.width + 40, 8), "B) HQ Procedural painted", fill=(217, 164, 65, 255), font=font)
        cmp.paste(a, (10, 36), a)
        cmp.paste(b, (a.width + 20, 36), b)
        out = OUT_DIR / "dungeon-style-compare-A-Kenney-vs-B-HQ.png"
        cmp.save(out, optimize=True)
        print("wrote", out)


if __name__ == "__main__":
    main()
