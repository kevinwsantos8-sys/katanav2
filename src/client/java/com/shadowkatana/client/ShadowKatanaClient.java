package com.shadowkatana.client;

import com.shadowkatana.client.render.BackKatanaFeatureRenderer;
import com.shadowkatana.client.screen.KatanaUpgradeScreen;
import com.shadowkatana.katana.KatanaPhase;
import com.shadowkatana.katana.ModAttachments;
import net.minecraft.text.Text;
import com.shadowkatana.network.PowerPayload;
import com.shadowkatana.network.ToggleDrawPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ShadowKatanaClient implements ClientModInitializer {
    public static KeyBinding DRAW_KEY;
    public static KeyBinding MENU_KEY;
    public static KeyBinding TELEPORT_KEY;
    public static KeyBinding INVIS_KEY;

    @Override
    public void onInitializeClient() {
        // Tecla R: sacar / guardar a katana
        DRAW_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.draw", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.shadowkatana"));

        // Tecla K: menu de upgrades da katana
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.shadowkatana"));

        // Poderes de Sombra: Z = teleporte, X = invisibilidade
        TELEPORT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.teleport", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.shadowkatana"));
        INVIS_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shadowkatana.invisibility", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.shadowkatana"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TELEPORT_KEY.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    ClientPlayNetworking.send(new PowerPayload(0));
                }
            }
            while (INVIS_KEY.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    ClientPlayNetworking.send(new PowerPayload(1));
                }
            }
            while (DRAW_KEY.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    ClientPlayNetworking.send(new ToggleDrawPayload());
                }
            }
            while (MENU_KEY.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    int phase = client.player.getAttachedOrElse(ModAttachments.PHASE, KatanaPhase.NONE);
                    if (phase == KatanaPhase.NONE) {
                        client.player.sendMessage(Text.translatable("message.shadowkatana.no_katana"), true);
                    } else {
                        client.setScreen(new KatanaUpgradeScreen());
                    }
                }
            }
        });

        ShadowHud.register();

        // Desenha a katana nas costas de todos os jogadores
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                        registrationHelper.register(
                                new BackKatanaFeatureRenderer(playerRenderer, context.getItemModelManager()));
                    }
                });

    }
}
