package dev.propulsionteam.propulsionsimulated.content.platinum;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;

public class CoralGeneratorBlock extends Block implements IBE<CoralGeneratorBlockEntity> {
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(1, 0, 1, 15, 16, 15),
        Block.box(0, 1, 0, 16, 15, 16)
    );

    public CoralGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<CoralGeneratorBlockEntity> getBlockEntityClass() {
        return CoralGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CoralGeneratorBlockEntity> getBlockEntityType() {
        return PropulsionBlockEntities.CORAL_GENERATOR_BLOCK_ENTITY.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoralGeneratorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == PropulsionBlockEntities.CORAL_GENERATOR_BLOCK_ENTITY.get()) {
            return new SmartBlockEntityTicker<>();
        }
        return null;
    }
}
