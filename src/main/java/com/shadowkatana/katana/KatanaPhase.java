package com.shadowkatana.katana;

/** Fases da katana de cada jogador (sincronizadas com os clientes). */
public final class KatanaPhase {
    public static final int NONE = 0;       // jogador nao tem katana
    public static final int SHEATHED = 1;   // katana guardada nas costas
    public static final int DRAWING = 2;    // animacao de sacar
    public static final int DRAWN = 3;      // katana na mao
    public static final int SHEATHING = 4;  // animacao de guardar

    public static final int DRAW_TICKS = 14;
    public static final int SHEATH_TICKS = 12;

    private KatanaPhase() {}
}
