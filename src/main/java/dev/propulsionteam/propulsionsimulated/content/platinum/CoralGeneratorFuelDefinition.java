package dev.propulsionteam.propulsionsimulated.content.platinum;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public record CoralGeneratorFuelDefinition(
    ResourceLocation fluidId,
    int fePerMb,
    Optional<String> requiredMod
) {
    public static final Codec<CoralGeneratorFuelDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(CoralGeneratorFuelDefinition::fluidId),
            Codec.INT.fieldOf("fe_per_mb").forGetter(CoralGeneratorFuelDefinition::fePerMb),
            Codec.STRING.optionalFieldOf("required_mod").forGetter(CoralGeneratorFuelDefinition::requiredMod)
        ).apply(instance, CoralGeneratorFuelDefinition::new));

    public Fluid getFluid() {
        Fluid fluid = BuiltInRegistries.FLUID.get(this.fluidId);
        return fluid == null ? Fluids.EMPTY : fluid;
    }
}
