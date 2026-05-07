package dev.propulsionteam.propulsionsimulated.content.platinum;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import dev.propulsionteam.propulsionsimulated.compat.PropulsionCompatibility;
import dev.propulsionteam.propulsionsimulated.compat.computercraft.ComputerBehaviour;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class CoralGeneratorBlockEntity extends SmartBlockEntity {
    public static final int ENERGY_CAPACITY = 1_000_000;
    public static final int FLUID_CAPACITY_MB = 8_000;
    public static final int FLUID_CONSUMPTION_PER_TICK_MB = 10;

    private int energyStored;
    private SmartFluidTankBehaviour tank;
    public AbstractComputerBehaviour computerBehaviour;

    private final IEnergyStorage energyHandler = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (maxExtract <= 0 || energyStored <= 0) {
                return 0;
            }
            int extracted = Math.min(maxExtract, energyStored);
            if (!simulate) {
                energyStored -= extracted;
                setChanged();
                notifyUpdate();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getMaxEnergyStored() {
            return ENERGY_CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public CoralGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(PropulsionBlockEntities.CORAL_GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, FLUID_CAPACITY_MB);
        behaviours.add(tank);
        if (PropulsionCompatibility.CC_ACTIVE) {
            behaviours.add(computerBehaviour = new ComputerBehaviour(this));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        if (energyStored >= ENERGY_CAPACITY || tank == null || tank.isEmpty()) {
            return;
        }

        FluidStack fluidStack = tank.getPrimaryHandler().getFluidInTank(0);
        CoralGeneratorFuelProperties properties = CoralGeneratorFuelManager.getProperties(fluidStack.getFluid());
        if (properties == null || properties.fePerMb() <= 0) {
            return;
        }

        if (fluidStack.getAmount() < FLUID_CONSUMPTION_PER_TICK_MB) {
            return;
        }

        int fePerConversion = properties.fePerMb() * FLUID_CONSUMPTION_PER_TICK_MB;
        if (energyStored + fePerConversion > ENERGY_CAPACITY) {
            return;
        }

        FluidStack drained = tank.getPrimaryHandler().drain(FLUID_CONSUMPTION_PER_TICK_MB, IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() < FLUID_CONSUMPTION_PER_TICK_MB) {
            return;
        }

        energyStored = Math.min(ENERGY_CAPACITY, energyStored + fePerConversion);
        setChanged();
        notifyUpdate();
    }

    public IFluidHandler getFluidHandler(Direction side) {
        if (tank == null) {
            return null;
        }
        if (side == null) {
            return tank.getPrimaryHandler();
        }
        return side.getAxis().isHorizontal() ? tank.getPrimaryHandler() : null;
    }

    public IEnergyStorage getEnergyHandler(Direction side) {
        if (side == null) {
            return energyHandler;
        }
        return side.getAxis() == Direction.Axis.Y ? energyHandler : null;
    }

    public int getCoralAmountMb() {
        if (tank == null) {
            return 0;
        }
        return tank.getPrimaryHandler().getFluidInTank(0).getAmount();
    }

    public int getCoralCapacityMb() {
        if (tank == null) {
            return FLUID_CAPACITY_MB;
        }
        return tank.getPrimaryHandler().getTankCapacity(0);
    }

    public int getEnergyStoredFe() {
        return energyStored;
    }

    public int getEnergyCapacityFe() {
        return ENERGY_CAPACITY;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("EnergyStored", energyStored);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        energyStored = Math.max(0, Math.min(ENERGY_CAPACITY, tag.getInt("EnergyStored")));
        super.read(tag, registries, clientPacket);
    }
}
