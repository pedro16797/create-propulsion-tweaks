package dev.propulsionteam.propulsionsimulated.content.heat.engine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionShapes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StirlingEngineBlock extends HorizontalKineticBlock implements IBE<StirlingEngineBlockEntity> {
    public static final BooleanProperty MULTIBLOCK = BooleanProperty.create("multi");

    public StirlingEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(MULTIBLOCK, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        boolean isSneaking = player != null && player.isShiftKeyDown();
        Direction preferred = null;

        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState neighborState = level.getBlockState(pos.relative(side));
            if (neighborState.getBlock() instanceof IRotate neighborRotate) {
                if (neighborRotate.hasShaftTowards(level, pos.relative(side), neighborState, side.getOpposite())) {
                    if (preferred != null && preferred != side) {
                        preferred = null;
                        break;
                    }
                    preferred = side;
                }
            }
        }

        if (preferred != null && !isSneaking) {
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferred);
        }
        
        Direction placedFacing = context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(HORIZONTAL_FACING, placedFacing);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof StirlingEngineBlockEntity engine && engine.isMultiblock) {
            return false;
        }
        return face == state.getValue(HORIZONTAL_FACING);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MULTIBLOCK);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public VoxelShape getShape(@Nullable BlockState pState, @Nullable BlockGetter pLevel, @Nullable BlockPos pPos, @Nullable CollisionContext pContext) {
        if (pState == null) {
            return PropulsionShapes.STIRLING_ENGINE.get(Direction.NORTH);
        }
        if (pState.getValue(MULTIBLOCK)) {
            return Shapes.block();
        }
        Direction direction = pState.getValue(HORIZONTAL_FACING);
        return PropulsionShapes.STIRLING_ENGINE.get(direction);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(MULTIBLOCK) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }
    
    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StirlingEngineBlockEntity engine) {
            engine.setPowered(level.hasNeighborSignal(pos));
            if (!engine.isMultiblock) {
                engine.updateConnectivity = true;
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StirlingEngineBlockEntity engine) {
            engine.updateConnectivity = true;
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StirlingEngineBlockEntity engine) {
                StirlingEngineBlockEntity controller = engine.isController() ? engine : engine.getControllerBE();
                if (controller != null) {
                    controller.disassembleMulti(pos);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<StirlingEngineBlockEntity> getBlockEntityClass() {
        return StirlingEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<StirlingEngineBlockEntity> getBlockEntityType() {
        return PropulsionBlockEntities.STIRLING_ENGINE_BLOCK_ENTITY.get();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == getBlockEntityType()) {
            return new SmartBlockEntityTicker<>();
        }
        return null;
    }
}
