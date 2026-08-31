#!/usr/bin/env python3
"""Slice generated 4-frame strips and synthesize rank walk/attack frames.

Output: app/src/main/assets/custom/hero_anim/frames/{set}_{0-3}.png  (220×320 RGBA)
"""
from __future__ import annotations

import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance

ROOT = Path(__file__).resolve().parents[1]
FRAMES = ROOT / "app/src/main/assets/custom/hero_anim/frames"
CUSTOM = ROOT / "app/src/main/assets/custom"
ART = Path("/opt/cursor/artifacts/assets")

W, H = 220, 320
WHITE = 242

JOBS = ("knight", "warrior", "mage", "archer")
ATTACK = {"knight": "slash", "warrior": "slash", "mage": "magic", "archer": "bow"}

GENERATED_STRIPS = [
    ("knight_walk_side", "knight_walk_side_strip.png"),
    ("knight_walk_down", "knight_walk_down_strip.png"),
    ("knight_walk_up", "knight_walk_up_strip.png"),
    ("knight_slash", "knight_slash_strip.png"),
    ("mage_walk_side", "mage_walk_side_strip.png"),
    ("mage_walk_down", "mage_walk_down_strip.png"),
    ("mage_walk_up", "mage_walk_up_strip.png"),
    ("mage_magic", "mage_magic_strip.png"),
    ("archer_walk_side", "archer_walk_side_strip.png"),
    ("archer_walk_down", "archer_walk_down_strip.png"),
    ("archer_walk_up", "archer_walk_up_strip.png"),
    ("archer_bow", "archer_bow_strip.png"),
    ("walk_up", "warrior_walk_up_strip.png"),  # warrior rank 0 unprefixed
]


def knock_bg(im: Image.Image, thresh: int = WHITE) -> Image.Image:
    arr = np.array(im.convert("RGBA"))
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    mx = np.maximum(np.maximum(r, g), b)
    mn = np.minimum(np.minimum(r, g), b)
    near_white = (r >= thresh) & (g >= thresh) & (b >= thresh)
    near_black = (mx <= 18) & (mn <= 18)
    gray = (mx - mn <= 8) & (mx >= 230)
    arr[near_white | near_black | gray, 3] = 0
    # soften leftover halo
    alpha = arr[:, :, 3].astype(np.float32)
    glow = (r.astype(np.int16) + g.astype(np.int16) + b.astype(np.int16)) / 3
    fade = (glow >= 220) & (alpha > 0)
    alpha[fade] *= np.clip((250 - glow[fade]) / 30.0, 0, 1)
    arr[:, :, 3] = alpha.astype(np.uint8)
    return Image.fromarray(arr)


def content_bbox(im: Image.Image, pad: int = 0) -> tuple[int, int, int, int] | None:
    a = np.array(im.split()[-1])
    ys, xs = np.where(a > 18)
    if xs.size == 0:
        return None
    x0, x1 = int(xs.min()), int(xs.max()) + 1
    y0, y1 = int(ys.min()), int(ys.max()) + 1
    x0 = max(0, x0 - pad)
    y0 = max(0, y0 - pad)
    x1 = min(im.width, x1 + pad)
    y1 = min(im.height, y1 + pad)
    return x0, y0, x1, y1


