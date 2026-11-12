package com.shadowkatana.katana;

import com.shadowkatana.ShadowKatanaMod;
import com.shadowkatana.item.ModItems;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Upgrades da katana. TODOS os numeros de balanceamento ficam aqui (facil de ajustar).
 * Pagamento: niveis de XP. Nivel maximo: 50 por upgrade.
 */
public final class KatanaUpgrades {
    public static final int MAX_LEVEL = 50;

    public static final int SWORD = 0;     // dano da espada
    public static final int TELEPORT = 1;  // dano do poder Teleporte (usado na Etapa 5)
    public static final int SHADOW = 2;    // dano do poder Sombra / golpe invisivel (usado na Etapa 5)

    private static final Identifier BONUS_ID = Identifier.of(ShadowKatanaMod.MOD_ID, "katana_damage");

    /** Custo (em niveis de XP) para subir do nivel atual para o proximo. 1 a 5 niveis de XP. */
    public static int cost(int currentLevel) {
        return 1 + currentLevel / 10;
    }

    /** Dano extra da espada quando segurada (+0.5 por nivel = +25 no nivel 50). */
    public static double swordBonus(int level) {
        return level * 0.5;
    }

    /** Dano do Teleporte em area (caminho + destino). */
    public static double teleportDamage(int level) {
        return 2.0 + level * 0.5;
    }

    /** Dano bonus do primeiro golpe depois de ficar invisivel. */
    public static double shadowStrikeBonus(int level) {
        return 2.0 + level * 0.75;
    }

    public static AttachmentType<Integer> typeOf(int id) {
        return switch (id) {
            case SWORD -> ModAttachments.SWORD_LEVEL;
            case TELEPORT -> ModAttachments.TELEPORT_LEVEL;
            default -> ModAttachments.SHADOW_LEVEL;
        };
    }

    public static int get(PlayerEntity p, int id) {
        return p.getAttachedOrElse(typeOf(id), 0);
    }

    /** Servidor: tenta comprar 1 nivel do upgrade. */
    public static void tryUpgrade(ServerPlayerEntity p, int id) {
        if (id < SWORD || id > SHADOW) return;
        if (p.getAttachedOrElse(ModAttachments.PHASE, KatanaPhase.NONE) == KatanaPhase.NONE) {
            p.sendMessage(Text.translatable("message.shadowkatana.no_katana"), true);
            return;
        }
        int level = get(p, id);
        if (level >= MAX_LEVEL) return;

        int cost = cost(level);
        if (!p.isCreative()) {
            if (p.experienceLevel < cost) {
                p.sendMessage(Text.translatable("message.shadowkatana.not_enough_xp", cost), true);
                return;
            }
            p.addExperienceLevels(-cost);
        }
        p.setAttached(typeOf(id), level + 1);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS, 0.8f, 0.7f);
        p.sendMessage(Text.translatable("message.shadowkatana.upgraded",
                Text.translatable(nameKey(id)), level + 1), true);
    }

    public static String nameKey(int id) {
        return switch (id) {
            case SWORD -> "screen.shadowkatana.sword";
            case TELEPORT -> "screen.shadowkatana.teleport";
            default -> "screen.shadowkatana.shadow";
        };
    }

    /** Servidor, todo tick: aplica o bonus de dano da espada enquanto o jogador segura a katana. */
    public static void tickBonus(ServerPlayerEntity p) {
        EntityAttributeInstance inst = p.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (inst == null) return;

        double want = 0.0;
        if (p.getMainHandStack().isOf(ModItems.SHADOW_KATANA)) {
            want = swordBonus(get(p, SWORD));
            if (p.getAttachedOrElse(ModAttachments.STRIKE_ARMED, false)) {
                want += shadowStrikeBonus(get(p, SHADOW)); // golpe sombrio: primeiro golpe apos ficar invisivel
            }
        }
        EntityAttributeModifier cur = inst.getModifier(BONUS_ID);

        if (want <= 0.0) {
            if (cur != null) inst.removeModifier(BONUS_ID);
            return;
        }
        if (cur == null || cur.value() != want) {
            inst.removeModifier(BONUS_ID);
            inst.addTemporaryModifier(
                    new EntityAttributeModifier(BONUS_ID, want, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    private KatanaUpgrades() {}
}
