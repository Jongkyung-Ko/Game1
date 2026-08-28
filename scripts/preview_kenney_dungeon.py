#!/usr/bin/env python3
"""Code-generated dungeon map previews using Kenney Tiny Dungeon tilesheet.

Tile indices verified against the packed 12x11 sheet in
app/src/main/assets/kenney/tiny_dungeon.png
"""

from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageEnhance

ROOT = Path("/workspace")
SHEET = ROOT / "app/src/main/assets/kenney/tiny_dungeon.png"
OUT_DIR = Path("/opt/cursor/artifacts/dungeon-preview")

TILE = 16
COLS = 12

# Terrain
FLOOR = 0
FLOOR_ALT = 2
FLOOR_ALT2 = 3
FLOOR_STONE = 12
WALL_FILL = 5
WALL_TOP = 4
WALL_MID = 16
WALL_BRICK = 17
WALL_WINDOW = 28
PILLAR = 7
DOOR_CLOSED = 10
DOOR_OPEN = 22
LADDER_UP = 63
LADDER_DOWN = 75
CHEST = 89
CHEST_OPEN = 91
BARREL = 82
TOMB = 64
POTION_R = 114
POTION_B = 116
# Characters / monsters
HERO = 100
KNIGHT = 97
SLIME = 108
ORC = 110
BAT = 120
SPIDER = 122
SKELETON = 121


def tile_at(sheet: Image.Image, index: int) -> Image.Image:
    c = index % COLS
    r = index // COLS
    return sheet.crop((c * TILE, r * TILE, c * TILE + TILE, r * TILE + TILE))


def generate_layout(floor: int, cols: int = 26, rows: int = 18, seed: int | None = None):
    rng = random.Random(seed if seed is not None else floor * 7919 + 42)
    grid = [["WALL"] * cols for _ in range(rows)]
    rooms: list[tuple[range, range]] = []

    for _ in range(6 + min(floor, 3)):
        w = rng.randint(4, 8)
        h = rng.randint(4, 6)
        c0 = rng.randint(1, cols - w - 2)
        r0 = rng.randint(1, rows - h - 2)
        cr, rr = range(c0, c0 + w), range(r0, r0 + h)
        # reject heavy overlap
        overlap = 0
        for r in rr:
            for c in cr:
                if grid[r][c] != "WALL":
                    overlap += 1
        if overlap > w * h * 0.35:
            continue
        rooms.append((cr, rr))
        for r in rr:
            for c in cr:
                grid[r][c] = "FLOOR"

    if len(rooms) < 3:
        # fallback fixed rooms
        rooms = [
            (range(2, 8), range(11, 16)),
            (range(9, 16), range(7, 12)),
            (range(16, 23), range(2, 8)),
            (range(4, 10), range(2, 7)),
        ]
        for cr, rr in rooms:
            for r in rr:
                for c in cr:
                    if 0 <= r < rows and 0 <= c < cols:
                        grid[r][c] = "FLOOR"

    def carve(c: int, r: int):
        if 1 <= c < cols - 1 and 1 <= r < rows - 1:
            grid[r][c] = "FLOOR"

    for i in range(len(rooms) - 1):
        a_c, a_r = rooms[i]
        b_c, b_r = rooms[i + 1]
        cx = (a_c.start + a_c.stop - 1) // 2
        cy = (a_r.start + a_r.stop - 1) // 2
        tx = (b_c.start + b_c.stop - 1) // 2
        ty = (b_r.start + b_r.stop - 1) // 2
        while cx != tx:
            carve(cx, cy)
            carve(cx, cy - 1)
            cx += 1 if tx > cx else -1
        while cy != ty:
            carve(cx, cy)
            carve(cx - 1, cy)
            cy += 1 if ty > cy else -1

    start = rooms[0]
    end = rooms[-1]
    sc = (start[0].start + start[0].stop - 1) // 2
    sr = (start[1].start + start[1].stop - 1) // 2
    ec = (end[0].start + end[0].stop - 1) // 2
    er = (end[1].start + end[1].stop - 1) // 2
    grid[sr][sc] = "STAIRS_UP"
    grid[er][ec] = "STAIRS_DOWN"

    floors = [(c, r) for r in range(rows) for c in range(cols) if grid[r][c] == "FLOOR"]
    rng.shuffle(floors)
    for c, r in floors[: 2 + floor]:
        grid[r][c] = "CHEST"
    for c, r in floors[5 : 7 + floor]:
        grid[r][c] = "POTION"
    for c, r in floors[12 : 14 + floor]:
        grid[r][c] = "BARREL"

    monsters = []
    for c, r in floors[20 : 20 + 5 + floor]:
        if grid[r][c] != "FLOOR":
            continue
        if abs(c - sc) + abs(r - sr) < 4:
            continue
        monsters.append((c, r, rng.choice(["SLIME", "BAT", "SPIDER", "ORC", "SKELETON"])))

    return grid, (sc, sr), monsters


