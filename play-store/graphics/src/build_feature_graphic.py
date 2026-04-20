#!/usr/bin/env python3
"""Render the Google Play feature graphic (1024x500 PNG).

Inputs : play-store/graphics/src/icon_512.svg
Output : play-store/graphics/feature_graphic_1024x500.png

Requires: Pillow, cairosvg, a system DejaVu Sans font.
"""
from __future__ import annotations

import io
import os
import sys
from pathlib import Path

import cairosvg
from PIL import Image, ImageDraw, ImageFilter, ImageFont

SRC_DIR = Path(__file__).resolve().parent
GFX = SRC_DIR.parent
SRC_SVG = SRC_DIR / "icon_512.svg"
OUT_PNG = GFX / "feature_graphic_1024x500.png"

W, H = 1024, 500
FONT_BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
FONT_REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"


def render_icon(size: int) -> Image.Image:
    png_bytes = cairosvg.svg2png(
        bytestring=SRC_SVG.read_bytes(),
        output_width=size,
        output_height=size,
    )
    return Image.open(io.BytesIO(png_bytes)).convert("RGBA")


def make_background() -> Image.Image:
    bg = Image.new("RGB", (W, H), (0, 0, 0))
    px = bg.load()
    for y in range(H):
        for x in range(W):
            t = (x + y) / (W + H)
            px[x, y] = (
                int(0x1C * (1 - t)),
                int(0x19 * (1 - t)),
                int(0x17 * (1 - t)),
            )
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(glow)
    cx, cy = 220, H // 2
    steps, max_r = 80, 340
    for i in range(steps, 0, -1):
        r = int(max_r * i / steps)
        a = int(80 * (1 - i / steps) ** 1.6)
        d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(249, 115, 22, a))
    glow = glow.filter(ImageFilter.GaussianBlur(26))
    return Image.alpha_composite(bg.convert("RGBA"), glow)


def main() -> int:
    if not SRC_SVG.exists():
        print(f"missing {SRC_SVG}", file=sys.stderr)
        return 1

    canvas = make_background()
    icon = render_icon(340)
    ix = 220 - icon.size[0] // 2
    iy = H // 2 - icon.size[1] // 2
    canvas.alpha_composite(icon, (ix, iy))

    draw = ImageDraw.Draw(canvas)
    f_title = ImageFont.truetype(FONT_BOLD, 68)
    f_sub = ImageFont.truetype(FONT_REG, 28)
    f_tag = ImageFont.truetype(FONT_BOLD, 22)
    f_foot = ImageFont.truetype(FONT_REG, 20)

    tx = 430
    draw.text((tx, 120), "HealthJournal", font=f_title, fill=(255, 255, 255))
    draw.text((tx, 205), "AI symptom tracker & health diary", font=f_sub,
              fill=(253, 186, 116))

    x, y = tx, 285
    for label, color in [
        ("Local-first", (249, 115, 22)),
        ("AI report", (255, 255, 255)),
        ("Family profiles", (253, 186, 116)),
    ]:
        tw = draw.textlength(label, font=f_tag)
        pad_x, pad_y = 16, 9
        bw, bh = int(tw + pad_x * 2), 40
        draw.rounded_rectangle((x, y, x + bw, y + bh), radius=20,
                               outline=color, width=2)
        draw.text((x + pad_x, y + pad_y - 2), label, font=f_tag, fill=color)
        x += bw + 14

    draw.text((tx, 385), "No account  •  No ads  •  HTTPS only",
              font=f_foot, fill=(168, 162, 158))

    OUT_PNG.parent.mkdir(parents=True, exist_ok=True)
    canvas.convert("RGB").save(OUT_PNG, "PNG", optimize=True)
    print(f"wrote {OUT_PNG} ({os.path.getsize(OUT_PNG)} bytes)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
