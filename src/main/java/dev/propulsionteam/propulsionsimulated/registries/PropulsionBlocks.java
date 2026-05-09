package dev.propulsionteam.propulsionsimulated.registries;

import dev.propulsionteam.propulsionsimulated.CreatePropulsion;
import dev.propulsionteam.propulsionsimulated.content.heat.burners.liquid.LiquidBurnerBlock;
import dev.propulsionteam.propulsionsimulated.content.heat.burners.solid.SolidBurnerBlock;
import dev.propulsionteam.propulsionsimulated.content.heat.engine.StirlingEngineBlock;
import dev.propulsionteam.propulsionsimulated.content.heat.engine.multi.StirlingMultiBlock;
import dev.propulsionteam.propulsionsimulated.content.redstone_transmission.RedstoneTransmissionBlock;
import dev.propulsionteam.propulsionsimulated.content.tilt_adapter.TiltAdapterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.creative_thruster.CreativeThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.creative_vector_thruster.CreativeVectorThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.thruster.ThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.IonThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.wing.CopycatWingBlock;
import dev.propulsionteam.propulsionsimulated.content.wing.CopycatWingItem;
import dev.propulsionteam.propulsionsimulated.content.wing.WingBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class PropulsionBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreatePropulsion.ID);
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(CreatePropulsion.ID);

    public static final DeferredBlock<ThrusterBlock> THRUSTER_BLOCK = BLOCKS.register("thruster",
        () -> new ThrusterBlock(Block.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
            .sound(SoundType.METAL).strength(5.5f, 4.0f).noOcclusion()));
    public static final DeferredBlock<CreativeThrusterBlock> CREATIVE_THRUSTER_BLOCK = BLOCKS.register("creative_thruster",
        () -> new CreativeThrusterBlock(Block.Properties.of().mapColor(MapColor.METAL)
            .sound(SoundType.METAL).strength(5.5f, 4.0f).noOcclusion()));
    public static final DeferredBlock<IonThrusterBlock> ION_THRUSTER_BLOCK = BLOCKS.register("ion_thruster",
        () -> new IonThrusterBlock(Block.Properties.of().mapColor(MapColor.METAL)
            .sound(SoundType.METAL).strength(5.5f, 4.0f).noOcclusion()));
    public static final DeferredBlock<VectorThrusterBlock> VECTOR_THRUSTER_BLOCK = BLOCKS.register("vector_thruster",
        () -> new VectorThrusterBlock(Block.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
            .sound(SoundType.METAL).strength(5.5f, 4.0f).noOcclusion()));
    public static final DeferredBlock<CreativeVectorThrusterBlock> CREATIVE_VECTOR_THRUSTER_BLOCK = BLOCKS.register("creative_vector_thruster",
        () -> new CreativeVectorThrusterBlock(Block.Properties.of().mapColor(MapColor.METAL)
            .sound(SoundType.METAL).strength(5.5f, 4.0f).noOcclusion()));
    public static final DeferredBlock<RedstoneTransmissionBlock> REDSTONE_TRANSMISSION_BLOCK = BLOCKS.register("redstone_transmission",
        () -> new RedstoneTransmissionBlock(Block.Properties.of().mapColor(MapColor.PODZOL)
            .sound(SoundType.METAL).strength(2.5f, 2.0f).noOcclusion()));
    public static final DeferredBlock<SolidBurnerBlock> SOLID_BURNER = BLOCKS.register("solid_burner",
        () -> new SolidBurnerBlock(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.COPPER)
            .requiresCorrectToolForDrops().strength(2.5f, 2.0f).lightLevel(s -> s.getValue(SolidBurnerBlock.LIT) ? 13 : 0)));
    public static final DeferredBlock<LiquidBurnerBlock> LIQUID_BURNER = BLOCKS.register("liquid_burner",
        () -> new LiquidBurnerBlock(Block.Properties.of().noOcclusion().mapColor(MapColor.STONE).sound(SoundType.COPPER)
            .requiresCorrectToolForDrops().strength(2.75f, 2.0f)));
    public static final DeferredBlock<StirlingEngineBlock> STIRLING_ENGINE_BLOCK = BLOCKS.register("stirling_engine",
        () -> new StirlingEngineBlock(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.COPPER)
            .requiresCorrectToolForDrops().strength(2.5f, 2.0f).noOcclusion()));
    public static final DeferredBlock<StirlingMultiBlock> STIRLING_MULTI = BLOCKS.register("stirling_multi",
        () -> new StirlingMultiBlock(Block.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL)
            .requiresCorrectToolForDrops().strength(5.5f, 4.0f).noOcclusion()));
    public static final DeferredBlock<TiltAdapterBlock> TILT_ADAPTER_BLOCK = BLOCKS.register("tilt_adapter",
        () -> new TiltAdapterBlock(Block.Properties.of().mapColor(MapColor.PODZOL)
            .sound(SoundType.METAL).strength(2.5f, 2.0f).noOcclusion()));

    public static final DeferredBlock<WingBlock> WING_BLOCK = BLOCKS.register("wing",
        () -> new WingBlock(Block.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).sound(SoundType.COPPER)
            .strength(1.5f, 2.0f).noOcclusion()));
    public static final DeferredBlock<WingBlock> TEMPERED_WING_BLOCK = BLOCKS.register("tempered_wing",
        () -> new WingBlock(Block.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).sound(SoundType.COPPER)
            .strength(1.5f, 2.0f).noOcclusion()));
    public static final DeferredBlock<CopycatWingBlock> COPYCAT_WING = BLOCKS.register("copycat_wing",
        () -> new CopycatWingBlock(Block.Properties.of().strength(1.5f, 2.0f), 4));
    public static final DeferredBlock<CopycatWingBlock> COPYCAT_WING_8 = BLOCKS.register("copycat_wing_8",
        () -> new CopycatWingBlock(Block.Properties.of().strength(1.5f, 2.0f), 8));
    public static final DeferredBlock<CopycatWingBlock> COPYCAT_WING_12 = BLOCKS.register("copycat_wing_12",
        () -> new CopycatWingBlock(Block.Properties.of().strength(1.5f, 2.0f), 12));
    static {
        registerDefaultBlockItem("thruster", THRUSTER_BLOCK);
        registerBlockItem("creative_thruster", CREATIVE_THRUSTER_BLOCK, new BlockItem.Properties().rarity(Rarity.EPIC));
        registerBlockItem("ion_thruster", ION_THRUSTER_BLOCK, new BlockItem.Properties().rarity(Rarity.UNCOMMON));
        registerBlockItem("vector_thruster", VECTOR_THRUSTER_BLOCK, new BlockItem.Properties().rarity(Rarity.UNCOMMON));
        registerBlockItem("creative_vector_thruster", CREATIVE_VECTOR_THRUSTER_BLOCK, new BlockItem.Properties().rarity(Rarity.EPIC));
        registerDefaultBlockItem("redstone_transmission", REDSTONE_TRANSMISSION_BLOCK);
        registerDefaultBlockItem("solid_burner", SOLID_BURNER);
        registerDefaultBlockItem("liquid_burner", LIQUID_BURNER);
        registerDefaultBlockItem("stirling_engine", STIRLING_ENGINE_BLOCK);
        registerDefaultBlockItem("stirling_multi", STIRLING_MULTI);
        registerDefaultBlockItem("tilt_adapter", TILT_ADAPTER_BLOCK);
        registerDefaultBlockItem("wing", WING_BLOCK);
        registerDefaultBlockItem("tempered_wing", TEMPERED_WING_BLOCK);
        BLOCK_ITEMS.register("copycat_wing", () -> new CopycatWingItem(COPYCAT_WING.get(), new BlockItem.Properties()));
        BLOCK_ITEMS.register("copycat_wing_8", () -> new CopycatWingItem(COPYCAT_WING_8.get(), new BlockItem.Properties()));
        BLOCK_ITEMS.register("copycat_wing_12", () -> new CopycatWingItem(COPYCAT_WING_12.get(), new BlockItem.Properties()));

        PropulsionDefaultStress.setImpact(ResourceLocation.fromNamespaceAndPath(CreatePropulsion.ID, "redstone_transmission"), 0, false);
        PropulsionDefaultStress.setImpact(ResourceLocation.fromNamespaceAndPath(CreatePropulsion.ID, "tilt_adapter"), 0, false);
    }

    private static <T extends Block> void registerDefaultBlockItem(String name, DeferredBlock<T> block) {
        registerBlockItem(name, block, new BlockItem.Properties());
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block, BlockItem.Properties properties) {
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), properties));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ITEMS.register(modBus);
    }
}
