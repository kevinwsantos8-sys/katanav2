"""Gera os sons da katana (ogg mono 44.1kHz) em assets/shadowkatana/sounds/. Requer numpy, scipy e ffmpeg."""
import subprocess, os
import numpy as np
from scipy.signal import butter, sosfilt

SR = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "shadowkatana", "sounds")
rng = np.random.default_rng(7)

def t_(d): return np.linspace(0, d, int(SR * d), endpoint=False)

def noise(d): return rng.standard_normal(int(SR * d))

def bp(x, lo, hi):
    return sosfilt(butter(4, [lo, hi], btype="band", fs=SR, output="sos"), x)

def lp(x, f): return sosfilt(butter(2, f, btype="low", fs=SR, output="sos"), x)

def ring(d, freqs, decay):
    t = t_(d)
    y = np.zeros_like(t)
    for i, f in enumerate(freqs):
        y += np.sin(2 * np.pi * f * t + i) * np.exp(-t * decay * (1 + 0.35 * i)) / (1 + 0.5 * i)
    return y

def norm(x, peak=0.9):
    x = x / (np.max(np.abs(x)) + 1e-9)
    fade = int(SR * 0.01)
    x[-fade:] *= np.linspace(1, 0, fade)
    return x * peak

def save(name, x):
    wav = os.path.join(OUT, name + ".wav")
    ogg = os.path.join(OUT, name + ".ogg")
    import scipy.io.wavfile as w
    w.write(wav, SR, (x * 32767).astype(np.int16))
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "5", ogg], check=True)
    os.remove(wav)

# SACAR: "shiiing" metalico - raspado agudo que sobe + toque de metal
d = 0.75
t = t_(d)
scrape = bp(noise(d), 2500, 9000) * np.exp(-t * 3.2) * np.clip(t / 0.05, 0, 1)
sweep = np.sin(2 * np.pi * (2800 * t + 2600 * t ** 2)) * np.exp(-t * 5) * 0.35
metal = ring(d, [2100, 3340, 4870, 6120], 6) * 0.45
draw = scrape * 0.9 + sweep + metal
save("katana_draw", norm(draw))

# CORTE NO AR: whoosh curto
d = 0.35
t = t_(d)
env = np.sin(np.pi * np.clip(t / d, 0, 1)) ** 2
whoosh = bp(noise(d), 500, 3500) * env
whoosh += lp(noise(d), 400) * env * 0.6
save("katana_swish", norm(whoosh, 0.8))

# GUARDAR: raspado curto descendo + "clack" seco da bainha
d = 0.6
t = t_(d)
scr = bp(noise(d), 1800, 6000) * np.exp(-t * 9) * np.clip(t / 0.03, 0, 1) * 0.5
clack_t = int(SR * 0.28)
click = np.zeros_like(t)
n = bp(noise(0.06), 800, 5000) * np.exp(-t_(0.06) * 70)
click[clack_t:clack_t + len(n)] += n * 1.2
thunk = np.zeros_like(t)
tt = t_(0.2)
th = np.sin(2 * np.pi * 150 * tt) * np.exp(-tt * 30)
thunk[clack_t:clack_t + len(th)] += th * 0.9
tail = np.zeros_like(t)
r = ring(0.3, [1900, 3100], 18) * 0.25
tail[clack_t:clack_t + len(r)] += r[: len(tail) - clack_t]
save("katana_sheathe", norm(scr + click + thunk + tail))
print("ok")
