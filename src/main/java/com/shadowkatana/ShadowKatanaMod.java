package com.shadowkatana;

import com.shadowkatana.item.ModItems;
import com.shadowkatana.katana.KatanaLogic;
import com.shadowkatana.katana.KatanaUpgrades;
import com.shadowkatana.katana.ModAttachments;
import com.shadowkatana.katana.ShadowPowers;
import com.shadowkatana.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShadowKatanaMod implements ModInitializer {
    public static final String MOD_ID = "shadowkatana";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModSounds.init();
        ModItems.register();
        ModAttachments.init();
        ModNetworking.register();
        ShadowPowers.register();
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerManager().getPlayerList().forEach(p -> {
                    KatanaLogic.tick(p);
                    ShadowPowers.tick(p);
                    KatanaUpgrades.tickBonus(p);
                }));
        LOGGER.info("Shadow Katana carregada.");
    }
}
