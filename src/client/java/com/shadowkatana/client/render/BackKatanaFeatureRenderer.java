package com.shadowkatana.client.render;

import com.shadowkatana.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Desenha a katana nas costas do jogador e anima o saque/guarda. */
public class BackKatanaFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    private final ItemModelManager itemModelManager;
    private final ItemRenderState itemState = new ItemRenderState();
    private ItemStack katana;

    public BackKatanaFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context,
                                     ItemModelManager itemModelManager) {
        super(context);
        this.itemModelManager = itemModelManager;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        if (state.invisible) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        Entity entity = mc.world.getEntityById(state.id);
        if (!(entity instanceof PlayerEntity player)) return;

        float t = KatanaAnim.progress(state);
        if (t < 0f) return; // NONE ou DRAWN: nada nas costas (o jogo desenha o item na mao)

        // a katana so sai das costas depois que o braco chega no cabo (REACH)
        float k = MathHelper.clamp((t - KatanaAnim.REACH) / (1f - KatanaAnim.REACH), 0f, 1f);
        float e = k * k * (3f - 2f * k); // suavizacao

        float x = MathHelper.lerp(e, KatanaPose.BACK_X, KatanaPose.HAND_X);
        float y = MathHelper.lerp(e, KatanaPose.BACK_Y, KatanaPose.HAND_Y)
                - KatanaPose.ARC_HEIGHT * MathHelper.sin((float) Math.PI * e);
        float z = MathHelper.lerp(e, KatanaPose.BACK_Z, KatanaPose.HAND_Z);
        float swing = MathHelper.sin((float) Math.PI * e);
        float roll = MathHelper.lerp(e, KatanaPose.BACK_ROLL, KatanaPose.HAND_ROLL) + KatanaPose.FLOURISH_ROLL * swing;
        float pitch = MathHelper.lerp(e, KatanaPose.BACK_PITCH, KatanaPose.HAND_PITCH) - KatanaPose.FLOURISH_PITCH * swing;

        if (katana == null) katana = new ItemStack(ModItems.SHADOW_KATANA);

        // !!! API MAIS PROVAVEL DE MUDAR/ERRAR: atualiza o estado de render do item.
        // Em 1.21.8 o esperado e updateForNonLivingEntity(state, stack, modo, entidade).
        itemModelManager.updateForNonLivingEntity(itemState, katana, ItemDisplayContext.HEAD, player);

        matrices.push();
        ModelPart body = getContextModel().body;
        body.rotate(matrices);                       // segue o corpo (inclusive agachado)
        matrices.translate(x, y, z);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f)); // desfaz o espelhamento do modelo do jogador
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        matrices.scale(KatanaPose.SCALE, KatanaPose.SCALE, KatanaPose.SCALE);
        itemState.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}