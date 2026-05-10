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
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.level.block.state.BlockState;

public class StirlingEngineVisual extends KineticBlockEntityVisual<StirlingEngineBlockEntity> implements SimpleDynamicVisual {
    protected final TransformedInstance shaft;
    protected final List<TransformedInstance> pistons = new ArrayList<>(4);
    protected final TransformedInstance body;
    
    private final static int[] offsetArray = {0, 7, 2, 9};
    private final Direction facing;
    private final Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);

    public StirlingEngineVisual(VisualizationContext context, StirlingEngineBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        this.facing = blockState.getValue(StirlingEngineBlock.HORIZONTAL_FACING);
        BlockState renderState = blockState;
        if (renderState.hasProperty(StirlingEngineBlock.MULTIBLOCK)) {
            renderState = renderState.setValue(StirlingEngineBlock.MULTIBLOCK, false);
        }
        body = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.block(renderState)).createInstance();
        shaft = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.SHAFT_HALF)).createInstance();

        var pistonModel = Models.partial(PropulsionPartialModels.STIRLING_ENGINE_PISTON);
        
        for (int i = 0; i < 4; i++) {
            TransformedInstance piston = instancerProvider().instancer(InstanceTypes.TRANSFORMED, pistonModel).createInstance();
            pistons.add(piston);
        }

        animate(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float partialTick) {
        float time = AnimationTickHolder.getRenderTime(blockEntity.getLevel());
        
        float speed = blockEntity.getSpeed();
        float angle = (time * speed * 3f / 10f) % 360;
        angle += StirlingEngineRenderer.getRotationOffsetForPosition(blockEntity, blockEntity.getBlockPos(), facing.getAxis());
        angle = angle / 180f * (float) Math.PI;

        PoseStack bodyMs = new PoseStack();
        bodyMs.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());
        if (blockEntity.isMultiblock) {
            bodyMs.translate(-1, -2, -1);
            bodyMs.scale(3, 3, 3);
            bodyMs.translate(0.5, 0.5, 0.5);
            bodyMs.mulPose(Axis.YP.rotationDegrees(180));
            bodyMs.translate(-0.5, -0.5, -0.5);
        } else {
            bodyMs.scale(0, 0, 0);
        }
        body.setTransform(bodyMs).setChanged();

        PoseStack shaftMs = new PoseStack();
        shaftMs.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());
        if (blockEntity.isMultiblock) {
            shaftMs.translate(-1, -2, -1);
            shaftMs.scale(3, 3, 3);
            shaftMs.translate(0.5, 0.5, 0.5);
            shaftMs.mulPose(Axis.YP.rotationDegrees(180));
            shaftMs.translate(-0.5, -0.5, -0.5);
        }
        shaftMs.translate(0.5, 0.5, 0.5);
        shaftMs.mulPose(facing.getRotation());
        shaftMs.mulPose(Axis.XP.rotationDegrees(angle * 180f / (float)Math.PI));
        shaftMs.translate(-0.5, -0.5, -0.5);
        shaft.setTransform(shaftMs).setChanged();

        float timeSeconds = time / 20.0f;
        float pistonSpeedScale = Math.abs(speed / StirlingEngineBlockEntity.MAX_GENERATED_RPM);
        float effectiveRevolutionPeriod = Float.MAX_VALUE;
        if (pistonSpeedScale > MathUtility.epsilon) {
            effectiveRevolutionPeriod = PropulsionConfig.STIRLING_REVOLUTION_PERIOD.get().floatValue() / pistonSpeedScale;
        }

        float crankRadius = PropulsionConfig.STIRLING_CRANK_RADIUS.get().floatValue();
        float conrodLength = PropulsionConfig.STIRLING_CONROD_LENGTH.get().floatValue();

        Vector4f normalizedExtensions = StirlingEngineRenderer.calculateExtensions(timeSeconds, crankRadius, conrodLength, effectiveRevolutionPeriod);

        final float offsetDistance = 2 / 16.0f;
        for (int i = 0; i < 4; i++) {
            float normalized;
            if (i == 0) normalized = normalizedExtensions.x;
            else if (i == 1) normalized = normalizedExtensions.y;
            else if (i == 2) normalized = normalizedExtensions.z;
            else normalized = normalizedExtensions.w;

            float offset = Math.min(offsetDistance - 0.001f, normalized * offsetDistance); 

            transformPiston(pistons.get(i), i, offset);
        }
    }

    private void transformPiston(TransformedInstance instance, int index, float extensionOffset) {
        PoseStack ms = new PoseStack();
        ms.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());
        if (blockEntity.isMultiblock) {
            ms.translate(-1, -2, -1);
            ms.scale(3, 3, 3);
            ms.translate(0.5, 0.5, 0.5);
            ms.mulPose(Axis.YP.rotationDegrees(180));
            ms.translate(-0.5, -0.5, -0.5);
        }

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
    }

    @Override
    public void updateLight(float partialTick) {
        relight(shaft);
        relight(body);
        for (TransformedInstance piston : pistons) {
            relight(piston);
        }
    }

    @Override
    protected void _delete() {
        shaft.delete();
        body.delete();
        for (TransformedInstance piston : pistons) {
            piston.delete();
        }
        pistons.clear();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(shaft);
        consumer.accept(body);
        for (TransformedInstance piston : pistons) {
            consumer.accept(piston);
        }
    }
}
