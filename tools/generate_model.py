"""Gera a Katana Sombria: textura PNG, modelo Java (item json) e arquivo .bbmodel do Blockbench.
Uso: python3 tools/generate_model.py   (precisa de Pillow)"""
import json, uuid, base64, io, random
from PIL import Image

random.seed(7)
RES = 32  # textura 32x32
img = Image.new("RGBA", (RES, RES), (0, 0, 0, 255))
px = img.load()

# --- Regioes da textura (pixels) ---
BLADE = (0, 0, 8, 32)      # lamina (face plana)
SPINE = (8, 0, 10, 32)     # lados/dorso
HANDLE = (10, 0, 18, 32)   # cabo enrolado
METAL = (18, 0, 26, 8)     # guarda, habaki, pomo

for y in range(32):
    for x in range(0, 8):  # lamina: fio mais claro na esquerda, hamon sutil
        base = (14, 14, 19)
        if x == 0: base = (70, 68, 88)
        elif x == 1: base = (42, 40, 56)
        elif x == 2: base = (26, 24, 34)
        elif x == 7: base = (9, 9, 12)
        n = random.randint(-2, 2)
        px[x, y] = (base[0]+n, base[1]+n, base[2]+n, 255)
for y in range(32):
    for x in range(8, 10): px[x, y] = (8, 8, 11, 255)
for y in range(32):  # cabo: padrao diagonal de tsuka-ito
    for x in range(10, 18):
        on = ((x + y) % 4 == 0) or ((x - y) % 4 == 0)
        px[x, y] = (30, 28, 38, 255) if on else (7, 7, 9, 255)
for y in range(8):
    for x in range(18, 26):
        n = random.randint(-3, 3)
        c = 16 + n
        if random.random() < 0.08: c = 44
        px[x, y] = (c, c, c + 5, 255)

png_path = "src/main/resources/assets/shadowkatana/textures/item/shadow_katana.png"
img.save(png_path)
buf = io.BytesIO(); img.save(buf, "PNG")
b64 = base64.b64encode(buf.getvalue()).decode()

# --- Elementos (coordenadas em unidades de modelo; lamina para cima no eixo Y) ---
E = [
 ("lamina",      [7.3, 6.6, 7.7], [8.7, 27.0, 8.3], BLADE),
 ("dorso",       [8.6, 6.6, 7.5], [8.95, 27.0, 8.5], SPINE),
 ("ponta_1",     [7.7, 27.0, 7.8], [8.95, 28.6, 8.2], BLADE),
 ("ponta_2",     [8.4, 28.6, 7.9], [8.95, 29.6, 8.1], BLADE),
 ("habaki",      [7.0, 5.7, 7.4], [9.0, 6.6, 8.6], METAL),
 ("tsuba",       [6.0, 5.0, 6.0], [10.0, 5.7, 10.0], METAL),
 ("cabo",        [7.3, -3.0, 7.3], [8.7, 5.0, 8.7], HANDLE),
 ("pomo",        [7.0, -3.9, 7.0], [9.0, -3.0, 9.0], METAL),
]
FACES = ["north", "east", "south", "west", "up", "down"]

def uv_for(region, scale):
    x0, y0, x1, y1 = region
    return [x0*scale, y0*scale, x1*scale, y1*scale]

# --- Item model JSON (Minecraft Java) ---
elements = []
for name, f, t, reg in E:
    elements.append({"name": name, "from": f, "to": t,
        "faces": {face: {"uv": uv_for(reg, 16/RES), "texture": "#0"} for face in FACES}})
display = {
 "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 3.5, 0.5], "scale": [0.6, 0.6, 0.6]},
 "thirdperson_lefthand":  {"rotation": [0, 90, -55], "translation": [0, 3.5, 0.5], "scale": [0.6, 0.6, 0.6]},
 "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.1, 3.0, 1.0], "scale": [0.5, 0.5, 0.5]},
 "firstperson_lefthand":  {"rotation": [0, 90, -25], "translation": [1.1, 3.0, 1.0], "scale": [0.5, 0.5, 0.5]},
 "gui":    {"rotation": [0, 0, 45], "translation": [0, 0, 0], "scale": [0.55, 0.55, 0.55]},
 "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
 "fixed":  {"rotation": [0, 0, 45], "translation": [0, 0, 0], "scale": [0.55, 0.55, 0.55]},
}
model = {"credit": "Shadow Katana", "texture_size": [RES, RES],
         "textures": {"0": "shadowkatana:item/shadow_katana", "particle": "shadowkatana:item/shadow_katana"},
         "elements": elements, "display": display}
json.dump(model, open("src/main/resources/assets/shadowkatana/models/item/shadow_katana.json", "w"), indent=2)

# --- Arquivo .bbmodel (Blockbench) ---
bb_elems, outliner = [], []
for name, f, t, reg in E:
    u = str(uuid.uuid4()); outliner.append(u)
    bb_elems.append({"name": name, "box_uv": False, "rescale": False, "locked": False,
        "light_emission": 0, "render_order": "default", "allow_mirror_modeling": True,
        "from": f, "to": t, "autouv": 0, "color": 0, "origin": [8, 0, 8],
        "faces": {face: {"uv": uv_for(reg, 1), "texture": 0} for face in FACES},
        "type": "cube", "uuid": u})
bb = {"meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
      "name": "shadow_katana", "model_identifier": "", "visible_box": [1, 1, 0],
      "variable_placeholders": "", "variable_placeholder_buttons": [],
      "timeline_setups": [], "unhandled_marker_colors": [],
      "resolution": {"width": RES, "height": RES}, "elements": bb_elems,
      "outliner": outliner, "display": display,
      "textures": [{"path": "", "name": "shadow_katana.png", "folder": "", "namespace": "",
        "id": "0", "particle": True, "render_mode": "default", "render_sides": "auto",
        "frame_time": 1, "frame_order_type": "loop", "frame_order": [], "frame_interpolate": False,
        "visible": True, "internal": True, "saved": False, "uuid": str(uuid.uuid4()),
        "source": "data:image/png;base64," + b64}]}
json.dump(bb, open("shadow_katana.bbmodel", "w"), indent=2)
print("OK: textura, item json e .bbmodel gerados")
