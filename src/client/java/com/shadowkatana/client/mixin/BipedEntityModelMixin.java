package com.shadowkatana.client.mixin;

import com.shadowkatana.client.render.KatanaAnim;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Levanta o braco direito do jogador ao sacar/guardar a katana. */
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
            at = @At("TAIL"), require = 0)
    private void shadowkatana$drawArm(BipedEntityRenderState state, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) return;
        float t = KatanaAnim.progress(playerState);
        if (t > 0f && t < 1f) {
            BipedEntityModel<?> self = (BipedEntityModel<?>) (Object) this;
            KatanaAnim.applyArm(self.rightArm, t);
        }
    }
}
