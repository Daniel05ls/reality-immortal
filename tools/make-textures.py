"""God-set procedural pixel art for reality-immortal.

Generates the same files as tools/make-textures.ps1 (which mirrors this
script for Windows users): 32px item sprites, a 4-frame animated sword
and standard 64x32 armor UVs. Run from the repository root:

    python3 tools/make-textures.py

Requires Pillow: pip install pillow
"""
from pathlib import Path

from PIL import Image, ImageDraw

DEST = Path(__file__).resolve().parents[1] / "src/main/resources/assets/realityimmortal/textures"

# Divine gold-and-white palette with a celestial glow.
PAL = {
    "O": "#2E2000",  # near-black bronze outline
    "D": "#8A5A00",  # dark bronze shading
    "M": "#C9920E",  # mid gold
    "G": "#FFC93C",  # bright gold, primary metal
    "L": "#FFE9A8",  # pale radiant gold highlight
    "W": "#FFFFFF",  # pure holy white
    "S": "#E9EEF5",  # platinum silver
    "C": "#8FD8FF",  # celestial sky-blue glow
    "V": "#FF9D2E",  # radiant amber glow
}


def col(key):
    h = PAL[key].lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def rect(draw, x, y, w, h, key):
    draw.rectangle([x, y, x + w - 1, y + h - 1], fill=col(key))


def poly(draw, points, key):
    draw.polygon(points, fill=col(key))


def sword():
    """32x128 strip of 4 frames: radiant blade, winged guard, pulsing tip."""
    strip = Image.new("RGBA", (32, 128), (0, 0, 0, 0))
    for f in range(4):
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        # Grip: dark bronze with gold rings, golden pommel with white gem.
        poly(d, [(4, 29), (2, 27), (10, 19), (12, 21)], "D")
        rect(d, 4, 26, 2, 2, "G")
        rect(d, 7, 23, 2, 2, "G")
        rect(d, 2, 27, 3, 3, "G")
        rect(d, 3, 28, 1, 1, "W")
        # Blade: bronze outline, gold edge, pale core, white-hot center.
        poly(d, [(7, 19), (22, 4), (30, 0), (29, 9), (14, 24)], "O")
        poly(d, [(8, 18), (23, 4), (29, 2), (28, 8), (14, 22)], "G")
        poly(d, [(10, 17), (24, 5), (28, 3), (26, 8), (15, 21)], "L")
        poly(d, [(12, 16), (25, 5), (26, 6), (24, 10), (16, 19)], "W")
        # Celestial rune line down the middle of the blade.
        poly(d, [(14, 16), (24, 6), (25, 7), (15, 17)], "C")
        # Sparkles travelling along the blade, one per frame step.
        for k in range(10):
            if (k + f) % 4 == 0:
                rect(d, 14 + k, 17 - k, 2, 1, "W")
        # Pulsing star at the tip.
        if f % 2 == 0:
            rect(d, 28, 1, 1, 3, "W")
            rect(d, 27, 2, 3, 1, "W")
        else:
            rect(d, 28, 2, 1, 1, "W")
        # Winged golden guard.
        poly(d, [(4, 16), (7, 13), (18, 24), (17, 28), (13, 26), (12, 22), (9, 19), (5, 19)], "O")
        poly(d, [(5, 16), (8, 15), (16, 23), (17, 26), (14, 24), (11, 21), (7, 18)], "G")
        poly(d, [(6, 16), (8, 16), (14, 22), (15, 24), (13, 23), (10, 20), (7, 17)], "L")
        rect(d, 6, 15, 2, 1, "W")
        rect(d, 10, 19, 3, 3, "C")
        rect(d, 11, 20, 1, 1, "W")
        strip.paste(img, (0, 32 * f))
    return strip


