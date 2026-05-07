package dev.propulsionteam.propulsionsimulated.registries;

import javax.annotation.Nonnull;

import dev.propulsionteam.propulsionsimulated.CreatePropulsion;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;

public class PropulsionCreativeTab {
    private static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreatePropulsion.ID);

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE_TAB = REGISTER.register("base", 
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createpropulsion.base"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> new ItemStack(PropulsionBlocks.THRUSTER_BLOCK.get()))
            .displayItems(new RegistrateDisplayItemsGenerator())
            .build());

        public static void register(IEventBus modEventBus){
            REGISTER.register(modEventBus);
        }

        private static class RegistrateDisplayItemsGenerator implements DisplayItemsGenerator {
            public RegistrateDisplayItemsGenerator() {}

            @Override
            public void accept(@Nonnull ItemDisplayParameters parameters, @Nonnull Output output) {
                //From 0.1
                output.accept(PropulsionBlocks.THRUSTER_BLOCK.get());
                output.accept(PropulsionBlocks.CREATIVE_THRUSTER_BLOCK.get());
                output.accept(PropulsionBlocks.ION_THRUSTER_BLOCK.get());
                output.accept(PropulsionBlocks.VECTOR_THRUSTER_BLOCK.get());
                output.accept(PropulsionBlocks.CREATIVE_VECTOR_THRUSTER_BLOCK.get());
                //From 0.2
                output.accept(PropulsionBlocks.REDSTONE_TRANSMISSION_BLOCK.get());
                output.accept(PropulsionBlocks.REDSTONE_CONVERTER_BLOCK.get());
                //From 0.2 (items)
                output.accept(PropulsionItems.TURPENTINE_BUCKET.get());
                output.accept(PropulsionItems.PINE_RESIN.get());
                //From 0.2.2
                output.accept(PropulsionBlocks.WING_BLOCK.get());
                output.accept(PropulsionBlocks.TEMPERED_WING_BLOCK.get());
                output.accept(PropulsionBlocks.COPYCAT_WING.get());
                //From 0.3
                output.accept(PropulsionBlocks.SOLID_BURNER.get());
                output.accept(PropulsionBlocks.LIQUID_BURNER.get());
                //output.accept(PropulsionBlocks.REACTION_WHEEL_BLOCK);
                output.accept(PropulsionBlocks.STIRLING_ENGINE_BLOCK.get());
                output.accept(PropulsionBlocks.TILT_ADAPTER_BLOCK.get());
                //From 0.3 (items)
                output.accept(PropulsionBlocks.PLATINUM_ORE.get());
                output.accept(PropulsionBlocks.DEEPSLATE_PLATINUM_ORE.get());
                output.accept(PropulsionBlocks.PLATINUM_BLOCK.get());
                output.accept(PropulsionBlocks.RAW_PLATINUM_BLOCK.get());
                output.accept(PropulsionBlocks.CORAL_GENERATOR.get());
                output.accept(PropulsionItems.PLATINUM_INGOT.get());
                output.accept(PropulsionItems.PLATINUM_NUGGET.get());
                output.accept(PropulsionItems.PLATINUM_SHEET.get());
                output.accept(PropulsionItems.RAW_PLATINUM.get());
                output.accept(PropulsionItems.CORAL_BUCKET.get());
                output.accept(PropulsionBlocks.FE_CABLE.get());
            }
        }
}
