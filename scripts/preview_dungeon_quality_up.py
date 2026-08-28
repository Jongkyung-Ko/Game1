#!/usr/bin/env python3
"""Preview higher-quality dungeon map styles BEFORE app coding.

Generates:
  A+  Kenney tiles + torch light / shadow polish
  B   HQ painted procedural (larger beveled stones)
  C   Hybrid: painted floors + Kenney props/mobs + lighting
"""

from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFont, ImageFilter

ROOT = Path("/workspace")
SHEET = ROOT / "app/src/main/assets/kenney/tiny_dungeon.png"
OUT = Path("/opt/cursor/artifacts/dungeon-preview")

TILE = 16
COLS = 12

# Kenney indices (verified)
FLOOR, FLOOR_ALT, FLOOR_STONE = 0, 2, 12
WALL_TOP, WALL_FILL, WALL_MID, WALL_BRICK, WALL_WINDOW = 4, 5, 16, 17, 28
PILLAR, DOOR_OPEN = 7, 22
LADDER_UP, LADDER_DOWN = 63, 75
TOMB, BARREL, CHEST, CHEST_OPEN = 64, 82, 89, 91
POTION_R, POTION_B = 114, 116
HERO, KNIGHT = 100, 97
SLIME, ORC, BAT, SKELETON, SPIDER = 108, 110, 120, 121, 122


def tile_at(sheet: Image.Image, index: int) -> Image.Image:
    c, r = index % COLS, index // COLS
    return sheet.crop((c * TILE, r * TILE, c * TILE + TILE, r * TILE + TILE))


def make_layout(cols=28, rows=18, seed=42):
    rng = random.Random(seed)
    grid = [[1] * cols for _ in range(rows)]
    rooms = []
    for _ in range(8):
        w, h = rng.randint(5, 8), rng.randint(4, 6)
        x, y = rng.randint(1, cols - w - 2), rng.randint(1, rows - h - 2)
        overlap = sum(
            1
            for rr in range(y - 1, y + h + 1)
            for cc in range(x - 1, x + w + 1)
            if 0 <= rr < rows and 0 <= cc < cols and grid[rr][cc] == 0
        )
        if rooms and overlap > w * h * 0.25:
            continue
        for rr in range(y, y + h):
            for cc in range(x, x + w):
                grid[rr][cc] = 0
        rooms.append((x, y, w, h))
    if len(rooms) < 4:
        rooms = [(2, 12, 7, 5), (11, 8, 8, 5), (20, 3, 6, 5), (3, 2, 6, 5), (13, 2, 5, 4)]
        grid = [[1] * cols for _ in range(rows)]
        for x, y, w, h in rooms:
            for rr in range(y, y + h):
                for cc in range(x, x + w):
                    grid[rr][cc] = 0

    def carve(x0, y0, x1, y1):
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
        carve(*centers[i], *centers[i + 1])
    start, end = centers[0], centers[-1]
    return grid, rooms, start, end, rng


def is_walk(grid, c, r):
    return 0 <= r < len(grid) and 0 <= c < len(grid[0]) and grid[r][c] == 0


def wall_id(grid, c, r):
    below, above = is_walk(grid, c, r + 1), is_walk(grid, c, r - 1)
    side = is_walk(grid, c - 1, r) or is_walk(grid, c + 1, r)
    if below and not above:
        return WALL_TOP
    if side and not below:
        return WALL_MID
    if (c + r) % 5 == 0:
        return WALL_WINDOW
    if (c * 3 + r) % 7 == 0:
        return WALL_BRICK
    return WALL_FILL


def frame(img: Image.Image, title: str, note: str) -> Image.Image:
    pad, banner = 28, 56
    canvas = Image.new("RGBA", (img.width + pad * 2, img.height + pad * 2 + banner), (58, 40, 28, 255))
    d = ImageDraw.Draw(canvas)
    d.rounded_rectangle(
        (6, 6, canvas.width - 7, canvas.height - 7),
        radius=18,
        fill=(236, 220, 188, 255),
        outline=(28, 22, 16, 255),
        width=5,
    )
    d.rounded_rectangle((16, 14, canvas.width - 17, 14 + banner - 10), radius=12, fill=(45, 32, 20, 255))
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 22)
        small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 13)
    except Exception:
        font = ImageFont.load_default()
        small = font
    d.text((32, 24), title, fill=(217, 164, 65, 255), font=font)
    d.text((32, canvas.height - 24), note, fill=(90, 66, 49, 255), font=small)
    canvas.paste(img, (pad, pad + banner - 6), img if img.mode == "RGBA" else None)
    return canvas