def helmet():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    poly(d, [(5, 24), (5, 10), (9, 5), (22, 5), (26, 10), (26, 24), (21, 27), (20, 18), (11, 18), (10, 27)], "O")
    poly(d, [(7, 22), (7, 11), (10, 7), (21, 7), (24, 11), (24, 23), (22, 24), (21, 16), (10, 16), (9, 24)], "G")
    # Crown spikes with white tips.
    rect(d, 10, 3, 2, 3, "G")
    rect(d, 15, 2, 2, 4, "G")
    rect(d, 20, 3, 2, 3, "G")
    rect(d, 10, 3, 2, 1, "W")
    rect(d, 15, 2, 2, 1, "W")
    rect(d, 20, 3, 2, 1, "W")
    # Top shine, brow band, glowing visor, platinum cheeks.
    rect(d, 9, 8, 13, 2, "L")
    rect(d, 9, 10, 14, 2, "M")
    rect(d, 9, 14, 14, 2, "C")
    rect(d, 11, 14, 10, 1, "W")
    rect(d, 7, 17, 3, 6, "S")
    rect(d, 22, 17, 3, 6, "S")
    rect(d, 6, 9, 1, 12, "L")
    rect(d, 25, 9, 1, 12, "L")
    return img


def chestplate():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    poly(d, [(3, 8), (10, 5), (13, 8), (18, 8), (21, 5), (28, 8), (30, 16), (25, 18), (24, 27), (7, 27), (6, 18), (1, 16)], "O")
    poly(d, [(4, 9), (9, 7), (12, 10), (19, 10), (22, 7), (27, 9), (28, 14), (23, 16), (22, 25), (9, 25), (8, 16), (3, 14)], "G")
    rect(d, 10, 12, 12, 12, "M")
    # Platinum shoulder wings.
    poly(d, [(9, 12), (15, 16), (15, 18), (9, 15)], "S")
    poly(d, [(22, 12), (16, 16), (16, 18), (22, 15)], "S")
    # Radiant sun emblem: bright white core, eight amber rays.
    rect(d, 15, 11, 2, 3, "V")
    rect(d, 15, 18, 2, 3, "V")
    rect(d, 11, 15, 3, 2, "V")
    rect(d, 18, 15, 3, 2, "V")
    rect(d, 12, 12, 2, 2, "V")
    rect(d, 18, 12, 2, 2, "V")
    rect(d, 12, 18, 2, 2, "V")
    rect(d, 18, 18, 2, 2, "V")
    rect(d, 14, 14, 4, 4, "W")
    # Belt with golden buckle, trims.
    rect(d, 10, 23, 12, 2, "D")
    rect(d, 14, 23, 4, 2, "G")
    rect(d, 15, 23, 2, 2, "W")
    rect(d, 3, 10, 5, 2, "L")
    rect(d, 24, 10, 5, 2, "L")
    return img


def leggings():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rect(d, 7, 4, 18, 24, "O")
    rect(d, 9, 6, 14, 6, "G")
    rect(d, 9, 12, 6, 14, "M")
    rect(d, 17, 12, 6, 14, "M")
    for y in range(14, 29):
        for x in range(15, 17):
            img.putpixel((x, y), (0, 0, 0, 0))
    rect(d, 8, 6, 16, 2, "D")
    rect(d, 14, 5, 4, 4, "G")
    rect(d, 15, 6, 2, 2, "W")
    rect(d, 9, 13, 1, 12, "L")
    rect(d, 22, 13, 1, 12, "L")
    rect(d, 10, 13, 4, 1, "G")
    rect(d, 18, 13, 4, 1, "G")
    rect(d, 10, 20, 4, 3, "C")
    rect(d, 18, 20, 4, 3, "C")
    rect(d, 11, 21, 2, 1, "W")
    rect(d, 19, 21, 2, 1, "W")
    return img


def boots():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rect(d, 5, 8, 9, 19, "O")
    rect(d, 18, 8, 9, 19, "O")
    rect(d, 3, 21, 11, 6, "O")
    rect(d, 18, 21, 11, 6, "O")
    rect(d, 7, 10, 5, 12, "G")
    rect(d, 20, 10, 5, 12, "G")
    rect(d, 5, 22, 7, 3, "M")
    rect(d, 20, 22, 7, 3, "M")
    rect(d, 6, 10, 7, 2, "L")
    rect(d, 19, 10, 7, 2, "L")
    # Platinum wing accents, celestial ankle gems, dark soles.
    rect(d, 4, 13, 2, 4, "S")
    rect(d, 26, 13, 2, 4, "S")
    rect(d, 4, 12, 1, 2, "W")
    rect(d, 27, 12, 1, 2, "W")
    rect(d, 9, 14, 2, 2, "C")
    rect(d, 21, 14, 2, 2, "C")
    rect(d, 4, 24, 9, 1, "D")
    rect(d, 19, 24, 9, 1, "D")
    return img


