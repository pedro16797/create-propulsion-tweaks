package dev.propulsionteam.propulsionsimulated.content.heat.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionPartialModels;
import dev.propulsionteam.propulsionsimulated.utility.math.MathUtility;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.mojang.blaze3d.vertex.PoseStack;

public class StirlingEngineVisual extends KineticBlockEntityVisual<StirlingEngineBlockEntity> implements SimpleDynamicVisual {
    protected TransformedInstance shaft;
    protected final List<TransformedInstance> pistons = new ArrayList<>(4);
    protected TransformedInstance multiblockBase;
    
    private final static int[] offsetArray = {0, 7, 2, 9};
    private final Direction facing;
    private final Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);

    public StirlingEngineVisual(VisualizationContext context, StirlingEngineBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        this.facing = blockState.getValue(StirlingEngineBlock.HORIZONTAL_FACING);

        if (blockEntity.isMultiblock && !blockEntity.isController()) return;

        shaft = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.SHAFT_HALF)).createInstance();

        var pistonModel = Models.partial(PropulsionPartialModels.STIRLING_ENGINE_PISTON);
        
        for (int i = 0; i < 4; i++) {
            TransformedInstance piston = instancerProvider().instancer(InstanceTypes.TRANSFORMED, pistonModel).createInstance();
            pistons.add(piston);
        }

        if (blockEntity.isMultiblock) {
            multiblockBase = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.block(blockState.setValue(StirlingEngineBlock.MULTIBLOCK, false))).createInstance();
        }
        
        animate(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (shaft == null) return;
        animate(ctx.partialTick());
    }

    private void animate(float partialTick) {
        float time = AnimationTickHolder.getRenderTime(blockEntity.getLevel());
        float timeSeconds = time / 20.0f;
        
        float speed = Math.abs(blockEntity.getSpeed() / StirlingEngineBlockEntity.MAX_GENERATED_RPM);
        float effectiveRevolutionPeriod = Float.MAX_VALUE;
        if (speed > MathUtility.epsilon) {
            effectiveRevolutionPeriod = PropulsionConfig.STIRLING_REVOLUTION_PERIOD.get().floatValue() / speed;
        }

        float crankRadius = PropulsionConfig.STIRLING_CRANK_RADIUS.get().floatValue();
        float conrodLength = PropulsionConfig.STIRLING_CONROD_LENGTH.get().floatValue();

        Vector4f normalizedExtensions = StirlingEngineRenderer.calculateExtensions(timeSeconds, crankRadius, conrodLength, effectiveRevolutionPeriod);

        float scale = blockEntity.isMultiblock ? 3.001f : 1.0f;
        BlockPos originOffset = blockEntity.isMultiblock && blockEntity.structureOrigin != null ? blockEntity.structureOrigin.subtract(blockEntity.getBlockPos()) : BlockPos.ZERO;

        // Animate shaft
        PoseStack ms = new PoseStack();
        ms.translate(originOffset.getX(), originOffset.getY(), originOffset.getZ());
        ms.scale(scale, scale, scale);

        float renderTime = AnimationTickHolder.getRenderTime(blockEntity.getLevel());
        float engineSpeed = blockEntity.getSpeed();
        float angle = (renderTime * engineSpeed * 3f / 10f) % 360;
        angle += rotationOffset(blockState, facing.getAxis(), pos);

        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(facing.getRotation());
        ms.mulPose(Axis.YP.rotationDegrees(180)); // SHAFT_HALF faces SOUTH by default in rotateToFace
        ms.mulPose(Axis.XP.rotationDegrees(angle));
        ms.translate(-0.5, -0.5, -0.5);

        shaft.setTransform(ms).setChanged();

        if (multiblockBase != null) {
            PoseStack baseMs = new PoseStack();
            baseMs.translate(originOffset.getX(), originOffset.getY(), originOffset.getZ());
            baseMs.scale(scale, scale, scale);
            multiblockBase.setTransform(baseMs).setChanged();
        }

        final float offsetDistance = 2 / 16.0f;
        for (int i = 0; i < 4; i++) {
            float normalized;
            if (i == 0) normalized = normalizedExtensions.x;
            else if (i == 1) normalized = normalizedExtensions.y;
            else if (i == 2) normalized = normalizedExtensions.z;
            else normalized = normalizedExtensions.w;

            float offset = Math.min(offsetDistance - 0.001f, normalized * offsetDistance); 

            transformPiston(pistons.get(i), i, offset, originOffset, scale);
        }
    }

    private void transformPiston(TransformedInstance instance, int index, float extensionOffset, BlockPos originOffset, float scale) {
        PoseStack ms = new PoseStack();
        ms.translate(originOffset.getX(), originOffset.getY(), originOffset.getZ());
        ms.scale(scale, scale, scale);

        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(facing.getRotation());
        
        if (index >= 2) {
            ms.mulPose(Axis.ZP.rotationDegrees(180));
        }

        ms.mulPose(Axis.XP.rotationDegrees(270));
        ms.translate(-0.5, -0.5, -0.5);

        ms.translate(extensionOffset, 0, offsetArray[index] / 16.0f);

        instance.setTransform(ms).setChanged();
    }

    @Override
    public void update(float pt) {
        // Shaft handled in animate for TransformedInstance
    }

    @Override
    public void updateLight(float partialTick) {
        if (shaft != null) relight(shaft);
        for (TransformedInstance piston : pistons) {
            relight(piston);
        }
        if (multiblockBase != null) relight(multiblockBase);
    }

    @Override
    protected void _delete() {
        if (shaft != null) shaft.delete();
        for (TransformedInstance piston : pistons) {
            piston.delete();
        }
        pistons.clear();
        if (multiblockBase != null) multiblockBase.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        if (shaft != null) consumer.accept(shaft);
        for (TransformedInstance piston : pistons) {
            consumer.accept(piston);
        }
        if (multiblockBase != null) consumer.accept(multiblockBase);
    }
}
