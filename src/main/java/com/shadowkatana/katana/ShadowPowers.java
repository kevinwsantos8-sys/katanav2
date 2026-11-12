package com.shadowkatana.katana;

import com.shadowkatana.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Poderes de Sombra (exigem a katana na mao):
 *  - Teleporte (Z): salta ate TELEPORT_RANGE blocos na direcao do olhar; causa dano em area no caminho/destino.
 *  - Invisibilidade (X): 20 s invisivel; o PRIMEIRO golpe de katana nesse periodo causa dano bonus.
 * Numeros de balanceamento ficam nas constantes abaixo e em KatanaUpgrades.
 */
public final class ShadowPowers {
    public static final int TELEPORT = 0;
    public static final int INVISIBILITY = 1;

    public static final double TELEPORT_RANGE = 12.0;
    public static final double TELEPORT_HIT_RADIUS = 2.5;
    public static final int TELEPORT_COOLDOWN = 60;     // 3 s
    public static final int INVIS_DURATION = 400;       // 20 s
    public static final int INVIS_COOLDOWN = 800;       // 40 s contados a partir do uso

    /** Evita que o dano do teleporte gaste o golpe sombrio. */
    private static boolean applyingPowerDamage = false;

    public static void register() {
        // Depois de acertar um golpe corpo a corpo com a katana armada, o golpe sombrio e consumido.
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (applyingPowerDamage) return;
            if (source.getAttacker() instanceof ServerPlayerEntity sp
                    && source.getSource() == sp
                    && entity != sp
                    && sp.getAttachedOrElse(ModAttachments.STRIKE_ARMED, false)
                    && sp.getMainHandStack().isOf(ModItems.SHADOW_KATANA)) {
                sp.setAttached(ModAttachments.STRIKE_ARMED, false);
                ServerWorld w = (ServerWorld) sp.getWorld();
                w.spawnParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY() + 1.0, entity.getZ(),
                        30, 0.4, 0.6, 0.4, 0.08);
            }
        });
    }

    /** Servidor, todo tick. */
    public static void tick(ServerPlayerEntity p) {
        if (p.getAttachedOrElse(ModAttachments.STRIKE_ARMED, false)
                && !p.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            p.setAttached(ModAttachments.STRIKE_ARMED, false);
        }
    }

    public static void use(ServerPlayerEntity p, int power) {
        if (p.getAttachedOrElse(ModAttachments.PHASE, KatanaPhase.NONE) != KatanaPhase.DRAWN) {
            p.sendMessage(Text.translatable("message.shadowkatana.draw_first"), true);
            return;
        }
        long now = p.getWorld().getTime();
        switch (power) {
            case TELEPORT -> teleport(p, now);
            case INVISIBILITY -> invisibility(p, now);
            default -> { }
        }
    }

    private static boolean onCooldown(ServerPlayerEntity p, long readyAt, long now) {
        if (now < readyAt) {
            long secs = (long) Math.ceil((readyAt - now) / 20.0);
            p.sendMessage(Text.translatable("message.shadowkatana.cooldown", secs), true);
            return true;
        }
        return false;
    }

    private static void teleport(ServerPlayerEntity p, long now) {
        if (onCooldown(p, p.getAttachedOrElse(ModAttachments.TELEPORT_READY_AT, 0L), now)) return;

        ServerWorld world = (ServerWorld) p.getWorld();
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d start = p.getPos();
        double eyeOffset = p.getEyeY() - p.getY();

        HitResult hit = p.raycast(TELEPORT_RANGE, 1.0f, false);
        double maxDist = hit.getType() == HitResult.Type.MISS ? TELEPORT_RANGE : hit.getPos().distanceTo(eye);

        Vec3d target = null;
        for (double d = maxDist; d >= 1.0; d -= 0.5) {
            Vec3d feet = eye.add(look.multiply(d)).subtract(0.0, eyeOffset, 0.0);
            Box box = p.getBoundingBox().offset(feet.subtract(start));
            if (world.isSpaceEmpty(p, box)) {
                target = feet;
                break;
            }
        }
        if (target == null) {
            p.sendMessage(Text.translatable("message.shadowkatana.teleport_blocked"), true);
            return;
        }

        // dano em area no caminho e no destino
        float dmg = (float) KatanaUpgrades.teleportDamage(KatanaUpgrades.get(p, KatanaUpgrades.TELEPORT));
        Box area = new Box(start, target).expand(TELEPORT_HIT_RADIUS);
        applyingPowerDamage = true;
        try {
            for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, area, e -> e != p && e.isAlive())) {
                e.damage(world, p.getDamageSources().playerAttack(p), dmg);
            }
        } finally {
            applyingPowerDamage = false;
        }

        // efeitos + salto
        world.spawnParticles(ParticleTypes.SQUID_INK, start.x, start.y + 1.0, start.z, 40, 0.3, 0.7, 0.3, 0.1);
        world.spawnParticles(ParticleTypes.PORTAL, start.x, start.y + 1.0, start.z, 30, 0.3, 0.7, 0.3, 0.5);
        world.playSound(null, start.x, start.y, start.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS, 1.0f, 0.6f);

        p.requestTeleport(target.x, target.y, target.z);
        p.fallDistance = 0;

        world.spawnParticles(ParticleTypes.SQUID_INK, target.x, target.y + 1.0, target.z, 40, 0.3, 0.7, 0.3, 0.1);
        world.spawnParticles(ParticleTypes.PORTAL, target.x, target.y + 1.0, target.z, 30, 0.3, 0.7, 0.3, 0.5);
        world.playSound(null, target.x, target.y, target.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS, 1.0f, 0.9f);

        p.setAttached(ModAttachments.TELEPORT_READY_AT, now + TELEPORT_COOLDOWN);
    }

    private static void invisibility(ServerPlayerEntity p, long now) {
        if (onCooldown(p, p.getAttachedOrElse(ModAttachments.SHADOW_READY_AT, 0L), now)) return;

        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, INVIS_DURATION, 0, false, false, true));
        p.setAttached(ModAttachments.STRIKE_ARMED, true);
        p.setAttached(ModAttachments.SHADOW_READY_AT, now + INVIS_COOLDOWN);

        ServerWorld world = (ServerWorld) p.getWorld();
        world.spawnParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1.0, p.getZ(), 50, 0.4, 0.8, 0.4, 0.08);
        world.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS, 0.7f, 0.4f);
        p.sendMessage(Text.translatable("message.shadowkatana.invisible_on", INVIS_DURATION / 20), true);
    }

    private ShadowPowers() {}
}
