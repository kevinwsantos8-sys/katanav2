package com.shadowkatana.client;

import com.shadowkatana.katana.KatanaPhase;
import com.shadowkatana.katana.ModAttachments;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Mostra a recarga dos poderes (Z / X) no canto da tela enquanto a katana esta na mao. */
public final class ShadowHud {
    private static final Identifier ID = Identifier.of("shadowkatana", "cooldowns");

    public static void register() {
        HudElementRegistry.addLast(ID, ShadowHud::render);
    }

    private static void render(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        if (mc.player.getAttachedOrElse(ModAttachments.PHASE, KatanaPhase.NONE) != KatanaPhase.DRAWN) return;

        long now = mc.world.getTime();
        long tp = mc.player.getAttachedOrElse(ModAttachments.TELEPORT_READY_AT, 0L) - now;
        long sh = mc.player.getAttachedOrElse(ModAttachments.SHADOW_READY_AT, 0L) - now;

        int y = mc.getWindow().getScaledHeight() - 52;
        line(ctx, mc, "Z", "hud.shadowkatana.teleport", tp, 8, y);
        line(ctx, mc, "X", "hud.shadowkatana.shadow", sh, 8, y + 11);
    }

    private static void line(DrawContext ctx, MinecraftClient mc, String key, String nameKey, long ticksLeft, int x, int y) {
        Text status = ticksLeft <= 0
                ? Text.translatable("hud.shadowkatana.ready")
                : Text.translatable("hud.shadowkatana.cooldown", (ticksLeft + 19) / 20);
        int color = ticksLeft <= 0 ? 0xFFB388FF : 0xFF888888;
        ctx.drawTextWithShadow(mc.textRenderer,
                Text.literal("[" + key + "] ").append(Text.translatable(nameKey)).append(": ").append(status),
                x, y, color);
    }

    private ShadowHud() {}
}
