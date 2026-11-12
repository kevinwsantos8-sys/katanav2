package com.shadowkatana.network;

import com.shadowkatana.ShadowKatanaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Cliente -> servidor: comprar 1 nivel do upgrade `type` (0 espada, 1 teleporte, 2 sombra). */
public record UpgradePayload(int type) implements CustomPayload {
    public static final CustomPayload.Id<UpgradePayload> ID =
            new CustomPayload.Id<>(Identifier.of(ShadowKatanaMod.MOD_ID, "upgrade"));
    public static final PacketCodec<ByteBuf, UpgradePayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, UpgradePayload::type, UpgradePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