def apply_torch_light(base: Image.Image, spots: list[tuple[int, int]], radius: int):
    light = Image.new("RGBA", base.size, (0, 0, 0, 0))
    ld = ImageDraw.Draw(light)
    for cx, cy in spots:
        for i in range(radius, 0, -3):
            a = int(70 * (1 - i / radius) ** 1.5)
            ld.ellipse((cx - i, cy - i, cx + i, cy + i), fill=(232, 132, 58, a))
    # darken outside a bit
    shade = Image.new("RGBA", base.size, (8, 4, 2, 70))
    out = Image.alpha_composite(base.convert("RGBA"), shade)
    out = Image.alpha_composite(out, light)
    return out


def render_a_plus(sheet: Image.Image, scale=5, seed=42) -> Image.Image:
    """Kenney tiles + lighting polish."""
    grid, rooms, start, end, rng = make_layout(seed=seed)
    rows, cols = len(grid), len(grid[0])
    cache = {}

    def blit(img, index, c, r):
        if index not in cache:
            cache[index] = tile_at(sheet, index)
        t = cache[index]
        img.paste(t, (c * TILE, r * TILE), t)

    raw = Image.new("RGBA", (cols * TILE, rows * TILE), (18, 14, 12, 255))
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 1:
                if any(is_walk(grid, c + dc, r + dr) for dc, dr in ((0, 1), (0, -1), (1, 0), (-1, 0))):
                    blit(raw, wall_id(grid, c, r), c, r)
            else:
                fid = FLOOR_STONE if (c * 5 + r * 3) % 11 == 0 else (FLOOR if (c + r) % 2 == 0 else FLOOR_ALT)
                blit(raw, fid, c, r)

    props = []
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            if (c, r) == start:
                blit(raw, LADDER_UP, c, r)
            elif (c, r) == end:
                blit(raw, LADDER_DOWN, c, r)
            elif (c * 13 + r * 7) % 29 == 0:
                blit(raw, PILLAR, c, r)
            elif (c + r * 2) % 31 == 0:
                blit(raw, TOMB, c, r)
            elif (c * 11 + r) % 23 == 0:
                blit(raw, CHEST if (c + r) % 2 == 0 else CHEST_OPEN, c, r)
                props.append((c, r))
            elif (c * 9 + r * 5) % 37 == 0:
                blit(raw, POTION_B if (c + r) % 2 else POTION_R, c, r)

    # monsters
    floors = [(c, r) for r in range(rows) for c in range(cols) if grid[r][c] == 0 and (c, r) not in (start, end)]
    rng.shuffle(floors)
    kinds = [SLIME, BAT, SPIDER, ORC, SKELETON]
    for i, (c, r) in enumerate(floors[: 5 + seed % 3]):
        if abs(c - start[0]) + abs(r - start[1]) < 4:
            continue
        blit(raw, kinds[i % len(kinds)], c, r)
    blit(raw, HERO, *start)
    if is_walk(grid, start[0] + 1, start[1]):
        blit(raw, KNIGHT, start[0] + 1, start[1])

    big = raw.resize((raw.width * scale, raw.height * scale), Image.NEAREST)
    # torch spots near walls
    spots = []
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            near = any(not is_walk(grid, c + dc, r + dr) for dc, dr in ((0, 1), (0, -1), (1, 0), (-1, 0)))
            if near and (c * 19 + r * 7) % 17 == 0:
                spots.append((c * TILE * scale + TILE * scale // 2, r * TILE * scale + TILE * scale // 3))
    lit = apply_torch_light(big, spots[:16], radius=TILE * scale * 3)
    lit = ImageEnhance.Contrast(lit.convert("RGB")).enhance(1.1).convert("RGBA")
    return frame(lit, "A+  Kenney tiles + torch light polish", "same Kenney sheet, sharper scale, warm light, deeper contrast")


def render_b_hq(seed=7, cell=32) -> Image.Image:
    """Painted HQ procedural."""
    grid, rooms, start, end, rng = make_layout(cols=30, rows=18, seed=seed)
    rows, cols = len(grid), len(grid[0])
    w, h = cols * cell, rows * cell
    img = Image.new("RGBA", (w, h), (16, 12, 10, 255))
    d = ImageDraw.Draw(img)

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            x0, y0 = c * cell, r * cell
            base = (210, 176, 124) if (c + r) % 2 == 0 else (190, 155, 100)
            n = rng.randint(-8, 8)
            col = tuple(max(40, min(255, ch + n)) for ch in base)
            d.rounded_rectangle((x0 + 1, y0 + 1, x0 + cell - 2, y0 + cell - 2), radius=5, fill=col)
            d.rectangle((x0 + 3, y0 + cell - 8, x0 + cell - 4, y0 + cell - 3), fill=(40, 28, 18, 40))

    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 1:
                continue
            if not any(is_walk(grid, c + dc, r + dr) for dc, dr in ((0, 1), (0, -1), (1, 0), (-1, 0))):
                continue
            x0, y0 = c * cell, r * cell
            d.rounded_rectangle(
                (x0 + 1, y0 + 1, x0 + cell - 2, y0 + cell - 2),
                radius=8,
                fill=(96, 90, 82),
                outline=(28, 24, 20),
                width=2,
            )
            d.ellipse((x0 + 7, y0 + 7, x0 + 14, y0 + 13), fill=(145, 138, 128, 170))
            d.ellipse((x0 + 16, y0 + 15, x0 + 23, y0 + 21), fill=(50, 46, 42, 150))

    light = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ld = ImageDraw.Draw(light)
    spots = []
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            if any(not is_walk(grid, c + dc, r + dr) for dc, dr in ((0, 1), (0, -1), (1, 0), (-1, 0))) and (
                c * 19 + r * 7
            ) % 15 == 0:
                cx, cy = c * cell + cell // 2, r * cell + cell // 3
                spots.append((cx, cy))
                d.rectangle((cx - 2, cy, cx + 2, cy + cell // 3), fill=(90, 58, 34))
                d.ellipse((cx - 6, cy - 9, cx + 6, cy + 2), fill=(232, 132, 58))
                d.ellipse((cx - 3, cy - 11, cx + 3, cy - 2), fill=(249, 222, 133))
    for cx, cy in spots:
        for i in range(cell * 4, 0, -3):
            a = int(60 * (1 - i / (cell * 4)) ** 1.4)
            ld.ellipse((cx - i, cy - i, cx + i, cy + i), fill=(232, 132, 58, a))
    img = Image.alpha_composite(img, light)

    # props
    def chest(c, r):
        x, y = c * cell + 5, r * cell + 9
        d.rounded_rectangle((x, y + 7, x + cell - 10, y + cell - 10), 3, fill=(107, 75, 46), outline=(30, 24, 18), width=2)
        d.rounded_rectangle((x, y, x + cell - 10, y + 11), 3, fill=(138, 90, 43), outline=(30, 24, 18), width=2)
        d.rectangle((x, y + 9, x + cell - 10, y + 12), fill=(217, 164, 65))

    def slime(c, r):
        x, y = c * cell + cell // 2, r * cell + cell // 2 + 2
        d.ellipse((x - 12, y - 12, x + 12, y + 7), fill=(111, 191, 90), outline=(40, 80, 35), width=2)
        d.ellipse((x - 5, y - 5, x - 2, y - 1), fill=(20, 30, 18))
        d.ellipse((x + 3, y - 5, x + 6, y - 1), fill=(20, 30, 18))

    def hero(c, r):
        x, y = c * cell + cell // 2, r * cell + cell - 3
        d.ellipse((x - 10, y - 4, x + 10, y + 3), fill=(0, 0, 0, 55))
        d.rectangle((x - 7, y - 28, x + 7, y - 10), fill=(62, 107, 138))
        d.polygon([(x - 11, y - 26), (x + 11, y - 26), (x + 13, y - 8), (x - 13, y - 8)], fill=(140, 47, 40))
        d.ellipse((x - 7, y - 40, x + 7, y - 26), fill=(231, 185, 143))

    def stairs_up(c, r):
        x, y = c * cell, r * cell
        for i in range(4):
            d.rounded_rectangle((x + 4 + i * 2, y + 5 + i * 6, x + cell - 4 - i * 2, y + 10 + i * 6), 2, fill=(138, 115, 80), outline=(30, 24, 18))

    def stairs_down(c, r):
        x, y = c * cell + cell // 2, r * cell + cell // 2
        d.ellipse((x - 12, y - 12, x + 12, y + 12), fill=(28, 22, 16), outline=(30, 24, 18), width=3)
        d.arc((x - 8, y - 8, x + 8, y + 8), 20, 280, fill=(217, 164, 65), width=3)

    stairs_up(*start)
    stairs_down(*end)
    for i, (x, y, rw, rh) in enumerate(rooms):
        chest(x + rw // 2, y + 1)
        slime(x + rw - 2, y + rh - 2)
    hero(*start)

    # edge outline
    edge = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ed = ImageDraw.Draw(edge)
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            x0, y0 = c * cell, r * cell
            if not is_walk(grid, c, r - 1):
                ed.line((x0 + 2, y0 + 2, x0 + cell - 3, y0 + 2), fill=(28, 24, 20, 230), width=3)
            if not is_walk(grid, c, r + 1):
                ed.line((x0 + 2, y0 + cell - 3, x0 + cell - 3, y0 + cell - 3), fill=(28, 24, 20, 230), width=3)
            if not is_walk(grid, c - 1, r):
                ed.line((x0 + 2, y0 + 2, x0 + 2, y0 + cell - 3), fill=(28, 24, 20, 230), width=3)
            if not is_walk(grid, c + 1, r):
                ed.line((x0 + cell - 3, y0 + 2, x0 + cell - 3, y0 + cell - 3), fill=(28, 24, 20, 230), width=3)
    img = Image.alpha_composite(img, edge)
    return frame(img, "B   HQ Painted procedural", "beveled stone, torch bloom, clear rooms — Compose Canvas port")


def render_c_hybrid(sheet: Image.Image, seed=21, cell=30, scale_tile=2) -> Image.Image:
    """Painted floors/walls + Kenney props/mobs on top + lighting."""
    grid, rooms, start, end, rng = make_layout(cols=28, rows=18, seed=seed)
    rows, cols = len(grid), len(grid[0])
    # paint at cell size
    w, h = cols * cell, rows * cell
    img = Image.new("RGBA", (w, h), (14, 10, 8, 255))
    d = ImageDraw.Draw(img)
    for r in range(rows):
        for c in range(cols):
            x0, y0 = c * cell, r * cell
            if grid[r][c] == 1:
                if any(is_walk(grid, c + dc, r + dr) for dc, dr in ((0, 1), (0, -1), (1, 0), (-1, 0))):
                    d.rounded_rectangle(
                        (x0 + 1, y0 + 1, x0 + cell - 2, y0 + cell - 2),
                        radius=7,
                        fill=(88, 82, 74),
                        outline=(25, 20, 16),
                        width=2,
                    )
                    d.ellipse((x0 + 6, y0 + 6, x0 + 12, y0 + 11), fill=(130, 122, 112, 160))
            else:
                base = (205, 170, 118) if (c + r) % 2 == 0 else (186, 150, 96)
                d.rounded_rectangle((x0 + 1, y0 + 1, x0 + cell - 2, y0 + cell - 2), radius=4, fill=base)

    # upscale kenney sprites onto painted map
    cache = {}

    def stamp(index, c, r, size=None):
        size = size or cell
        if index not in cache:
            cache[index] = tile_at(sheet, index).resize((size, size), Image.NEAREST)
        t = cache[index]
        x = c * cell + (cell - size) // 2
        y = r * cell + (cell - size) // 2
        img.paste(t, (x, y), t)

    stamp(LADDER_UP, *start)
    stamp(LADDER_DOWN, *end)
    floors = [(c, r) for r in range(rows) for c in range(cols) if grid[r][c] == 0]
    rng.shuffle(floors)
    for c, r in floors[:6]:
        if (c, r) in (start, end):
            continue
        stamp(CHEST if (c + r) % 2 == 0 else BARREL, c, r)
    for c, r in floors[8:12]:
        stamp(POTION_R if c % 2 == 0 else POTION_B, c, r)
    kinds = [SLIME, BAT, SPIDER, ORC, SKELETON]
    for i, (c, r) in enumerate(floors[15:22]):
        if abs(c - start[0]) + abs(r - start[1]) < 4:
            continue
        stamp(kinds[i % len(kinds)], c, r)
    stamp(HERO, *start)
    if is_walk(grid, start[0] + 1, start[1]):
        stamp(KNIGHT, start[0] + 1, start[1])

    spots = []
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] != 0:
                continue
            if any(not is_walk(grid, c + dc, r + dr) for dc, dr in ((0, 1), (0, -1), (1, 0), (-1, 0))) and (
                c * 17 + r * 9
            ) % 16 == 0:
                cx, cy = c * cell + cell // 2, r * cell + cell // 3
                spots.append((cx, cy))
                d.ellipse((cx - 5, cy - 8, cx + 5, cy + 1), fill=(232, 132, 58))
    lit = apply_torch_light(img, spots[:14], radius=cell * 3)
    lit = ImageEnhance.Color(lit.convert("RGB")).enhance(1.12).convert("RGBA")
    return frame(
        lit,
        "C   Hybrid (painted + Kenney props)",
        "beveled floors/walls + Kenney chests/mobs + torch glow — recommended upgrade",
    )


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    sheet = Image.open(SHEET).convert("RGBA")

    a = render_a_plus(sheet, scale=5, seed=42)
    b = render_b_hq(seed=7, cell=32)
    c = render_c_hybrid(sheet, seed=21, cell=30)

    pa = OUT / "quality-Aplus-kenney-lit.png"
    pb = OUT / "quality-B-hq-painted.png"
    pc = OUT / "quality-C-hybrid.png"
    a.save(pa, optimize=True)
    b.save(pb, optimize=True)
    c.save(pc, optimize=True)
    print("wrote", pa, a.size)
    print("wrote", pb, b.size)
    print("wrote", pc, c.size)

    # comparison strip
    def fit(im, tw=720):
        ratio = tw / im.width
        return im.resize((tw, int(im.height * ratio)), Image.Resampling.LANCZOS)

    aa, bb, cc = fit(a), fit(b), fit(c)
    gap = 16
    comp_w = aa.width + bb.width + cc.width + gap * 4
    comp_h = max(aa.height, bb.height, cc.height) + 48
    comp = Image.new("RGBA", (comp_w, comp_h), (36, 26, 18, 255))
    d = ImageDraw.Draw(comp)
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 20)
    except Exception:
        font = ImageFont.load_default()
    d.text((gap, 12), "Pick a higher-quality dungeon look (code-generated previews)", fill=(217, 164, 65, 255), font=font)
    x = gap
    for im in (aa, bb, cc):
        comp.paste(im, (x, 40), im)
        x += im.width + gap
    out = OUT / "quality-compare-Aplus-B-C.png"
    comp.save(out, optimize=True)
    print("wrote", out)

    # mobile crops
    for src, name in ((c, "quality-C-hybrid-mobile.png"), (a, "quality-Aplus-mobile.png")):
        im = Image.open(src) if isinstance(src, Path) else src
        # use saved
    Image.open(pc).crop((40, 70, 40 + 720, 70 + 1100)).save(OUT / "quality-C-hybrid-mobile.png", optimize=True)
    Image.open(pa).crop((40, 70, 40 + 720, 70 + 1100)).save(OUT / "quality-Aplus-mobile.png", optimize=True)
    print("mobile crops done")


if __name__ == "__main__":
    main()
