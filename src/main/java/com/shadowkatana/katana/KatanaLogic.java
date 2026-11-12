package com.shadowkatana.katana;

import com.shadowkatana.ModSounds;
import com.shadowkatana.item.ModItems;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/**
 * Logica do servidor: sacar/guardar a katana.
 * Ideia: a katana fica na hotbar. "Sacar" = (apos a animacao) selecionar o slot dela.
 * "Guardar" = voltar para o slot que estava selecionado antes.
 */
public final class KatanaLogic {

    /** Chamado quando o jogador aperta R. */
    public static void onToggle(ServerPlayerEntity p) {
        int phase = p.getAttachedOrElse(ModAttachments.PHASE, KatanaPhase.NONE);
        long now = p.getWorld().getTime();

        switch (phase) {
            case KatanaPhase.SHEATHED -> {
                if (findHotbarSlot(p) < 0) {
                    p.sendMessage(Text.translatable("message.shadowkatana.not_in_hotbar"), true);
                    return;
                }
                p.setAttached(ModAttachments.PREV_SLOT, p.getInventory().getSelectedSlot());
                setPhase(p, KatanaPhase.DRAWING, now);
                p.getWorld().playSound(null, p.getBlockPos(),
                        SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), SoundCategory.PLAYERS, 0.5f, 1.2f);
            }
            case KatanaPhase.DRAWN -> {
                int current = p.getInventory().getSelectedSlot();
                int prev = p.getAttachedOrElse(ModAttachments.PREV_SLOT, 0);
                if (prev == current || prev < 0 || prev > 8) {
                    prev = (current + 1) % 9;
                }
                selectSlot(p, prev);
                setPhase(p, KatanaPhase.SHEATHING, now);
                p.getWorld().playSound(null, p.getBlockPos(),
                        ModSounds.KATANA_SWISH, SoundCategory.PLAYERS, 0.6f, 0.8f);
            }
            case KatanaPhase.NONE ->
                    p.sendMessage(Text.translatable("message.shadowkatana.no_katana"), true);
            default -> { /* ja esta animando */ }
        }
    }

    /** Chamado todo tick do servidor para cada jogador. */
    public static void tick(ServerPlayerEntity p) {
        int phase = p.getAttachedOrElse(ModAttachments.PHASE, KatanaPhase.NONE);
        long now = p.getWorld().getTime();
        long start = p.getAttachedOrElse(ModAttachments.START, 0L);
        boolean has = hasKatana(p);
        boolean holding = p.getMainHandStack().isOf(ModItems.SHADOW_KATANA);

        switch (phase) {
            case KatanaPhase.NONE -> {
                if (has) setPhase(p, holding ? KatanaPhase.DRAWN : KatanaPhase.SHEATHED, now);
            }
            case KatanaPhase.SHEATHED -> {
                if (!has) setPhase(p, KatanaPhase.NONE, now);
                else if (holding) setPhase(p, KatanaPhase.DRAWN, now); // jogador rolou ate o slot manualmente
            }
            case KatanaPhase.DRAWING -> {
                long el = now - start;
                if (el == 4) playAt(p, ModSounds.KATANA_DRAW, 1.0f, 1.0f);   // katana sai das costas
                if (el == 9) playAt(p, ModSounds.KATANA_SWISH, 0.9f, 1.0f);  // corte no ar
                if (!has) {
                    setPhase(p, KatanaPhase.NONE, now);
                } else if (now - start >= KatanaPhase.DRAW_TICKS) {
                    int slot = findHotbarSlot(p);
                    if (slot >= 0) {
                        selectSlot(p, slot);
                        setPhase(p, KatanaPhase.DRAWN, now);
                    } else {
                        setPhase(p, KatanaPhase.SHEATHED, now);
                    }
                }
            }
            case KatanaPhase.DRAWN -> {
                if (!has) setPhase(p, KatanaPhase.NONE, now);
                else if (!holding) setPhase(p, KatanaPhase.SHEATHED, now);
            }
            case KatanaPhase.SHEATHING -> {
                if (now - start == 9) playAt(p, ModSounds.KATANA_SHEATHE, 1.0f, 1.0f); // encaixa nas costas
                if (!has) setPhase(p, KatanaPhase.NONE, now);
                else if (now - start >= KatanaPhase.SHEATH_TICKS) setPhase(p, KatanaPhase.SHEATHED, now);
            }
            default -> { }
        }
    }

    private static void playAt(ServerPlayerEntity p, net.minecraft.sound.SoundEvent sound, float vol, float pitch) {
        p.getWorld().playSound(null, p.getBlockPos(), sound, SoundCategory.PLAYERS, vol, pitch);
    }

    private static void setPhase(ServerPlayerEntity p, int phase, long now) {
        p.setAttached(ModAttachments.START, now);
        p.setAttached(ModAttachments.PHASE, phase);
    }

    private static void selectSlot(ServerPlayerEntity p, int slot) {
        p.getInventory().setSelectedSlot(slot);
        p.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(slot));
    }

    private static int findHotbarSlot(ServerPlayerEntity p) {
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(ModItems.SHADOW_KATANA)) return i;
        }
        return -1;
    }

    private static boolean hasKatana(ServerPlayerEntity p) {
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(ModItems.SHADOW_KATANA)) return true;
        }
        return false;
    }

    private KatanaLogic() {}
}
