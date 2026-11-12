package com.shadowkatana.katana;

import com.shadowkatana.ShadowKatanaMod;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

/** Dados guardados em cada jogador (Fabric Data Attachments). */
public final class ModAttachments {
    /** Fase atual da katana. Sincronizada com todos os clientes (para desenhar nas costas). */
    public static final AttachmentType<Integer> PHASE = AttachmentRegistry.<Integer>create(
            Identifier.of(ShadowKatanaMod.MOD_ID, "phase"),
            b -> b.initializer(() -> KatanaPhase.NONE)
                    .syncWith(PacketCodecs.VAR_INT, AttachmentSyncPredicate.all()));

    /** Tick do servidor em que a fase comecou (so servidor). */
    public static final AttachmentType<Long> START = AttachmentRegistry.<Long>create(
            Identifier.of(ShadowKatanaMod.MOD_ID, "start"),
            b -> b.initializer(() -> 0L));

    /** Slot da hotbar que estava selecionado antes de sacar (so servidor). */
    public static final AttachmentType<Integer> PREV_SLOT = AttachmentRegistry.<Integer>create(
            Identifier.of(ShadowKatanaMod.MOD_ID, "prev_slot"),
            b -> b.initializer(() -> 0));

    // ---- Upgrades (salvos no jogador, mantidos ao morrer, enviados so ao proprio jogador) ----
    public static final AttachmentType<Integer> SWORD_LEVEL = level("sword_level");
    public static final AttachmentType<Integer> TELEPORT_LEVEL = level("teleport_level");
    public static final AttachmentType<Integer> SHADOW_LEVEL = level("shadow_level");

    private static AttachmentType<Integer> level(String name) {
        return AttachmentRegistry.<Integer>create(
                Identifier.of(ShadowKatanaMod.MOD_ID, name),
                b -> b.initializer(() -> 0)
                        .persistent(Codec.INT)
                        .copyOnDeath()
                        .syncWith(PacketCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly()));
    }

    // ---- Poderes de Sombra (nao salvos; recargas sincronizadas com o dono para o HUD) ----
    public static final AttachmentType<Long> TELEPORT_READY_AT = AttachmentRegistry.<Long>create(
            Identifier.of(ShadowKatanaMod.MOD_ID, "teleport_ready_at"),
            b -> b.initializer(() -> 0L).syncWith(PacketCodecs.VAR_LONG, AttachmentSyncPredicate.targetOnly()));
    public static final AttachmentType<Long> SHADOW_READY_AT = AttachmentRegistry.<Long>create(
            Identifier.of(ShadowKatanaMod.MOD_ID, "shadow_ready_at"),
            b -> b.initializer(() -> 0L).syncWith(PacketCodecs.VAR_LONG, AttachmentSyncPredicate.targetOnly()));
    /** true enquanto o proximo golpe da katana causa dano bonus (golpe sombrio). */
    public static final AttachmentType<Boolean> STRIKE_ARMED = AttachmentRegistry.<Boolean>create(
            Identifier.of(ShadowKatanaMod.MOD_ID, "strike_armed"), b -> b.initializer(() -> false));

    public static void init() {
        // so para forcar o carregamento da classe
    }

    private ModAttachments() {}
}
