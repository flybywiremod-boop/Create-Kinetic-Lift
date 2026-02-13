package net.flybywire.createkineticlift.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.flybywire.createkineticlift.jet.TurbofanFrontBlock;
import net.flybywire.createkineticlift.jet.TurbofanFrontBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renderer for Turbofan Front Block Entity
 *
 * Features:
 * - Rotating fan blades based on blade speed
 * - Blur effect at high speeds (simulated with transparency)
 * - Smooth interpolation between frames
 */
public class TurbofanFrontRenderer implements BlockEntityRenderer<TurbofanFrontBlockEntity> {

    // Speed threshold for blur effect (degrees per tick)
    private static final float BLUR_SPEED_THRESHOLD = 180.0f;
    private static final float MAX_BLUR_SPEED = 360.0f;

    // Blade rendering constants
    private static final int BLADE_COUNT = 8;
    private static final float BLADE_LENGTH = 0.4f;
    private static final float BLADE_WIDTH = 0.08f;
    private static final float HUB_RADIUS = 0.1f;

    public TurbofanFrontRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TurbofanFrontBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof TurbofanFrontBlock)) return;

        Direction facing = state.getValue(TurbofanFrontBlock.FACING);
        float bladeAngle = entity.getBladeAngle();
        float bladeSpeed = entity.getBladeSpeed();

        // Interpolate angle for smooth animation
        float interpolatedAngle = bladeAngle + (bladeSpeed * partialTick);

        poseStack.pushPose();

        // Move to center of block
        poseStack.translate(0.5, 0.5, 0.5);

        // Rotate based on facing direction
        applyFacingRotation(poseStack, facing);

        // Calculate blur alpha based on speed
        float blurAlpha = calculateBlurAlpha(bladeSpeed);

        if (blurAlpha < 1.0f) {
            // Render multiple blade sets at different angles for blur effect
            renderBlurredBlades(poseStack, buffer, packedLight, packedOverlay,
                    interpolatedAngle, blurAlpha);
        } else {
            // Render normal blades
            renderBlades(poseStack, buffer, packedLight, packedOverlay,
                    interpolatedAngle, 1.0f);
        }

        // Render the center hub
        renderHub(poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void applyFacingRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case SOUTH -> {} // Default orientation
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
        }
    }

    private float calculateBlurAlpha(float bladeSpeed) {
        if (bladeSpeed < BLUR_SPEED_THRESHOLD) {
            return 1.0f; // No blur
        }
        // Fade from solid to blur as speed increases
        float blurFactor = (bladeSpeed - BLUR_SPEED_THRESHOLD) / (MAX_BLUR_SPEED - BLUR_SPEED_THRESHOLD);
        return Math.max(0.3f, 1.0f - blurFactor * 0.7f);
    }

    private void renderBlurredBlades(PoseStack poseStack, MultiBufferSource buffer,
                                     int packedLight, int packedOverlay,
                                     float angle, float baseAlpha) {
        // Render multiple blade sets at offset angles for motion blur effect
        int blurSteps = 3;
        float angleStep = 15.0f; // Degrees between blur copies

        for (int i = 0; i < blurSteps; i++) {
            float offsetAngle = angle - (i * angleStep);
            float alpha = baseAlpha * (1.0f - (i * 0.2f));
            renderBlades(poseStack, buffer, packedLight, packedOverlay, offsetAngle, alpha);
        }

        // Also render a solid blur disc at very high speeds
        if (baseAlpha < 0.5f) {
            renderBlurDisc(poseStack, buffer, packedLight, packedOverlay, 0.4f);
        }
    }

    private void renderBlades(PoseStack poseStack, MultiBufferSource buffer,
                              int packedLight, int packedOverlay,
                              float angle, float alpha) {

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));

        for (int i = 0; i < BLADE_COUNT; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * (360.0f / BLADE_COUNT)));

            // Draw blade quad
            Matrix4f matrix = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            int a = (int)(alpha * 255);
            int r = 0x88;
            int g = 0x88;
            int b = 0x88;

            // Blade from hub to outer edge
            float innerX = HUB_RADIUS;
            float outerX = HUB_RADIUS + BLADE_LENGTH;
            float halfWidth = BLADE_WIDTH / 2;

            // Front face - 4 vertices for quad
            vertex(vertexConsumer, matrix, normal, innerX, -halfWidth, 0.01f, 0, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, outerX, -halfWidth, 0.01f, 1, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, outerX, halfWidth, 0.01f, 1, 1, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, innerX, halfWidth, 0.01f, 0, 1, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);

            // Back face - 4 vertices for quad
            vertex(vertexConsumer, matrix, normal, innerX, halfWidth, -0.01f, 0, 1, 0, 0, -1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, outerX, halfWidth, -0.01f, 1, 1, 0, 0, -1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, outerX, -halfWidth, -0.01f, 1, 0, 0, 0, -1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, innerX, -halfWidth, -0.01f, 0, 0, 0, 0, -1, packedLight, packedOverlay, r, g, b, a);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderBlurDisc(PoseStack poseStack, MultiBufferSource buffer,
                                int packedLight, int packedOverlay, float alpha) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int a = (int)(alpha * 255);
        int r = 0xAA;
        int g = 0xAA;
        int b = 0xAA;

        float radius = HUB_RADIUS + BLADE_LENGTH;
        int segments = 16;

        // Render a disc as a fan of quads
        for (int i = 0; i < segments; i++) {
            float angle1 = (float)(i * 2 * Math.PI / segments);
            float angle2 = (float)((i + 1) * 2 * Math.PI / segments);

            float x1 = (float)Math.cos(angle1) * radius;
            float y1 = (float)Math.sin(angle1) * radius;
            float x2 = (float)Math.cos(angle2) * radius;
            float y2 = (float)Math.sin(angle2) * radius;

            // Quad from center to edge (degenerate quad with center vertex duplicated)
            vertex(vertexConsumer, matrix, normal, 0, 0, 0.02f, 0.5f, 0.5f, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, x1, y1, 0.02f, 0, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, x2, y2, 0.02f, 1, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, x2, y2, 0.02f, 1, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
        }
    }

    private void renderHub(PoseStack poseStack, MultiBufferSource buffer,
                          int packedLight, int packedOverlay) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int r = 0x44;
        int g = 0x44;
        int b = 0x44;
        int a = 255;

        float depth = 0.05f;
        int segments = 8;

        // Render hub as octagon front face
        for (int i = 0; i < segments; i++) {
            float angle1 = (float)(i * 2 * Math.PI / segments);
            float angle2 = (float)((i + 1) * 2 * Math.PI / segments);

            float x1 = (float)Math.cos(angle1) * HUB_RADIUS;
            float y1 = (float)Math.sin(angle1) * HUB_RADIUS;
            float x2 = (float)Math.cos(angle2) * HUB_RADIUS;
            float y2 = (float)Math.sin(angle2) * HUB_RADIUS;

            // Front face quad
            vertex(vertexConsumer, matrix, normal, 0, 0, depth, 0.5f, 0.5f, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, x1, y1, depth, 0, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, x2, y2, depth, 1, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
            vertex(vertexConsumer, matrix, normal, x2, y2, depth, 1, 0, 0, 0, 1, packedLight, packedOverlay, r, g, b, a);
        }
    }

    /**
     * Helper method to add a vertex with all attributes - compatible with 1.20.1 API
     */
    private void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normalMatrix,
                        float x, float y, float z, float u, float v,
                        float nx, float ny, float nz,
                        int light, int overlay, int r, int g, int b, int a) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(TurbofanFrontBlockEntity entity) {
        return true;
    }
}
