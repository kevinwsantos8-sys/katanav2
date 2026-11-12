package com.shadowkatana;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/** Sons proprios da katana (arquivos em assets/shadowkatana/sounds, gerados por tools/generate_sounds.py). */
public final class ModSounds {
    public static final SoundEvent KATANA_DRAW = register("katana_draw");
    public static final SoundEvent KATANA_SWISH = register("katana_swish");
    public static final SoundEvent KATANA_SHEATHE = register("katana_sheathe");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(ShadowKatanaMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    /** Chamado no onInitialize para garantir o carregamento da classe. */
    public static void init() {}

    private ModSounds() {}
}
