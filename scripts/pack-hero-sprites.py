#!/usr/bin/env python3
"""Build export/HeroClassSprites.zip (hires originals + in-game sprites)."""
from __future__ import annotations

import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HIRES = Path("/opt/cursor/artifacts/assets")
GAME = ROOT / "app/src/main/assets/custom"
SHEET = Path("/opt/cursor/artifacts/job_advance_designs.png")
OUT = ROOT / "export/HeroClassSprites.zip"

README = """Hero class sprite pack — Medieval Village
==========================================

Four starter jobs and three promotions each (levels 5 / 10 / 15).
Level 20 uses the same final-rank look (awakening).

Jobs
----
knight  기사 → 수호기사 → 성기사 → 천상의 성벽
warrior 용사 → 광전사 → 전쟁군주 → 종말의 검성
mage    마법사 → 원소술사 → 대마도사 → 세계의 아크메이지
archer  궁수 → 사냥꾼 → 저격수 → 별빛의 명사수

Folders
-------
hires/          Generated art ~1024×1536 RGB (white background). Best for other games.
game_175x286/   In-game sprites 175×286 RGBA (transparent).
overview.png    Contact sheet of all ranks (front).

File names (hires)
------------------
hero_{job}_{front|back|side}.png              starter
hero_{job}_r1_{front|back|side}.png           1st promotion (Lv.5)
hero_{job}_r2_{front|back|side}.png           2nd promotion (Lv.10)
hero_{job}_r3_{front|back|side}.png           3rd promotion (Lv.15)
warrior starter files are hero_front/back/side.png in game_175x286/ (no 1024 original).

License / reuse
---------------
Drawn for this project. You may reuse these images in your own games.
Please keep this README with the pack if you share it.

Source: https://github.com/Jongkyung-Ko/Game1
"""


def pack() -> None:
    OUT.parent.mkdir(parents=True, exist_ok=True)
    if OUT.exists():
        OUT.unlink()
    readme = ROOT / "export/.hero-pack-readme.txt"
    readme.write_text(README, encoding="utf-8")
    with zipfile.ZipFile(OUT, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as zf:
        zf.write(readme, "HeroClassSprites/README.txt")
        if SHEET.exists():
            zf.write(SHEET, "HeroClassSprites/overview.png")
        if HIRES.exists():
            for path in sorted(HIRES.glob("hero_*.png")):
                zf.write(path, f"HeroClassSprites/hires/{path.name}")
        for path in sorted(GAME.glob("hero_*.png")):
            zf.write(path, f"HeroClassSprites/game_175x286/{path.name}")
    readme.unlink(missing_ok=True)
    print("wrote", OUT, "bytes", OUT.stat().st_size)


if __name__ == "__main__":
    pack()
