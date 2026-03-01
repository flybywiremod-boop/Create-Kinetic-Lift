package net.flybywire.createkineticlift.content.turbofan.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.flybywire.createkineticlift.content.turbofan.TurbofanIntakeBlock;
import net.flybywire.createkineticlift.content.turbofan.TurbofanBlockEntity;
import net.flybywire.createkineticlift.registries.KineticPartialModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class TurbofanRenderer extends SmartBlockEntityRenderer<TurbofanBlockEntity> {

    public TurbofanRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(TurbofanBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (!(be.getBlockState().getBlock() instanceof TurbofanIntakeBlock)) return;

        Direction direction = be.getBlockState().getValue(TurbofanIntakeBlock.FACING);
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        // Partials
        SuperByteBuffer coneModel = CachedBuffers.partial(KineticPartialModels.TURBOFAN_CONE, be.getBlockState());
        SuperByteBuffer bladeModel = CachedBuffers.partial(KineticPartialModels.IRON_BLADE, be.getBlockState());

        float currentAngle = 0f;

        // Cone
        ms.pushPose();
        ms.translate(0.5, 0.5, 0.5);

        ms.mulPose(direction.getRotation());

        ms.mulPose(Axis.XP.rotationDegrees(90));

        ms.mulPose(Axis.ZP.rotationDegrees(currentAngle));
        ms.translate(-0.5, -0.5, -0.5);

        coneModel.light(light).overlay(overlay).renderInto(ms, vb);
        ms.popPose();

        // Blades
        int bladeCount = be.getBladeCount();
        if (bladeCount > 0) {

            for (int i = 0; i < bladeCount; i++) {

                float smoothedAngle = net.minecraft.util.Mth.lerp(partialTicks, be.prevBladeAngles[i], be.visualBladeAngles[i]);

                float bladeOffsetAngle = currentAngle + smoothedAngle;

                ms.pushPose();

                ms.translate(0.5, 0.5, 0.5);
                ms.mulPose(direction.getRotation());

                ms.mulPose(Axis.XP.rotationDegrees(90));

                ms.translate(0f, 0f, -0.7f);

                ms.mulPose(Axis.ZP.rotationDegrees(bladeOffsetAngle));

                ms.translate(0f, 0.775f, 0f);

                ms.mulPose(Axis.YP.rotationDegrees(15f));

                ms.translate(-0.5, -0.5, -0.5);

                bladeModel.light(light).overlay(overlay).renderInto(ms, vb);
                ms.popPose();
            }
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}