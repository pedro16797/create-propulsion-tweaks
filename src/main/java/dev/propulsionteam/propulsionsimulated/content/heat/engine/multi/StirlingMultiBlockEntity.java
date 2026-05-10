package dev.propulsionteam.propulsionsimulated.content.heat.engine.multi;

import dev.propulsionteam.propulsionsimulated.content.heat.engine.StirlingEngineBlockEntity;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class StirlingMultiBlockEntity extends GeneratingKineticBlockEntity {
    @Nullable
    public BlockPos controllerPos;
    protected BlockState originalState;
    protected CompoundTag originalTag;
    protected boolean isOutput = false;

    public StirlingMultiBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StirlingMultiBlockEntity(BlockPos pos, BlockState state) {
        this(PropulsionBlockEntities.STIRLING_MULTI_BLOCK_ENTITY.get(), pos, state);
    }

    public void setController(BlockPos pos) {
        this.controllerPos = pos;
        setChanged();
    }

    @Nullable
    public StirlingEngineBlockEntity getControllerBE() {
        if (level == null || controllerPos == null) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof StirlingEngineBlockEntity s ? s : null;
    }

    public void setOriginalData(BlockState state, @Nullable CompoundTag tag) {
        this.originalState = state;
        this.originalTag = tag;
        setChanged();
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public CompoundTag getOriginalTag() {
        return originalTag;
    }

    public void setOutput(boolean output) {
        this.isOutput = output;
        setChanged();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!isOutput) return 0;
        StirlingEngineBlockEntity controller = getControllerBE();
        return controller != null ? controller.getGeneratedSpeed() : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!isOutput) return 0;
        StirlingEngineBlockEntity controller = getControllerBE();
        return controller != null ? controller.calculateAddedStressCapacity() : 0;
    }


    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        if (controllerPos != null) compound.putLong("ControllerPos", controllerPos.asLong());
        if (originalState != null) compound.put("OriginalState", NbtUtils.writeBlockState(originalState));
        if (originalTag != null) compound.put("OriginalTag", originalTag);
        compound.putBoolean("IsOutput", isOutput);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("ControllerPos")) controllerPos = BlockPos.of(compound.getLong("ControllerPos"));
        if (compound.contains("OriginalState")) originalState = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), compound.getCompound("OriginalState"));
        if (compound.contains("OriginalTag")) originalTag = compound.getCompound("OriginalTag");
        isOutput = compound.getBoolean("IsOutput");
    }
}
