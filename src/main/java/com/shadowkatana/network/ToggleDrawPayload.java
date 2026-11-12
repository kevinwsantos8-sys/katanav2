package com.shadowkatana.network;

import com.shadowkatana.ShadowKatanaMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Cliente -> servidor: o jogador apertou a tecla de sacar/guardar. */
public record ToggleDrawPayload() implements CustomPayload {
    public static final CustomPayload.Id<ToggleDrawPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ShadowKatanaMod.MOD_ID, "toggle_draw"));
    public static final PacketCodec<RegistryByteBuf, ToggleDrawPayload> CODEC =
            PacketCodec.unit(new ToggleDrawPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
