package com.shadowkatana.network;

import com.shadowkatana.ShadowKatanaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Cliente -> servidor: usar um poder (0 = teleporte, 1 = invisibilidade). */
public record PowerPayload(int power) implements CustomPayload {
    public static final CustomPayload.Id<PowerPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ShadowKatanaMod.MOD_ID, "power"));
    public static final PacketCodec<ByteBuf, PowerPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, PowerPayload::power, PowerPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
