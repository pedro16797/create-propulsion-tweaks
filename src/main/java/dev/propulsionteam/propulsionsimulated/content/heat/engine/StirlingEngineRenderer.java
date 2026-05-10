package dev.propulsionteam.propulsionsimulated.content.heat.engine;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import org.joml.Vector4f;

import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionPartialModels;
import dev.propulsionteam.propulsionsimulated.utility.math.MathUtility;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StirlingEngineRenderer extends KineticBlockEntityRenderer<StirlingEngineBlockEntity> {
    private final static int[] offsetArray = {0, 7, 2, 9};

    public StirlingEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void renderSafe(StirlingEngineBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(blockEntity.getLevel())) return;

        Direction direction = blockEntity.getBlockState().getValue(StirlingEngineBlock.HORIZONTAL_FACING);
        BlockState state = blockEntity.getBlockState();
        Level level = blockEntity.getLevel();
        
        ms.pushPose();
        if (blockEntity.isMultiblock) {
            ms.translate(-1, -2, -1);
            ms.scale(3, 3, 3);

            // Render the engine body (it's hidden from the block itself)
            renderBlock(blockEntity, partialTicks, ms, bufferSource, light, overlay, direction);

            // Render shaft on the front side
            renderShaft(blockEntity, ms, bufferSource, light, state, level, direction);

            float pistonSpeed = Math.abs(blockEntity.getSpeed() / StirlingEngineBlockEntity.MAX_GENERATED_RPM);
            renderPistons(blockEntity, partialTicks, ms, bufferSource, light, overlay, direction, pistonSpeed);
        } else {
            // Render shaft on the front side
            renderShaft(blockEntity, ms, bufferSource, light, state, level, direction);

            float pistonSpeed = Math.abs(blockEntity.getSpeed() / StirlingEngineBlockEntity.MAX_GENERATED_RPM);
            renderPistons(blockEntity, partialTicks, ms, bufferSource, light, overlay, direction, pistonSpeed);
        }
        ms.popPose();
    }

    /**
     * Helper method to handle the manual rotation and translation of the shaft
     * so that it renders at the front center, pointing outward, while maintaining its spin.
     */
    private void renderShaft(StirlingEngineBlockEntity blockEntity, PoseStack ms, MultiBufferSource bufferSource, int light, BlockState state, Level level, Direction direction) {
        float time = AnimationTickHolder.getRenderTime(level);
        float speed = blockEntity.getSpeed();
        float angle = (time * speed * 3f / 10f) % 360;
        angle += getRotationOffsetForPosition(blockEntity, blockEntity.getBlockPos(), direction.getAxis());
        angle = angle / 180f * (float) Math.PI;

        ms.pushPose();
        
        // Move to the center of the block
        ms.translate(0.5, 0.5, 0.5);
        
        // Orient the PoseStack to face the block's direction
        Direction alignDir = direction.getOpposite();
        ms.mulPose(Axis.YP.rotationDegrees(-alignDir.toYRot()));
        ms.mulPose(Axis.XP.rotationDegrees(90));
        
        // Move the center back so the model connects from the center of the block to the face
        ms.translate(-0.5, -0.5, -0.5);

        // Fetch the raw unrotated shaft model instead of partialFacing
        SuperByteBuffer shaft = CachedBuffers.partial(AllPartialModels.SHAFT_HALF, state);
        
        // Spin the shaft along its LOCAL Y axis (which the PoseStack has now rotated to point forwards)
        kineticRotationTransform(shaft, blockEntity, Direction.Axis.Y, angle, light);
        
        shaft.renderInto(ms, bufferSource.getBuffer(RenderType.solid()));
        ms.popPose();
    }

    private void renderBlock(StirlingEngineBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay, Direction direction) {
        BlockState state = blockEntity.getBlockState();
        if (state.hasProperty(StirlingEngineBlock.MULTIBLOCK)) {
            state = state.setValue(StirlingEngineBlock.MULTIBLOCK, false);
        }
        SuperByteBuffer body = CachedBuffers.block(state);
        body.light(light).overlay(overlay).renderInto(ms, bufferSource.getBuffer(RenderType.solid()));
    }

    private void renderPistonsMultiblock(StirlingEngineBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay, Direction direction, float speed) {
        renderPistons(blockEntity, partialTicks, ms, bufferSource, light, overlay, direction, speed);
    }

    private void renderPistons(StirlingEngineBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay, Direction direction, float speed) {
        BlockState state = blockEntity.getBlockState();
        VertexConsumer cutoutVB = bufferSource.getBuffer(RenderType.cutoutMipped());
        Level level = blockEntity.getLevel();
        if (level == null) return;
        float time = AnimationTickHolder.getRenderTime(level);

        SuperByteBuffer pistonModel = CachedBuffers.partial(PropulsionPartialModels.STIRLING_ENGINE_PISTON, state);

        float timeSeconds = time / 20.0f;
        float effectiveRevolutionPeriod = Float.MAX_VALUE;
        if (speed > MathUtility.epsilon) {
            effectiveRevolutionPeriod = PropulsionConfig.STIRLING_REVOLUTION_PERIOD.get().floatValue() / speed;
        }
        float crankRadius = PropulsionConfig.STIRLING_CRANK_RADIUS.get().floatValue();
        float conrodLength = PropulsionConfig.STIRLING_CONROD_LENGTH.get().floatValue();
        Vector4f normalizedExtensions = calculateExtensions(timeSeconds, crankRadius, conrodLength, effectiveRevolutionPeriod);

        for (int i = 0; i < 4; i++) {
            float normalized;
            if (i == 0) normalized = normalizedExtensions.x;
            else if (i == 1) normalized = normalizedExtensions.y;
            else if (i == 2) normalized = normalizedExtensions.z;
            else normalized = normalizedExtensions.w;

            final float offsetDistance = 2 / 16.0f;
            float offset = Math.min(offsetDistance - 0.001f, normalized * offsetDistance); //Avoid z-fighting

            ms.pushPose();
            ms.translate(0.5, 0.5, 0.5);
            ms.mulPose(direction.getRotation());
            
            if (i >= 2) {
                ms.mulPose(Axis.ZP.rotationDegrees(180));
            }

            ms.mulPose(Axis.XP.rotationDegrees(270));
            ms.translate(-0.5, -0.5, -0.5);

            ms.translate(offset, 0, offsetArray[i] / 16.0f);
            pistonModel.light(light).overlay(overlay).renderInto(ms, cutoutVB);
            ms.popPose();
        }
    }

    public static Vector4f calculateExtensions(float time, float crankRadius, float conrodLength, float revolutionPeriod, float[] phases) {
        float[] phaseOffsets = phases;
        float angularSpeed = 2.0f * (float)Math.PI / revolutionPeriod;
        float[] crankAngles = new float[4];
        for (int i = 0; i < 4; ++i) {
            crankAngles[i] = angularSpeed * time + phaseOffsets[i];
        }
        float[] extensions = new float[4];
        for (int i = 0; i < 4; ++i) {
            float sinTheta = (float)Math.sin(crankAngles[i]);
            float cosTheta = (float)Math.cos(crankAngles[i]);
            float underSqrt = conrodLength * conrodLength - (crankRadius * sinTheta) * (crankRadius * sinTheta);
            float pistonDisplacement = crankRadius * (1.0f - cosTheta) + conrodLength - (float)Math.sqrt(underSqrt);
            float normalizedExtension = pistonDisplacement / (2.0f * crankRadius);
            extensions[i] = normalizedExtension;
        }
        return new Vector4f(extensions[0], extensions[1], extensions[2], extensions[3]);
    }

    public static Vector4f calculateExtensions(float time, float crankRadius, float conrodLength, float revolutionPeriod) {
        return calculateExtensions(time, crankRadius, conrodLength, revolutionPeriod, new float[] { 0.0f, (float)Math.PI, 2.0f * (float)Math.PI, 3.0f * (float)Math.PI });
    }
}

