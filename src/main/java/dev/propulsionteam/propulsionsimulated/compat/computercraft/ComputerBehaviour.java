package dev.propulsionteam.propulsionsimulated.compat.computercraft;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;

import dev.propulsionteam.propulsionsimulated.content.heat.engine.StirlingEngineBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.platinum.CoralGeneratorBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.redstone_transmission.RedstoneTransmissionBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.tilt_adapter.TiltAdapterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.creative_thruster.CreativeThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.thruster.ThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterBlockEntity;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import dan200.computercraft.api.peripheral.IPeripheral;

public class ComputerBehaviour extends AbstractComputerBehaviour {
    protected IPeripheral peripheral;
    protected Supplier<IPeripheral> peripheralSupplier;

    private static final Map<Class<? extends SmartBlockEntity>, Function<SmartBlockEntity, IPeripheral>> PERIPHERAL_FACTORIES = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static <T extends SmartBlockEntity> void register(Class<T> clazz, Function<T, IPeripheral> factory) {
        PERIPHERAL_FACTORIES.put(clazz, be -> factory.apply((T) be));
    }

    static {
        register(ThrusterBlockEntity.class, ThrusterPeripheral::new);
        register(VectorThrusterBlockEntity.class, VectorThrusterPeripheral::new);
        register(CreativeThrusterBlockEntity.class, CreativeThrusterPeripheral::new);
        register(CoralGeneratorBlockEntity.class, CoralGeneratorPeripheral::new);
        register(StirlingEngineBlockEntity.class, StirlingEnginePeripheral::new);
        register(RedstoneTransmissionBlockEntity.class, RedstoneTransmissionPeripheral::new);
        register(TiltAdapterBlockEntity.class, TiltAdapterPeripheral::new);
    }

    public ComputerBehaviour(SmartBlockEntity blockEntity) {
        super(blockEntity);
        this.peripheralSupplier = getPeripheralFor(blockEntity);
    }

    public static Supplier<IPeripheral> getPeripheralFor(SmartBlockEntity blockEntity) {
        Class<?> current = blockEntity.getClass();
        while (current != null && SmartBlockEntity.class.isAssignableFrom(current)) {
            @SuppressWarnings("unchecked")
            Function<SmartBlockEntity, IPeripheral> factory =
                PERIPHERAL_FACTORIES.get((Class<? extends SmartBlockEntity>) current);
            if (factory != null) {
                return () -> factory.apply(blockEntity);
            }
            current = current.getSuperclass();
        }

        throw new IllegalArgumentException("No peripheral available for " + blockEntity.getType());
    }

    @Override
    public IPeripheral getPeripheralCapability() {
        if (peripheral == null)
            peripheral = peripheralSupplier.get();
        return peripheral;
    }

    @Override
    public void removePeripheral() {
        peripheral = null;
    }
}



