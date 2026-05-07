package dev.propulsionteam.propulsionsimulated.compat.computercraft;

import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import dev.propulsionteam.propulsionsimulated.content.platinum.CoralGeneratorBlockEntity;

public class CoralGeneratorPeripheral extends SyncedPeripheral<CoralGeneratorBlockEntity> {
    public CoralGeneratorPeripheral(CoralGeneratorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "coral_generator";
    }

    @LuaFunction
    public final int getCoralAmountMb() {
        return blockEntity.getCoralAmountMb();
    }

    @LuaFunction
    public final int getCoralCapacityMb() {
        return blockEntity.getCoralCapacityMb();
    }

    @LuaFunction
    public final int getEnergyAmountFe() {
        return blockEntity.getEnergyStoredFe();
    }

    @LuaFunction
    public final int getEnergyCapacityFe() {
        return blockEntity.getEnergyCapacityFe();
    }
}
