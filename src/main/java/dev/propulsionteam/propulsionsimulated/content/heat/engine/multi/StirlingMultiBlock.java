package dev.propulsionteam.propulsionsimulated.content.heat.engine.multi;

import dev.propulsionteam.propulsionsimulated.content.heat.engine.StirlingEngineBlockEntity;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nonnull;

public class StirlingMultiBlock extends KineticBlock implements IBE<StirlingMultiBlockEntity> {
    public static final BooleanProperty RENDER = BooleanProperty.create("render");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public StirlingMultiBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(RENDER, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RENDER, FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(RENDER) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public Class<StirlingMultiBlockEntity> getBlockEntityClass() {
        return StirlingMultiBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StirlingMultiBlockEntity> getBlockEntityType() {
        return PropulsionBlockEntities.STIRLING_MULTI_BLOCK_ENTITY.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof StirlingMultiBlockEntity multiBE && multiBE.isOutput) {
            return face == state.getValue(FACING);
        }
        return false;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StirlingMultiBlockEntity multiBE) {
                StirlingEngineBlockEntity controller = multiBE.getControllerBE();
                if (controller != null) {
                    controller.disassembleMulti(pos);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