def is_walk(grid, c, r) -> bool:
    rows, cols = len(grid), len(grid[0])
    if not (0 <= c < cols and 0 <= r < rows):
        return False
    return grid[r][c] != "WALL"


def wall_index(grid, c, r) -> int:
    below = is_walk(grid, c, r + 1)
    above = is_walk(grid, c, r - 1)
    left = is_walk(grid, c - 1, r)
    right = is_walk(grid, c + 1, r)
    if below and not above:
        return WALL_TOP
    if (left or right) and not below:
        return WALL_MID
    if (c + r) % 5 == 0:
        return WALL_WINDOW
    if (c * 3 + r) % 7 == 0:
        return WALL_BRICK
    return WALL_FILL


def render_floor(sheet: Image.Image, floor: int, scale: int = 5, seed: int | None = None) -> Image.Image:
    grid, start, monsters = generate_layout(floor, seed=seed)
    rows, cols = len(grid), len(grid[0])
    map_w, map_h = cols * TILE, rows * TILE

    # deep backdrop under walls
    base = Image.new("RGBA", (map_w, map_h), (18, 14, 12, 255))
    cache: dict[int, Image.Image] = {}

    def blit(index: int, c: int, r: int):
        if index not in cache:
            cache[index] = tile_at(sheet, index)
        t = cache[index]
        base.paste(t, (c * TILE, r * TILE), t)

    # pass 1: floors + walls
    for r in range(rows):
        for c in range(cols):
            cell = grid[r][c]
            if cell == "WALL":
                blit(wall_index(grid, c, r), c, r)
            else:
                idx = FLOOR if (c + r) % 2 == 0 else FLOOR_ALT
                if (c * 5 + r * 3) % 11 == 0:
                    idx = FLOOR_STONE
                blit(idx, c, r)

    # pass 2: props / stairs
    for r in range(rows):
        for c in range(cols):
            cell = grid[r][c]
            if cell == "STAIRS_UP":
                blit(LADDER_UP, c, r)
            elif cell == "STAIRS_DOWN":
                blit(LADDER_DOWN, c, r)
            elif cell == "CHEST":
                blit(CHEST if (c + r) % 2 == 0 else CHEST_OPEN, c, r)
            elif cell == "POTION":
                blit(POTION_R if (c + r) % 2 == 0 else POTION_B, c, r)
            elif cell == "BARREL":
                blit(BARREL, c, r)
            elif cell == "FLOOR" and (c * 13 + r * 7) % 23 == 0 and is_walk(grid, c, r + 1) is False:
                blit(PILLAR, c, r)
            elif cell == "FLOOR" and (c + r * 2) % 31 == 0:
                blit(TOMB, c, r)
            elif cell == "FLOOR" and abs(c - start[0]) + abs(r - start[1]) == 2:
                blit(DOOR_OPEN, c, r)

    kind_tile = {
        "SLIME": SLIME,
        "BAT": BAT,
        "SPIDER": SPIDER,
        "ORC": ORC,
        "SKELETON": SKELETON,
    }
    for c, r, kind in monsters:
        blit(kind_tile[kind], c, r)

    blit(HERO, start[0], start[1])
    # companion knight nearby if space
    if is_walk(grid, start[0] + 1, start[1]):
        blit(KNIGHT, start[0] + 1, start[1])

    # soft vignette for atmosphere
    big = base.resize((map_w * scale, map_h * scale), Image.NEAREST)
    vignette = Image.new("RGBA", big.size, (0, 0, 0, 0))
    vdraw = ImageDraw.Draw(vignette)
    for i in range(40):
        alpha = int(8 + i * 2.2)
        vdraw.rectangle(
            (i, i, big.width - 1 - i, big.height - 1 - i),
            outline=(10, 6, 4, alpha),
        )
    big = Image.alpha_composite(big, vignette)

    # warm grade
    big_rgb = big.convert("RGB")
    big_rgb = ImageEnhance.Color(big_rgb).enhance(1.15)
    big_rgb = ImageEnhance.Contrast(big_rgb).enhance(1.08)
    big = big_rgb.convert("RGBA")

    pad = 30
    banner_h = 56
    canvas = Image.new("RGBA", (big.width + pad * 2, big.height + pad * 2 + banner_h), (58, 40, 28, 255))
    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle(
        (6, 6, canvas.width - 7, canvas.height - 7),
        radius=20,
        fill=(236, 220, 188, 255),
        outline=(28, 22, 16, 255),
        width=5,
    )
    draw.rounded_rectangle(
        (16, 14, canvas.width - 17, 14 + banner_h - 10),
        radius=12,
        fill=(45, 32, 20, 255),
    )
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 24)
        small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 14)
    except Exception:
        font = ImageFont.load_default()
        small = font
    draw.text((32, 24), f"Floor {floor}  ·  Kenney Tiny Dungeon tiles", fill=(217, 164, 65, 255), font=font)
    draw.text(
        (32, canvas.height - 24),
        "code-generated preview (procedural rooms + tilesheet) — not AI collage",
        fill=(90, 66, 49, 255),
        font=small,
    )
    canvas.paste(big, (pad, pad + banner_h - 6), big)
    return canvas


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    sheet = Image.open(SHEET).convert("RGBA")

    # clear old weak previews
    for p in OUT_DIR.glob("dungeon-floor-*.png"):
        p.unlink()

    paths = []
    for floor, seed in [(1, 42), (2, 99), (3, 2026)]:
        img = render_floor(sheet, floor=floor, scale=5, seed=seed)
        path = OUT_DIR / f"dungeon-floor-{floor}-kenney.png"
        img.save(path, optimize=True)
        paths.append(path)
        print("wrote", path, img.size)

    # mobile viewport crop from floor 1 (around hero/start room)
    full = Image.open(paths[0]).convert("RGBA")
    # approx start room lower-left → crop a phone-like window
    crop = full.crop((80, 120, 80 + 720, 120 + 1100))
    crop_path = OUT_DIR / "dungeon-floor-1-mobile-view.png"
    crop.save(crop_path, optimize=True)
    print("wrote", crop_path, crop.size)

    # side-by-side comparison strip: floor1 + floor2
    a = Image.open(paths[0]).resize((920, 700), Image.NEAREST)
    b = Image.open(paths[1]).resize((920, 700), Image.NEAREST)
    compare = Image.new("RGBA", (a.width + b.width + 24, max(a.height, b.height) + 20), (40, 30, 22, 255))
    compare.paste(a, (8, 10))
    compare.paste(b, (a.width + 16, 10))
    cmp_path = OUT_DIR / "dungeon-compare-floor1-2.png"
    compare.save(cmp_path, optimize=True)
    print("wrote", cmp_path)


if __name__ == "__main__":
    main()
