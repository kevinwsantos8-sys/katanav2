package com.shadowkatana.network;

import com.shadowkatana.katana.KatanaLogic;
import com.shadowkatana.katana.KatanaUpgrades;
import com.shadowkatana.katana.ShadowPowers;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ToggleDrawPayload.ID, ToggleDrawPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpgradePayload.ID, UpgradePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PowerPayload.ID, PowerPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ToggleDrawPayload.ID,
                (payload, context) -> KatanaLogic.onToggle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(UpgradePayload.ID,
                (payload, context) -> KatanaUpgrades.tryUpgrade(context.player(), payload.type()));
        ServerPlayNetworking.registerGlobalReceiver(PowerPayload.ID,
                (payload, context) -> ShadowPowers.use(context.player(), payload.power()));
    }

    private ModNetworking() {}
}
