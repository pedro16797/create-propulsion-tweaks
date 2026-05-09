package dev.propulsionteam.propulsionsimulated.content.heat.engine;

import java.util.List;

import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.compat.PropulsionCompatibility;
import dev.propulsionteam.propulsionsimulated.compat.computercraft.ComputerBehaviour;
import dev.propulsionteam.propulsionsimulated.content.heat.IHeatConsumer;
import dev.propulsionteam.propulsionsimulated.content.heat.engine.multi.StirlingMultiBlock;
import dev.propulsionteam.propulsionsimulated.content.heat.engine.multi.StirlingMultiBlockEntity;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlocks;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StirlingEngineBlockEntity extends GeneratingKineticBlockEntity implements IHeatConsumer {
    public static final float MAX_GENERATED_RPM = 256.0f;
    public static final float HEAT_CONSUMPTION_RATE = 1.0f;

    protected StirlingScrollValueBehaviour targetSpeedBehaviour;
    @javax.annotation.Nullable
    protected BlockPos controllerPos;
    protected boolean isMultiblock = false;
    protected boolean updateConnectivity = true;

    private int activeTicks = 0;
    private boolean firstTick = true;

    private boolean isPowered = false;
    private boolean computerActive = true;
    private boolean wasEngineActive = true;

    public AbstractComputerBehaviour computerBehaviour;

    public StirlingEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StirlingEngineBlockEntity(BlockPos pos, BlockState state) {
        this(PropulsionBlockEntities.STIRLING_ENGINE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        wasEngineActive = isEngineActive();
        if (activeTicks > 0 || getGeneratedSpeed() > getTheoreticalSpeed()) {
            updateGeneratedRotation();
        }
    }

    public StirlingScrollValueBehaviour getTargetSpeedBehaviour() { return targetSpeedBehaviour; }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviors) {
        super.addBehaviours(behaviors);
        targetSpeedBehaviour = new StirlingScrollValueBehaviour(Component.translatable("createpropulsion.stirling_engine.generated_speed"), this, new StirlingEngineValueBox());
        targetSpeedBehaviour.value = 4;
        targetSpeedBehaviour.withCallback(i -> this.updateGeneratedRotation());
        behaviors.add(targetSpeedBehaviour);

        if (PropulsionCompatibility.CC_ACTIVE) {
            behaviors.add(computerBehaviour = new ComputerBehaviour(this));
        }
    }

    public void setPowered(boolean powered) {
        this.isPowered = powered;
    }

    public void setComputerActive(boolean active) {
        this.computerActive = active;
    }

    public boolean isEngineActive() {
        if (PropulsionCompatibility.CC_ACTIVE && computerBehaviour != null && computerBehaviour.hasAttachedComputer()) {
            return computerActive;
        }
        return !isPowered;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) return;

        if (firstTick) {
            firstTick = false;
            isPowered = level.hasNeighborSignal(getBlockPos());
            wasEngineActive = isEngineActive();
            if (activeTicks > 0) {
                reActivateSource = true;
            }
        }

        if (updateConnectivity) {
            updateConnectivity = false;
            if (isController() && !isMultiblock) {
                tryAssemble();
            }
        }

        if (isController() && isMultiblock) {
            if (!isValidMultiblock(worldPosition.offset(-1, -2, -1))) {
                disassembleMulti();
            }
        }

        boolean currentlyActive = isEngineActive();
        if (wasEngineActive != currentlyActive) {
            wasEngineActive = currentlyActive;
            updateGeneratedRotation();
        }

        if (activeTicks > 0) {
            activeTicks--;
            if (activeTicks == 0) {
                updateGeneratedRotation();
            }
        }

        tickBlazeBurnerHeat();
    }

    public boolean isController() {
        return controllerPos == null;
    }

    @javax.annotation.Nullable
    public StirlingEngineBlockEntity getControllerBE() {
        if (isController() || level == null) return this;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof StirlingEngineBlockEntity s ? s : null;
    }

    protected void tryAssemble() {
        Direction facing = getBlockState().getValue(StirlingEngineBlock.HORIZONTAL_FACING);
        BlockPos origin = worldPosition.offset(-1, -2, -1);
        if (isValidCube(origin, facing)) {
            formMulti(origin, facing);
        }
    }

    protected boolean isValidCube(BlockPos origin, Direction facing) {
        // Bottom layer (y=0)
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if (!isBlock(origin.offset(x, 0, z), "minecraft:copper_block")) return false;
            }
        }

        // Middle layer (y=1)
        if (!isBlock(origin.offset(0, 1, 0), "create:fluid_tank")) return false;
        if (!isBlock(origin.offset(2, 1, 0), "create:fluid_tank")) return false;
        if (!isBlock(origin.offset(0, 1, 2), "create:fluid_tank")) return false;
        if (!isBlock(origin.offset(2, 1, 2), "create:fluid_tank")) return false;

        BlockPos center = origin.offset(1, 1, 1);
        BlockPos front = center.relative(facing);
        BlockPos back = center.relative(facing.getOpposite());
        BlockPos left = center.relative(facing.getCounterClockWise());
        BlockPos right = center.relative(facing.getClockWise());

        if (!isBlock(center, "create:andesite_alloy_block")) return false;
        if (!isBlock(front, "create:andesite_alloy_block")) return false;
        if (!isBlock(back, "create:railway_casing")) return false;
        if (!isBlock(left, "create:railway_casing")) return false;
        if (!isBlock(right, "create:railway_casing")) return false;

        // Top layer (y=2)
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                BlockPos pos = origin.offset(x, 2, z);
                if (x == 1 && z == 1) {
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof StirlingEngineBlock)) return false;
                    if (state.getValue(StirlingEngineBlock.HORIZONTAL_FACING) != facing) return false;
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof StirlingEngineBlockEntity s && s.isMultiblock && s != this) return false;
                } else {
                    if (!isBlock(pos, "create:railway_casing")) return false;
                }
            }
        }
        return true;
    }

    private boolean isBlock(BlockPos pos, String id) {
        if (level == null) return false;
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString().equals(id);
    }

    protected void formMulti(BlockPos origin, Direction facing) {
        level.setBlock(worldPosition, getBlockState().setValue(StirlingEngineBlock.MULTIBLOCK, true), 3);
        BlockPos outputPos = origin.offset(1, 1, 1).relative(facing);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (x == 1 && y == 2 && z == 1) continue;

                    BlockState oldState = level.getBlockState(pos);
                    CompoundTag oldTag = null;
                    BlockEntity oldBE = level.getBlockEntity(pos);
                    if (oldBE != null) {
                        oldTag = oldBE.saveWithFullMetadata(level.registryAccess());
                    }

                    boolean isOutput = pos.equals(outputPos);
                    level.setBlock(pos, PropulsionBlocks.STIRLING_MULTI.get().defaultBlockState()
                        .setValue(StirlingMultiBlock.RENDER, true)
                        .setValue(StirlingMultiBlock.FACING, facing), 3);

                    BlockEntity newBE = level.getBlockEntity(pos);
                    if (newBE instanceof StirlingMultiBlockEntity multiBE) {
                        multiBE.setController(worldPosition);
                        multiBE.setOriginalData(oldState, oldTag);
                        multiBE.setOutput(isOutput);
                    }
                }
            }
        }
        this.isMultiblock = true;
        this.updateGeneratedRotation();
        this.setChanged();
        this.sendData();
    }

    public void disassembleMulti() {
        if (!isController() || !isMultiblock) return;
        this.isMultiblock = false;
        level.setBlock(worldPosition, getBlockState().setValue(StirlingEngineBlock.MULTIBLOCK, false), 3);
        BlockPos origin = worldPosition.offset(-1, -2, -1);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (x == 1 && y == 2 && z == 1) continue;
                    BlockPos pos = origin.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof StirlingMultiBlockEntity multiBE) {
                        BlockState oldState = multiBE.getOriginalState();
                        CompoundTag oldTag = multiBE.getOriginalTag();
                        level.setBlock(pos, oldState, 3);
                        if (oldTag != null) {
                            BlockEntity restoredBE = level.getBlockEntity(pos);
                            if (restoredBE != null) {
                                restoredBE.loadWithComponents(oldTag, level.registryAccess());
                            }
                        }
                    }
                }
            }
        }
        this.updateGeneratedRotation();
        this.setChanged();
        this.sendData();
    }

    protected boolean isValidMultiblock(BlockPos origin) {
        Direction facing = getBlockState().getValue(StirlingEngineBlock.HORIZONTAL_FACING);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (x == 1 && y == 2 && z == 1) {
                        BlockState state = level.getBlockState(pos);
                        if (!(state.getBlock() instanceof StirlingEngineBlock)) return false;
                        if (state.getValue(StirlingEngineBlock.HORIZONTAL_FACING) != facing) return false;
                        BlockEntity be = level.getBlockEntity(pos);
                        if (!(be instanceof StirlingEngineBlockEntity s) || s != this) return false;
                    } else {
                        BlockState state = level.getBlockState(pos);
                        if (!(state.getBlock() instanceof StirlingMultiBlock)) return false;
                        if (state.getValue(StirlingMultiBlock.FACING) != facing) return false;
                        BlockEntity be = level.getBlockEntity(pos);
                        if (!(be instanceof StirlingMultiBlockEntity multiBE) || !worldPosition.equals(multiBE.controllerPos)) return false;
                    }
                }
            }
        }
        return true;
    }

    private void tickBlazeBurnerHeat() {
        if (!isEngineActive()) return;
        if (!PropulsionConfig.BLAZE_BURNERS_HEAT_STIRLING_ENGINES.get()) return;

        if (isMultiblock) {
            if (isController()) {
                BlockPos origin = worldPosition.offset(-1, -2, -1);
                boolean anyHeat = false;
                for (int x = 0; x < 3; x++) {
                    for (int z = 0; z < 3; z++) {
                        BlockPos heatPos = origin.offset(x, -1, z);
                        if (getHeatLevel(level, heatPos, level.getBlockState(heatPos)).isAtLeast(HeatLevel.KINDLED)) {
                            anyHeat = true;
                            break;
                        }
                    }
                    if (anyHeat) break;
                }
                if (anyHeat) {
                    boolean wasInactive = activeTicks == 0;
                    activeTicks = 3;
                    if (wasInactive) updateGeneratedRotation();
                }
            }
            return;
        }

        BlockState below = level.getBlockState(worldPosition.below());
        if (!(below.getBlock() instanceof BlazeBurnerBlock)) return;
        if (!below.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) return;
        if (!below.getValue(BlazeBurnerBlock.HEAT_LEVEL).isAtLeast(HeatLevel.FADING)) return;

        boolean wasInactive = activeTicks == 0;
        activeTicks = 3;
        if (wasInactive) {
            updateGeneratedRotation();
        }
    }

    @Override
    public boolean isActive() {
        return isEngineActive(); 
    }

    @Override
    public float getOperatingThreshold() {
        return 0.1f;
    }

    @Override
    public float consumeHeat(float maxAvailable, float expectedHeatOutput, boolean simulate) {
        if (!isEngineActive()) return 0f;

        float rpm = targetSpeedBehaviour.getUnsignedRPM();
        float modeConsumptionFactor = rpm / MAX_GENERATED_RPM;
        float toConsume = Math.min(modeConsumptionFactor * HEAT_CONSUMPTION_RATE, maxAvailable); //Do not care about expected heat output (we always consume less than it)

        if (!simulate && toConsume > 0) {
            boolean wasInactive = activeTicks == 0;
            activeTicks = 3;

            //We were off, but now we are activate -> update rotation
            if (wasInactive) {
                updateGeneratedRotation();
            }
        }

        return toConsume;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!isController()) return 0f;
        if (!isEngineActive() || activeTicks <= 0) return 0f;
        int generatedRPM = targetSpeedBehaviour.getRPM();
        return convertToDirection(generatedRPM, getBlockState().getValue(StirlingEngineBlock.HORIZONTAL_FACING));
    }

    @Override
    public void updateGeneratedRotation() {
        super.updateGeneratedRotation();
        if (isController() && isMultiblock && level != null) {
            BlockPos origin = worldPosition.offset(-1, -2, -1);
            Direction facing = getBlockState().getValue(StirlingEngineBlock.HORIZONTAL_FACING);
            BlockPos outputPos = origin.offset(1, 1, 1).relative(facing);
            BlockEntity be = level.getBlockEntity(outputPos);
            if (be instanceof StirlingMultiBlockEntity multiBE) {
                multiBE.updateGeneratedRotation();
            }
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!isController()) return 0f;
        if (!isEngineActive() || activeTicks <= 0) return 0f;
        float rpm = targetSpeedBehaviour.getUnsignedRPM();
        if (rpm == 0) return 0f;

        if (isMultiblock) {
            float totalCapacity = 0;
            BlockPos origin = worldPosition.offset(-1, -2, -1);
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos heatPos = origin.offset(x, -1, z);
                    BlockState heatState = level.getBlockState(heatPos);
                    HeatLevel heat = getHeatLevel(level, heatPos, heatState);
                    if (heat == HeatLevel.SEETHING) {
                        totalCapacity += 32768;
                    } else if (heat.isAtLeast(HeatLevel.KINDLED)) {
                        totalCapacity += 16384;
                    }
                }
            }
            this.lastCapacityProvided = totalCapacity;
            return totalCapacity;
        }

        float stressFactor = MAX_GENERATED_RPM / rpm;
        float baseCapacity = PropulsionConfig.STIRLING_GENERATED_SU.get().floatValue();
        float capacity = stressFactor * baseCapacity;

        this.lastCapacityProvided = capacity;
        return capacity;
    }

    public static HeatLevel getHeatLevel(Level level, BlockPos pos, BlockState state) {
        if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            return state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
        }
        return HeatLevel.NONE;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean active = isEngineActive();

        String status;
        ChatFormatting statusColor;
        if (active) {
            if (activeTicks == 0) {
                status = "createpropulsion.gui.goggles.stirling_engine.status.no_heat";
                statusColor = ChatFormatting.GOLD;
            } else {
                status = "createpropulsion.gui.goggles.stirling_engine.status.on";
                statusColor = ChatFormatting.GREEN;
            }
        } else {
            status = "createpropulsion.gui.goggles.stirling_engine.status.off";
            statusColor = ChatFormatting.RED;
        }

        CreateLang.builder()
            .add(Component.translatable("createpropulsion.gui.goggles.stirling_engine.status"))
            .text(": ")
            .add(Component.translatable(status).withStyle(statusColor))
            .forGoggles(tooltip);

        if (PropulsionCompatibility.CC_ACTIVE && computerBehaviour != null && computerBehaviour.hasAttachedComputer()) {
            CreateLang.builder()
                .add(Component.translatable("createpropulsion.gui.goggles.cc.peripheral_controlled"))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        }

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return true;
    }

    @Override
    protected void write(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("activeTicks", activeTicks);
        compound.putBoolean("isPowered", isPowered);
        compound.putBoolean("computerActive", computerActive);
        compound.putBoolean("isMultiblock", isMultiblock);
        if (controllerPos != null) {
            compound.putLong("controllerPos", controllerPos.asLong());
        }
    }

    @Override
    protected void read(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        activeTicks = compound.getInt("activeTicks");
        isPowered = compound.getBoolean("isPowered");
        if (compound.contains("computerActive")) {
            computerActive = compound.getBoolean("computerActive");
        } else {
            computerActive = true;
        }
        isMultiblock = compound.getBoolean("isMultiblock");
        if (compound.contains("controllerPos")) {
            controllerPos = BlockPos.of(compound.getLong("controllerPos"));
        } else {
            controllerPos = null;
        }
    }
}

