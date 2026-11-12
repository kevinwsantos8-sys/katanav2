package com.shadowkatana.client.render;

import com.shadowkatana.katana.KatanaPhase;
import com.shadowkatana.katana.ModAttachments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Animacao de sacar/guardar (lado do cliente).
 * progress(): 0 = katana nas costas / braco normal, 1 = katana na mao. Guardar toca o mesmo movimento ao contrario.
 * Para ajustar o movimento do BRACO mude a tabela ARM abaixo; o da katana fica em KatanaPose.
 */
public final class KatanaAnim {

    /** Fracao inicial da animacao em que so o braco se move (a katana ainda esta nas costas). */
    public static final float REACH = 0.25f;

    /** Quadros-chave do braco direito: {t, pitch, yaw, roll} em radianos. Primeiro e ultimo = pose normal do jogo. */
    private static final float[][] ARM = {
            {0.25f, -2.75f, 0.35f, 0.45f},  // levanta e alcanca o cabo atras do ombro
            {0.55f, -2.45f, 0.10f, 0.30f},  // puxa a lamina para fora
            {0.80f, -1.45f, -0.35f, 0.00f}, // corta para a frente
    };

    private static final class Anim { int phase = -1; float start; }
    private static final Map<Integer, Anim> ANIMS = new HashMap<>();

    /** Retorna o progresso 0..1, ou -1 se este jogador nao esta animando a katana. */
    public static float progress(PlayerEntityRenderState state) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return -1f;
        Entity entity = mc.world.getEntityById(state.id);
        if (!(entity instanceof PlayerEntity player)) return -1f;

        int phase = player.getAttachedOrElse(ModAttachments.PHASE, KatanaPhase.NONE);
        if (phase != KatanaPhase.DRAWING && phase != KatanaPhase.SHEATHING) {
            ANIMS.remove(state.id);
            return phase == KatanaPhase.SHEATHED ? 0f : -1f;
        }

        Anim anim = ANIMS.computeIfAbsent(state.id, k -> new Anim());
        if (anim.phase != phase) {
            anim.phase = phase;
            anim.start = state.age;
        }
        float elapsed = state.age - anim.start;
        if (phase == KatanaPhase.DRAWING) {
            return MathHelper.clamp(elapsed / KatanaPhase.DRAW_TICKS, 0f, 1f);
        }
        return 1f - MathHelper.clamp(elapsed / KatanaPhase.SHEATH_TICKS, 0f, 1f);
    }

    /** Aplica a pose animada no braco direito (chamado pelo mixin depois da pose normal do jogo). */
    public static void applyArm(ModelPart arm, float t) {
        if (t <= 0f || t >= 1f) return;
        float[] prev = {0f, arm.pitch, arm.yaw, arm.roll};      // pose normal do jogo
        float[] next = null;
        for (float[] k : ARM) {
            if (t <= k[0]) { next = k; break; }
            prev = k;
        }
        if (next == null) next = new float[]{1f, arm.pitch, arm.yaw, arm.roll};
        // se prev ainda e o ponto inicial, mantem t=0; se estamos apos o ultimo, o destino e a pose normal
        float span = next[0] - prev[0];
        float u = span <= 0f ? 1f : (t - prev[0]) / span;
        u = u * u * (3f - 2f * u);
        arm.pitch = MathHelper.lerp(u, prev[1], next[1]);
        arm.yaw = MathHelper.lerp(u, prev[2], next[2]);
        arm.roll = MathHelper.lerp(u, prev[3], next[3]);
    }

    private KatanaAnim() {}
}
