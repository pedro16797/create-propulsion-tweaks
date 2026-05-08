package dev.propulsionteam.propulsionsimulated.content.heat.engine;

import java.util.List;

import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.compat.PropulsionCompatibility;
import dev.propulsionteam.propulsionsimulated.compat.computercraft.ComputerBehaviour;
import dev.propulsionteam.propulsionsimulated.content.heat.IHeatConsumer;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlocks;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

public class StirlingEngineBlockEntity extends GeneratingKineticBlockEntity implements IHeatConsumer {
    public static final float MAX_GENERATED_RPM = 256.0f;
    public static final float HEAT_CONSUMPTION_RATE = 1.0f;

    protected StirlingScrollValueBehaviour targetSpeedBehaviour;
    @javax.annotation.Nullable
    protected BlockPos controllerPos;
    protected BlockPos structureOrigin;
    protected boolean isMultiblock = false;
    protected int heatSourceCount = 0;
    protected int superheatedCount = 0;
    protected BlockState originalState;
    protected CompoundTag originalNbt;
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
                tryAssemble3x3x3();
            }
        }

        if (isController() && isMultiblock) {
            if (level.getGameTime() % 20 == 0) {
                if (structureOrigin == null || !isValid3x3x3(structureOrigin)) {
                    disassembleMulti();
                }
            }
            if (isMultiblock) tickBlazeBurnerHeat();
        } else if (!isMultiblock) {
            tickBlazeBurnerHeat();
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
        return isMultiblock ? (controllerPos != null && controllerPos.equals(worldPosition)) : controllerPos == null;
    }

    @javax.annotation.Nullable
    public StirlingEngineBlockEntity getControllerBE() {
        if (isController() || level == null || controllerPos == null) return this;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof StirlingEngineBlockEntity s ? s : null;
    }

    protected void tryAssemble3x3x3() {
        BlockPos origin = worldPosition.offset(-1, -2, -1);
        if (isValid3x3x3(origin)) {
            formMulti(origin);
        }
    }

    private boolean isBlock(BlockPos pos, String id) {
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString().equals(id);
    }

    protected boolean isValid3x3x3(BlockPos origin) {
        Direction facing = getBlockState().getValue(StirlingEngineBlock.HORIZONTAL_FACING);
        Direction.Axis axis = facing.getAxis();

        // Bottom layer: 3x3 copper blocks
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if (!isBlock(origin.offset(x, 0, z), "minecraft:copper_block")) return false;
            }
        }

        // Center layer
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                BlockPos pos = origin.offset(x, 1, z);
                BlockState state = level.getBlockState(pos);
                if (x == 1 && z == 1) { // Center: large cogwheel
                    if (!isBlock(pos, "create:large_cogwheel")) return false;
                    if (!state.hasProperty(RotatedPillarKineticBlock.AXIS) || state.getValue(RotatedPillarKineticBlock.AXIS) != axis) return false;
                } else if ((x == 0 || x == 2) && (z == 0 || z == 2)) { // Corners: andesite alloy
                    if (!isBlock(pos, "create:andesite_alloy_block")) return false;
                } else { // Sides
                    boolean isFrontBack = false;
                    if (axis == Direction.Axis.X) {
                        if (z == 1) isFrontBack = true;
                    } else {
                        if (x == 1) isFrontBack = true;
                    }

                    if (isFrontBack) { // Front/Back: shafts
                        if (!isBlock(pos, "create:shaft")) return false;
                        if (!state.hasProperty(RotatedPillarKineticBlock.AXIS) || state.getValue(RotatedPillarKineticBlock.AXIS) != axis) return false;
                    } else { // Sides: sturdy blocks
                        if (!isBlock(pos, "create:railway_casing")) return false;
                    }
                }
            }
        }

        // Top layer: 3x3 sturdy blocks
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                BlockPos pos = origin.offset(x, 2, z);
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof StirlingEngineBlock) {
                    if (state.getValue(StirlingEngineBlock.HORIZONTAL_FACING) != facing) return false;
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof StirlingEngineBlockEntity s && s.isMultiblock && s.controllerPos != null && !s.controllerPos.equals(worldPosition)) return false;
                } else {
                    if (!isBlock(pos, "create:railway_casing")) return false;
                }
            }
        }

        return true;
    }

    protected void formMulti(BlockPos origin) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 3; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState oldState = level.getBlockState(pos);
                    CompoundTag oldNbt = null;
                    BlockEntity oldBe = level.getBlockEntity(pos);
                    if (oldBe != null) {
                        oldNbt = oldBe.saveWithFullMetadata(level.registryAccess());
                    }

                    if (!pos.equals(worldPosition)) {
                        level.setBlock(pos, PropulsionBlocks.STIRLING_ENGINE_BLOCK.get().defaultBlockState()
                            .setValue(StirlingEngineBlock.HORIZONTAL_FACING, getBlockState().getValue(StirlingEngineBlock.HORIZONTAL_FACING))
                            .setValue(StirlingEngineBlock.MULTIBLOCK, true), 3);
                    } else {
                        level.setBlock(pos, oldState.setValue(StirlingEngineBlock.MULTIBLOCK, true), 3);
                    }

                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof StirlingEngineBlockEntity s) {
                        s.controllerPos = worldPosition;
                        s.structureOrigin = origin;
                        s.isMultiblock = true;
                        s.originalState = oldState;
                        s.originalNbt = oldNbt;
                        s.setChanged();
                        s.sendData();
                    }
                }
            }
        }
    }

    public void disassembleMulti() {
        if (!isController() || !isMultiblock) return;
        BlockPos origin = structureOrigin;
        if (origin == null) return;

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 3; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    BlockState restoreState = null;
                    CompoundTag restoreNbt = null;

                    if (be instanceof StirlingEngineBlockEntity s) {
                        restoreState = s.originalState;
                        restoreNbt = s.originalNbt;
                    }

                    if (restoreState != null) {
                        BlockState toSet = restoreState;
                        if (toSet.hasProperty(StirlingEngineBlock.MULTIBLOCK)) {
                            toSet = toSet.setValue(StirlingEngineBlock.MULTIBLOCK, false);
                        }
                        level.setBlock(pos, toSet, 3);
                        if (restoreNbt != null) {
                            BlockEntity newBe = level.getBlockEntity(pos);
                            if (newBe != null) {
                                newBe.loadWithComponents(restoreNbt, level.registryAccess());
                            }
                        }
                    } else {
                        // Fallback if something went wrong
                        BlockState currentState = level.getBlockState(pos);
                        if (currentState.hasProperty(StirlingEngineBlock.MULTIBLOCK)) {
                            level.setBlock(pos, currentState.setValue(StirlingEngineBlock.MULTIBLOCK, false), 3);
                        }
                    }

                    be = level.getBlockEntity(pos);
                    if (be instanceof StirlingEngineBlockEntity s) {
                        s.isMultiblock = false;
                        s.heatSourceCount = 0;
                        s.superheatedCount = 0;
                        s.controllerPos = null;
                        s.structureOrigin = null;
                        s.originalState = null;
                        s.originalNbt = null;
                        s.updateConnectivity = true;
                        s.updateGeneratedRotation();
                        s.setChanged();
                        s.sendData();
                    }
                }
            }
        }
    }

    private void tickBlazeBurnerHeat() {
        if (!isEngineActive()) return;
        if (!PropulsionConfig.BLAZE_BURNERS_HEAT_STIRLING_ENGINES.get()) return;

        boolean wasInactive = activeTicks == 0;
        int prevHeat = heatSourceCount;
        int prevSuper = superheatedCount;

        if (isMultiblock) {
            if (isController()) {
                BlockPos origin = structureOrigin;
                if (origin == null) return;
                heatSourceCount = 0;
                superheatedCount = 0;
                for (int x = 0; x < 3; x++) {
                    for (int z = 0; z < 3; z++) {
                        BlockPos belowPos = origin.offset(x, -1, z);
                        BlockState below = level.getBlockState(belowPos);
                        if (below.getBlock() instanceof BlazeBurnerBlock && below.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
                            HeatLevel heat = below.getValue(BlazeBurnerBlock.HEAT_LEVEL);
                            if (heat.isAtLeast(HeatLevel.FADING)) {
                                heatSourceCount++;
                                if (heat.isAtLeast(HeatLevel.SEETHING)) superheatedCount++;
                            }
                        }
                    }
                }
                if (heatSourceCount > 0) activeTicks = 3;
            }
        } else {
            BlockState below = level.getBlockState(worldPosition.below());
            if (below.getBlock() instanceof BlazeBurnerBlock && below.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
                HeatLevel heat = below.getValue(BlazeBurnerBlock.HEAT_LEVEL);
                if (heat.isAtLeast(HeatLevel.FADING)) {
                    activeTicks = 3;
                    heatSourceCount = 1;
                    superheatedCount = heat.isAtLeast(HeatLevel.SEETHING) ? 1 : 0;
                } else {
                    heatSourceCount = 0;
                    superheatedCount = 0;
                }
            } else {
                heatSourceCount = 0;
                superheatedCount = 0;
            }
        }

        if ((wasInactive && activeTicks > 0) || (prevHeat != heatSourceCount) || (prevSuper != superheatedCount)) {
            updateGeneratedRotation();
            sendData();
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
        if (isMultiblock && !isController()) {
            StirlingEngineBlockEntity controller = getControllerBE();
            if (controller != null) return controller.consumeHeat(maxAvailable, expectedHeatOutput, simulate);
        }

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
    public float calculateAddedStressCapacity() {
        if (!isController()) return 0f;
        if (!isEngineActive() || activeTicks <= 0) return 0f;
        float rpm = targetSpeedBehaviour.getUnsignedRPM();
        if (rpm == 0) return 0f; 

        float stressFactor = MAX_GENERATED_RPM / rpm;

        float capacity;
        if (isMultiblock) {
            capacity = (heatSourceCount * 64f + superheatedCount * 64f) * stressFactor;
        } else {
            float baseCapacity = PropulsionConfig.STIRLING_GENERATED_SU.get().floatValue();
            capacity = stressFactor * baseCapacity;
            if (superheatedCount > 0) capacity *= 2f;
        }

        this.lastCapacityProvided = capacity;
        return capacity;
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
        compound.putInt("heatSourceCount", heatSourceCount);
        compound.putInt("superheatedCount", superheatedCount);
        if (controllerPos != null) {
            compound.putLong("controllerPos", controllerPos.asLong());
        }
        if (structureOrigin != null) {
            compound.putLong("structureOrigin", structureOrigin.asLong());
        }
        if (originalState != null) {
            compound.put("originalState", NbtUtils.writeBlockState(originalState));
        }
        if (originalNbt != null) {
            compound.put("originalNbt", originalNbt);
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
        heatSourceCount = compound.getInt("heatSourceCount");
        superheatedCount = compound.getInt("superheatedCount");
        if (compound.contains("controllerPos")) {
            controllerPos = BlockPos.of(compound.getLong("controllerPos"));
        } else {
            controllerPos = null;
        }
        if (compound.contains("structureOrigin")) {
            structureOrigin = BlockPos.of(compound.getLong("structureOrigin"));
        } else {
            structureOrigin = null;
        }
        if (compound.contains("originalState")) {
            originalState = NbtUtils.readBlockState(level != null ? level.holderLookup(net.minecraft.core.registries.Registries.BLOCK) : registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK), compound.getCompound("originalState"));
        }
        if (compound.contains("originalNbt")) {
            originalNbt = compound.getCompound("originalNbt");
        }
    }
}