def armor_layer(layer):
    """64x32 vanilla humanoid armor UVs: bright gold metal, sun emblem."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for (x, y, w, h) in [(0, 0, 32, 16), (0, 16, 16, 16), (16, 16, 24, 16), (40, 16, 16, 16)]:
        rect(d, x, y, w, h, "G")
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                if (xx + 2 * yy) % 7 == 0:
                    rect(d, xx, yy, 1, 1, "M")
    # Helmet front (8,8) 8x8: crown band, visor, cheek plates.
    rect(d, 8, 8, 8, 1, "L")
    rect(d, 8, 10, 8, 1, "M")
    rect(d, 8, 11, 8, 1, "C")
    rect(d, 10, 11, 4, 1, "W")
    rect(d, 10, 12, 4, 3, "D")
    rect(d, 8, 12, 1, 4, "S")
    rect(d, 15, 12, 1, 4, "S")
    rect(d, 11, 8, 2, 2, "W")
    rect(d, 0, 11, 8, 1, "C")
    rect(d, 16, 11, 16, 1, "C")
    # Chest front (20,20) 8x12, back (32,20) 8x12: sun emblem.
    rect(d, 20, 20, 8, 2, "M")
    rect(d, 20, 22, 2, 2, "C")
    rect(d, 26, 22, 2, 2, "C")
    rect(d, 23, 21, 2, 2, "V")
    rect(d, 23, 27, 2, 2, "V")
    rect(d, 20, 24, 2, 2, "V")
    rect(d, 26, 24, 2, 2, "V")
    rect(d, 21, 22, 2, 1, "V")
    rect(d, 25, 22, 2, 1, "V")
    rect(d, 21, 26, 2, 1, "V")
    rect(d, 25, 26, 2, 1, "V")
    rect(d, 22, 23, 4, 4, "W")
    rect(d, 20, 29, 8, 1, "D")
    rect(d, 20, 31, 8, 1, "L")
    rect(d, 35, 21, 2, 8, "M")
    rect(d, 35, 23, 2, 4, "C")
    rect(d, 32, 29, 8, 1, "D")
    # Arms and boots/leggings use shared 4x12 front UVs.
    rect(d, 44, 20, 4, 3, "M")
    rect(d, 44, 20, 4, 1, "L")
    rect(d, 44, 27, 4, 1, "D")
    rect(d, 45, 23, 1, 4, "V")
    rect(d, 4, 20, 4, 1, "L")
    rect(d, 4, 21, 1, 9, "C")
    rect(d, 5, 25, 3, 2, "M")
    rect(d, 4, 30, 4, 1, "L")
    rect(d, 12, 30, 4, 1, "L")
    rect(d, 48, 27, 8, 1, "D")
    if layer == 2:
        rect(d, 20, 20, 8, 3, "M")
        rect(d, 20, 20, 8, 1, "L")
        rect(d, 23, 20, 2, 2, "C")
    return img


def main():
    (DEST / "item").mkdir(parents=True, exist_ok=True)
    (DEST / "models" / "armor").mkdir(parents=True, exist_ok=True)
    sword().save(DEST / "item" / "reality_sword.png")
    helmet().save(DEST / "item" / "immortal_helmet.png")
    chestplate().save(DEST / "item" / "immortal_chestplate.png")
    leggings().save(DEST / "item" / "immortal_leggings.png")
    boots().save(DEST / "item" / "immortal_boots.png")
    armor_layer(1).save(DEST / "models" / "armor" / "immortal_layer_1.png")
    armor_layer(2).save(DEST / "models" / "armor" / "immortal_layer_2.png")
    print("Wrote 7 god-set textures to", DEST)


if __name__ == "__main__":
    main()
