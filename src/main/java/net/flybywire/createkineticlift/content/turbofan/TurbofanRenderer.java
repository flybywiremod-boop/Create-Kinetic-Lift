package net.flybywire.createkineticlift.content.turbofan;

import net.flybywire.createkineticlift.registries.CKLPartialModels;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class TurbofanRenderer extends SmartBlockEntityRenderer<TurbofanBlockEntity> {

	public TurbofanRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(TurbofanBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		BlockState state = be.getBlockState();
		Direction direction = state.getValue(TurbofanIntakeBlock.FACING);
		float facingRot = AngleHelper.rad(AngleHelper.horizontalAngle(direction.getOpposite()));
		float rotorAngle = Mth.lerp(partialTicks, be.prevRotorAngle, be.rotorAngle);
		float rotorRot = AngleHelper.rad(rotorAngle);

		VertexConsumer vc = buffer.getBuffer(RenderType.cutoutMipped());

		SuperByteBuffer cone = CachedBuffers.partial(CKLPartialModels.TURBOFAN_INTAKE_CONE, state);
		SuperByteBuffer blade = CachedBuffers.partial(CKLPartialModels.IRON_BLADE, state);

		cone.rotateCentered(facingRot, Direction.UP);
		cone.rotateCentered(rotorRot, Axis.Z);
		cone.light(light);
		cone.renderInto(ms, vc);

		int blades = be.getBladeCount();

		if (blades > 0) {
			for (int i = 0; i < blades; i++) {
				float bladeAngle = Mth.lerp(partialTicks, be.prevBladeAngles[i], be.bladeAngles[i]);
				float angle = rotorAngle + bladeAngle;

				blade.rotateCentered(facingRot, Direction.UP);
				blade.rotateCentered(AngleHelper.rad(angle), Axis.Z);
				blade.translate(0f, 12.5f / 16f, -.5f);
				blade.rotateCentered(AngleHelper.rad(25), Direction.UP);
				blade.light(light);
				blade.renderInto(ms, vc);
			}
		}
	}

	@Override
	public int getViewDistance() {
		return 256;
	}
}