def fit_frame(sprite: Image.Image) -> Image.Image:
    sprite = sprite.convert("RGBA")
    bbox = content_bbox(sprite)
    if bbox is None:
        return Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sprite = sprite.crop(bbox)
    sw, sh = sprite.size
    scale = min((W - 20) / max(sw, 1), (H - 10) / max(sh, 1))
    nw = max(1, int(round(sw * scale)))
    nh = max(1, int(round(sh * scale)))
    sprite = sprite.resize((nw, nh), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    x = (W - nw) // 2
    y = H - nh - 4
    canvas.paste(sprite, (x, y), sprite)
    return canvas


def split_strip(path: Path) -> list[Image.Image]:
    raw = knock_bg(Image.open(path))
    arr = np.array(raw)
    alpha = arr[:, :, 3]
    col = (alpha > 16).any(axis=0)
    runs: list[tuple[int, int]] = []
    i = 0
    n = col.size
    while i < n:
        if not col[i]:
            i += 1
            continue
        j = i
        while j < n and col[j]:
            j += 1
        if j - i >= 8:
            runs.append((i, j))
        i = j
    if len(runs) >= 4:
        # merge extras by taking the 4 widest
        runs = sorted(runs, key=lambda r: r[1] - r[0], reverse=True)[:4]
        runs = sorted(runs, key=lambda r: r[0])
        frames = []
        for x0, x1 in runs:
            frames.append(fit_frame(raw.crop((x0, 0, x1, raw.height))))
        return frames
    # equal columns fallback
    cw = raw.width / 4.0
    frames = []
    for k in range(4):
        x0 = int(round(k * cw))
        x1 = int(round((k + 1) * cw))
        frames.append(fit_frame(raw.crop((x0, 0, x1, raw.height))))
    return frames


def save_set(name: str, frames: list[Image.Image]) -> None:
    FRAMES.mkdir(parents=True, exist_ok=True)
    for i, fr in enumerate(frames[:4]):
        out = FRAMES / f"{name}_{i}.png"
        fr.save(out, "PNG")
        print("wrote", out.relative_to(ROOT), fr.size)


def static_path(job: str, rank: int, facing: str) -> Path | None:
    if job == "warrior" and rank <= 0:
        p = CUSTOM / f"hero_{facing}.png"
        return p if p.exists() else None
    if rank <= 0:
        p = CUSTOM / f"hero_{job}_{facing}.png"
        return p if p.exists() else None
    p = CUSTOM / f"hero_{job}_r{rank}_{facing}.png"
    return p if p.exists() else None


def load_static(job: str, rank: int, facing: str) -> Image.Image | None:
    p = static_path(job, rank, facing)
    if p is None:
        return None
    return fit_frame(knock_bg(Image.open(p)))


def _paste(dst: Image.Image, src: Image.Image, xy: tuple[int, int]) -> None:
    tmp = Image.new("RGBA", dst.size, (0, 0, 0, 0))
    tmp.paste(src, xy, src)
    out = Image.alpha_composite(dst, tmp)
    dst.paste(out)


def walk_from_static(base: Image.Image, frame: int, facing: str) -> Image.Image:
    """Split-leg stride + bob from a still sprite."""
    bbox = content_bbox(base)
    if bbox is None:
        return base
    x0, y0, x1, y1 = bbox
    bh = y1 - y0
    hip = y0 + int(bh * 0.58)
    cx = (x0 + x1) // 2
    # contact / pass / contact / pass
    bob = [3, 0, 3, 0][frame]
    if facing == "side":
        ldx, rdx, ldy, rdy = [
            (7, -6, 5, 0),
            (2, 2, 0, 3),
            (-6, 7, 0, 5),
            (2, 2, 3, 0),
        ][frame]
        sway = [3, 0, -3, 0][frame]
    else:
        ldx, rdx, ldy, rdy = [
            (6, -6, 4, 0),
            (0, 0, 0, 2),
            (-6, 6, 0, 4),
            (0, 0, 2, 0),
        ][frame]
        sway = [2, 0, -2, 0][frame]

    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    torso = base.crop((0, 0, W, min(H, hip + 10)))
    _paste(canvas, torso, (sway, bob))

    left = base.crop((0, hip, cx + 4, H))
    right = base.crop((cx - 4, hip, W, H))
    _paste(canvas, left, (ldx, hip + ldy + bob))
    _paste(canvas, right, (cx - 4 + rdx, hip + rdy + bob))
    return canvas


def rotate_about(im: Image.Image, angle: float, cx: float, cy: float) -> Image.Image:
    rot = im.rotate(angle, resample=Image.Resampling.BICUBIC, center=(cx, cy), fillcolor=(0, 0, 0, 0))
    return rot


def attack_from_static(base: Image.Image, frame: int, kind: str) -> Image.Image:
    bbox = content_bbox(base) or (20, 8, W - 20, H - 4)
    x0, y0, x1, y1 = bbox
    hip_x = (x0 + x1) / 2
    hip_y = y0 + (y1 - y0) * 0.62
    canvas = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas, "RGBA")

    if kind == "slash":
        angles = [-16, 4, 22, 8]
        shifts = [(-10, 4), (6, 2), (16, 6), (8, 4)]
        body = rotate_about(base, angles[frame], hip_x, hip_y)
        dx, dy = shifts[frame]
        _paste(canvas, body, (dx, dy))
        if frame in (1, 2):
            alpha = 90 if frame == 1 else 150
            draw.arc(
                [hip_x + 10 + dx, hip_y - 90 + dy, hip_x + 118 + dx, hip_y + 40 + dy],
                start=300,
                end=40,
                fill=(230, 240, 255, alpha),
                width=6 if frame == 2 else 4,
            )
        if frame == 2:
            draw.arc(
                [hip_x + 18 + dx, hip_y - 78 + dy, hip_x + 128 + dx, hip_y + 28 + dy],
                start=310,
                end=30,
                fill=(255, 255, 255, 180),
                width=3,
            )
        return canvas

    if kind == "bow":
        scales = [1.0, 0.92, 1.04, 1.0]
        shifts = [(-4, 2), (-12, 4), (10, 2), (2, 1)]
        s = scales[frame]
        nw, nh = max(1, int(W * s)), H
        scaled = base.resize((nw, nh), Image.Resampling.BICUBIC)
        dx, dy = shifts[frame]
        _paste(canvas, scaled, (dx + (W - nw) // 2, dy))
        if frame == 1:
            draw.line([(hip_x - 18 + dx, hip_y - 18 + dy), (hip_x + 48 + dx, hip_y - 8 + dy)], fill=(90, 60, 30, 220), width=2)
        if frame == 2:
            draw.line([(hip_x + 20 + dx, hip_y - 12 + dy), (hip_x + 90 + dx, hip_y - 10 + dy)], fill=(240, 240, 230, 200), width=2)
        return canvas

    # magic
    lifts = [(-2, -6), (0, -12), (10, -4), (4, -2)]
    dx, dy = lifts[frame]
    glow = base
    if frame in (1, 2):
        glow = ImageEnhance.Brightness(base).enhance(1.12 if frame == 1 else 1.22)
    _paste(canvas, glow, (dx, dy))
    orb_r = [8, 14, 22, 10][frame]
    ox, oy = hip_x + 42 + dx, y0 + 36 + dy
    col = [(120, 160, 255, 80), (140, 120, 255, 120), (210, 180, 255, 170), (160, 140, 255, 70)][frame]
    draw.ellipse([ox - orb_r, oy - orb_r, ox + orb_r, oy + orb_r], fill=col)
    if frame == 2:
        draw.ellipse([ox + 8, oy - 8, ox + 40, oy + 24], fill=(255, 255, 255, 90))
        for i in range(6):
            ang = i * math.pi / 3
            px = ox + int(math.cos(ang) * 34)
            py = oy + int(math.sin(ang) * 18)
            draw.ellipse([px - 3, py - 3, px + 3, py + 3], fill=(230, 210, 255, 160))
    return canvas


def synthesize_missing() -> None:
    """Rank 1–3 walk/attack + any rank-0 set the strips missed."""
    for job in JOBS:
        atk = ATTACK[job]
        ranks = [0, 1, 2, 3]
        for rank in ranks:
            prefix = "" if (job == "warrior" and rank <= 0) else (
                job if rank <= 0 else f"{job}_r{rank}"
            )

            def key(suffix: str) -> str:
                return suffix if prefix == "" else f"{prefix}_{suffix}"

            for facing, suffix in (("side", "walk_side"), ("front", "walk_down"), ("back", "walk_up")):
                out0 = FRAMES / f"{key(suffix)}_0.png"
                if out0.exists():
                    continue
                base = load_static(job, rank, facing)
                if base is None:
                    print("skip missing static", job, rank, facing)
                    continue
                frames = [walk_from_static(base, i, "side" if facing == "side" else "down") for i in range(4)]
                save_set(key(suffix), frames)

            out0 = FRAMES / f"{key(atk)}_0.png"
            if out0.exists():
                continue
            base = load_static(job, rank, "side")
            if base is None:
                print("skip missing attack static", job, rank)
                continue
            frames = [attack_from_static(base, i, atk) for i in range(4)]
            save_set(key(atk), frames)


def main() -> None:
    FRAMES.mkdir(parents=True, exist_ok=True)
    for set_name, filename in GENERATED_STRIPS:
        path = ART / filename
        if not path.exists():
            print("missing strip", path)
            continue
        frames = split_strip(path)
        nonempty = sum(1 for fr in frames if content_bbox(fr))
        print(f"strip {filename} -> {set_name} nonempty={nonempty}")
        if nonempty < 3:
            print("  WARN: skip weak slice")
            continue
        save_set(set_name, frames)
    synthesize_missing()


if __name__ == "__main__":
    main()
