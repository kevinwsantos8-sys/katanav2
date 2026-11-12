package com.shadowkatana.client.screen;

import com.shadowkatana.client.ShadowKatanaClient;
import com.shadowkatana.katana.KatanaUpgrades;
import com.shadowkatana.network.UpgradePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;

/** Menu da katana (tecla K): comprar niveis de dano com niveis de XP. */
public class KatanaUpgradeScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 200;
    private static final int ROW_H = 48;

    private final ButtonWidget[] buttons = new ButtonWidget[3];
    private int left, top;

    public KatanaUpgradeScreen() {
        super(Text.translatable("screen.shadowkatana.title"));
    }

    @Override
    protected void init() {
        left = (this.width - PANEL_W) / 2;
        top = (this.height - PANEL_H) / 2;
        for (int i = 0; i < 3; i++) {
            final int id = i;
            buttons[i] = ButtonWidget.builder(Text.empty(), b ->
                            ClientPlayNetworking.send(new UpgradePayload(id)))
                    .dimensions(left + PANEL_W - 12 - 110, top + 34 + i * ROW_H + 6, 110, 20)
                    .build();
            addDrawableChild(buttons[i]);
        }
    }

    private double valueOf(int id, int level) {
        return switch (id) {
            case KatanaUpgrades.SWORD -> KatanaUpgrades.swordBonus(level);
            case KatanaUpgrades.TELEPORT -> KatanaUpgrades.teleportDamage(level);
            default -> KatanaUpgrades.shadowStrikeBonus(level);
        };
    }

    private static String effectKey(int id) {
        return switch (id) {
            case KatanaUpgrades.SWORD -> "screen.shadowkatana.effect.sword";
            case KatanaUpgrades.TELEPORT -> "screen.shadowkatana.effect.teleport";
            default -> "screen.shadowkatana.effect.shadow";
        };
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.fill(left, top, left + PANEL_W, top + PANEL_H, 0xE0101014);
        context.drawBorder(left, top, PANEL_W, PANEL_H, 0xFF5A3A8C);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClientPlayerEntity player = this.client != null ? this.client.player : null;
        int xp = player != null ? player.experienceLevel : 0;
        boolean creative = player != null && player.isCreative();

        // atualiza botoes antes de desenhar
        for (int i = 0; i < 3; i++) {
            int level = player != null ? KatanaUpgrades.get(player, i) : 0;
            boolean max = level >= KatanaUpgrades.MAX_LEVEL;
            int cost = KatanaUpgrades.cost(level);
            buttons[i].setMessage(max
                    ? Text.translatable("screen.shadowkatana.max")
                    : Text.translatable("screen.shadowkatana.upgrade", cost));
            buttons[i].active = !max && (creative || xp >= cost);
        }

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, top + 10, 0xFFB388FF);

        for (int i = 0; i < 3; i++) {
            int level = player != null ? KatanaUpgrades.get(player, i) : 0;
            int y = top + 34 + i * ROW_H;
            context.drawTextWithShadow(this.textRenderer,
                    Text.translatable("screen.shadowkatana.level_line",
                            Text.translatable(KatanaUpgrades.nameKey(i)), level, KatanaUpgrades.MAX_LEVEL),
                    left + 12, y + 2, 0xFFFFFFFF);

            String now = String.format(Locale.ROOT, "%.1f", valueOf(i, level));
            String next = String.format(Locale.ROOT, "%.1f", valueOf(i, Math.min(level + 1, KatanaUpgrades.MAX_LEVEL)));
            context.drawTextWithShadow(this.textRenderer,
                    Text.translatable(effectKey(i), now, next),
                    left + 12, y + 14, 0xFFAAAAAA);
        }

        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("screen.shadowkatana.xp", xp),
                left + 12, top + PANEL_H - 16, 0xFF80FF20);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ShadowKatanaClient.MENU_KEY != null && ShadowKatanaClient.MENU_KEY.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
