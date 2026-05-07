package dev.propulsionteam.propulsionsimulated;

import dev.propulsionteam.propulsionsimulated.registries.PropulsionDefaultStress;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropulsionConfig {
    public static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // ── Thruster (reference-style typed values for new code) ────────────────
    public static final ModConfigSpec.DoubleValue BASE_THRUST;
    public static final ModConfigSpec.IntValue OBSTRUCTION_SCAN_LENGTH;
    public static final ModConfigSpec.BooleanValue OBSTRUCTION_IGNORE_OTHER_SUBLEVELS;
    public static final ModConfigSpec.BooleanValue REQUIRE_FUEL;
    public static final ModConfigSpec.IntValue FUEL_TANK_CAPACITY_MB;
    public static final ModConfigSpec.DoubleValue CREATIVE_THRUSTER_BASE_THRUST;
    public static final ModConfigSpec.DoubleValue CREATIVE_THRUSTER_MAX_THRUST;
    public static final ModConfigSpec.DoubleValue CREATIVE_VECTOR_THRUSTER_BASE_THRUST;
    public static final ModConfigSpec.DoubleValue CREATIVE_VECTOR_THRUSTER_MAX_THRUST;
    public static final ModConfigSpec.DoubleValue FUEL_MB_PER_TICK_AT_FULL_THROTTLE;
    public static final ModConfigSpec.IntValue ION_THRUSTER_ENERGY_CAPACITY_FE;
    public static final ModConfigSpec.DoubleValue ION_THRUSTER_FE_PER_TICK_AT_FULL_THROTTLE;
    public static final ModConfigSpec.DoubleValue ION_THRUSTER_BASE_THRUST;
    public static final ModConfigSpec.DoubleValue VECTOR_THRUSTER_BASE_THRUST;
    public static final ModConfigSpec.DoubleValue MULTIBLOCK_2X_THRUST_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MULTIBLOCK_3X_THRUST_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MULTIBLOCK_2X_FUEL_EFFICIENCY;
    public static final ModConfigSpec.DoubleValue MULTIBLOCK_3X_FUEL_EFFICIENCY;
    public static final ModConfigSpec.BooleanValue DAMAGE_ENTITIES;
    public static final ModConfigSpec.IntValue DAMAGE_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue NOZZLE_OFFSET_FROM_CENTER;
    public static final ModConfigSpec.BooleanValue USE_ATMOSPHERIC_PRESSURE;
    public static final ModConfigSpec.DoubleValue ATMOSPHERIC_PRESSURE_AMOUNT;
    public static final ModConfigSpec.IntValue CLIENT_PARTICLES_PER_TICK;
    public static final ModConfigSpec.DoubleValue GROUND_FRICTION_COEFFICIENT;
    public static final ModConfigSpec.DoubleValue GROUND_LINEAR_DRAG;
    public static final ModConfigSpec.DoubleValue GROUND_ROLLING_RESISTANCE;
    public static final ModConfigSpec.DoubleValue GROUNDED_SPEED_DEADZONE;
    public static final ModConfigSpec.DoubleValue GROUND_PROBE_DISTANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_PROPERTIES;
    public static final Map<String, ModConfigSpec.ConfigValue<String>> THRUSTER_DYE_COLORS = new LinkedHashMap<>();
    public static final ModConfigSpec.DoubleValue TILT_ADAPTER_MAX_ANGLE;
    public static final ModConfigSpec.IntValue CABLE_ENERGY_TRANSFER;

    public static final ModConfigSpec.BooleanValue DEBUG_THRUSTER;
    public static final ModConfigSpec.BooleanValue DEBUG_BURNER;
    public static final ModConfigSpec.BooleanValue DEBUG_MAGNET;

    // Optical sensors
    public static final ModConfigSpec.ConfigValue<Integer> OPTICAL_SENSOR_TICKS_PER_UPDATE;
    public static final ModConfigSpec.ConfigValue<Integer> INLINE_OPTICAL_SENSOR_MAX_DISTANCE;
    public static final ModConfigSpec.ConfigValue<Integer> OPTICAL_SENSOR_MAX_DISTANCE;

    // Wings
    public static final ModConfigSpec.ConfigValue<Double> BASE_WING_LIFT;
    public static final ModConfigSpec.ConfigValue<Double> BASE_WING_DRAG;

    // Stirling engine
    public static final ModConfigSpec.ConfigValue<Double> STIRLING_GENERATED_SU;
    public static final ModConfigSpec.ConfigValue<Double> TILT_ADAPTER_ANGLE_RANGE;
    public static final ModConfigSpec.ConfigValue<Double> STIRLING_REVOLUTION_PERIOD;
    public static final ModConfigSpec.ConfigValue<Double> STIRLING_CRANK_RADIUS;
    public static final ModConfigSpec.ConfigValue<Double> STIRLING_CONROD_LENGTH;

    // Burners
    public static final ModConfigSpec.ConfigValue<Boolean> BURNERS_POWER_HEATED_MIXERS;
    public static final ModConfigSpec.ConfigValue<Boolean> BURNERS_HEAT_STEAM_ENGINES;
    public static final ModConfigSpec.ConfigValue<Boolean> BURNERS_SUPERHEAT_STEAM_ENGINES;
    public static final ModConfigSpec.ConfigValue<Boolean> BLAZE_BURNERS_HEAT_STIRLING_ENGINES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_EFFICIENCY_RATES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_BURN_RATE_RATES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CORAL_FUEL_CONVERSION_RATES;

    static {
        //#region Server
        SERVER_BUILDER.push("thruster");

        BASE_THRUST = SERVER_BUILDER.comment("Base thrust at redstone 15 and full obstruction efficiency for the standard thruster.",
                        "Effective thrust uses: baseThrust * fuel_thrust_percent / 100.")
                .defineInRange("baseThrust", 800.0d, 1.0d, 10000000.0d);
        OBSTRUCTION_SCAN_LENGTH = SERVER_BUILDER.comment("How many blocks behind the nozzle are checked for obstruction.")
                .defineInRange("obstructionScanLength", 10, 1, 64);
        OBSTRUCTION_IGNORE_OTHER_SUBLEVELS = SERVER_BUILDER.comment(
                        "Ignore non-sublevel blocks when checking for obstruction.")
                .define("obstructionIgnoreOtherSubLevels", false);
        REQUIRE_FUEL = SERVER_BUILDER.comment("If true, standard thrusters require configured fluid fuel to produce force.")
                .define("requireFuel", true);
        FUEL_TANK_CAPACITY_MB = SERVER_BUILDER.comment("Internal fuel tank capacity in millibuckets.")
                .defineInRange("fuelTankCapacityMb", 1000, 250, 10000000);
        FUEL_MB_PER_TICK_AT_FULL_THROTTLE = SERVER_BUILDER.comment("Fuel consumption in millibuckets per tick at full redstone throttle.")
                .defineInRange("fuelMbPerTickAtFullThrottle", 1.0d, 0.0001d, 1000.0d);
        DAMAGE_ENTITIES = SERVER_BUILDER.comment("If true, entities inside active thruster plume are damaged.")
                .define("damageEntities", true);

        CLIENT_PARTICLES_PER_TICK = SERVER_BUILDER.comment("Max client particles per tick while active.")
                .defineInRange("clientParticlesPerTick", 4, 0, 64);

        SERVER_BUILDER.pop(); // thruster

        SERVER_BUILDER.push("ionThruster");
        ION_THRUSTER_ENERGY_CAPACITY_FE = SERVER_BUILDER.comment("Ion thruster internal FE capacity.")
                .defineInRange("ionThrusterEnergyCapacityFe", 4000, 1, 100000000);
        ION_THRUSTER_FE_PER_TICK_AT_FULL_THROTTLE = SERVER_BUILDER.comment("Ion thruster energy consumption in FE per tick at full redstone throttle.")
                .defineInRange("ionThrusterFePerTickAtFullThrottle", 80.0d, 0.0001d, 1000000.0d);
        ION_THRUSTER_BASE_THRUST = SERVER_BUILDER.comment("Ion thruster base thrust at redstone 15 and full obstruction efficiency.")
                .defineInRange("ionThrusterBaseThrust", 1200.d, 1.d, 10000000.d);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Creative Thruster");
            CREATIVE_THRUSTER_BASE_THRUST = SERVER_BUILDER.comment("Starting thrust value (kN) when a creative thruster is placed.")
                .defineInRange("creativeThrusterBaseThrust", 1000.0d, 1.0d, 1000000.0d);
            CREATIVE_THRUSTER_MAX_THRUST = SERVER_BUILDER.comment("Maximum thrust (kN) the scroll can reach on a creative thruster.")
                .defineInRange("creativeThrusterMaxThrust", 10000.0d, 10.0d, 1000000.0d);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("vectorThruster");
            VECTOR_THRUSTER_BASE_THRUST = SERVER_BUILDER.comment("Vector thruster base thrust at redstone 15 and full obstruction efficiency.")
                .defineInRange("vectorThrusterBaseThrust", 1100.0d, 1.0d, 10000000.0d);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("multiblockThruster");
            MULTIBLOCK_2X_THRUST_MULTIPLIER = SERVER_BUILDER.comment("Thrust multiplier for a 2x2x2 multiblock thruster (e.g. 1.10 = 10% bonus).")
                .defineInRange("multiblock2xThrustMultiplier", 1.25d, 0.01d, 10.0d);
            MULTIBLOCK_3X_THRUST_MULTIPLIER = SERVER_BUILDER.comment("Thrust multiplier for a 3x3x3 multiblock thruster (e.g. 1.25 = 25% bonus).")
                .defineInRange("multiblock3xThrustMultiplier", 1.5d, 0.01d, 10.0d);
            MULTIBLOCK_2X_FUEL_EFFICIENCY = SERVER_BUILDER.comment("Fuel cost multiplier for a 2x2x2 multiblock thruster (e.g. 1.0 = no reduction, 0.8 = 20% cheaper).")
                .defineInRange("multiblock2xFuelEfficiency", 0.8d, 0.01d, 10.0d);
            MULTIBLOCK_3X_FUEL_EFFICIENCY = SERVER_BUILDER.comment("Fuel cost multiplier for a 3x3x3 multiblock thruster (e.g. 0.95 = 5% cheaper).")
                .defineInRange("multiblock3xFuelEfficiency", 0.6d, 0.01d, 10.0d);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("creativeVectorThruster");
            CREATIVE_VECTOR_THRUSTER_BASE_THRUST = SERVER_BUILDER.comment("Starting thrust value (kN) when a creative vector thruster is placed.")
                .defineInRange("creativeVectorThrusterBaseThrust", 1000.0d, 1.0d, 1000000.0d);
            CREATIVE_VECTOR_THRUSTER_MAX_THRUST = SERVER_BUILDER.comment("Maximum thrust (kN) the scroll can reach on a creative vector thruster.")
                .defineInRange("creativeVectorThrusterMaxThrust", 10000.0d, 10.0d, 1000000.0d);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("physics");
            DAMAGE_TICK_INTERVAL = SERVER_BUILDER.comment("How often plume damage checks run, in ticks.")
                .defineInRange("damageTickInterval", 5, 1, 40);
            NOZZLE_OFFSET_FROM_CENTER = SERVER_BUILDER.comment("Offset from the block center where force is applied.")
                .defineInRange("nozzleOffsetFromCenter", 0.45d, 0.0d, 1.5d);
            USE_ATMOSPHERIC_PRESSURE = SERVER_BUILDER.comment("If true, atmospheric pressure affects thruster output at altitude.")
                .define("useAtmosphericPressure", false);
            ATMOSPHERIC_PRESSURE_AMOUNT = SERVER_BUILDER.comment("Strength of atmospheric pressure influence. 1.0 = full effect, 0.0 = no effect.")
                .defineInRange("atmosphericPressureAmount", 1.0d, 0.0d, 2.0d);
            GROUND_FRICTION_COEFFICIENT = SERVER_BUILDER.comment("Ground friction coefficient applied while a thruster detects support under it.")
                .defineInRange("groundFrictionCoefficient", 0.08d, 0.0d, 5.0d);
            GROUND_LINEAR_DRAG = SERVER_BUILDER.comment("Grounded linear drag coefficient in pN per m/s.")
                .defineInRange("groundLinearDrag", 180.0d, 0.0d, 10000.0d);
            GROUND_ROLLING_RESISTANCE = SERVER_BUILDER.comment("Additional grounded rolling resistance in pN.")
                .defineInRange("groundRollingResistance", 80.0d, 0.0d, 10_000.0d);
            GROUNDED_SPEED_DEADZONE = SERVER_BUILDER.comment("Horizontal speed below this value is treated as stopped for grounded drag.")
                .defineInRange("groundedSpeedDeadzone", 0.03d, 0.0d, 5.0d);
            GROUND_PROBE_DISTANCE = SERVER_BUILDER.comment("How far downward a thruster probes to detect grounded support.")
                .defineInRange("groundProbeDistance", 1.5d, 0.05d, 5.0d);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Optical sensors");
            OPTICAL_SENSOR_TICKS_PER_UPDATE = SERVER_BUILDER.comment("How many ticks between casting a ray.")
                .defineInRange("Optical sensor tick rate", 2, 1, 100);
            INLINE_OPTICAL_SENSOR_MAX_DISTANCE = SERVER_BUILDER.comment("Length of the raycast ray.")
                .defineInRange("Inline optical sensor max raycast distance", 16, 4, 32);
            OPTICAL_SENSOR_MAX_DISTANCE = SERVER_BUILDER.comment("Length of the raycast ray. Very high values may degrade performance.")
                .defineInRange("Optical sensor max raycast distance", 32, 8, 64);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Wing");
            BASE_WING_LIFT = SERVER_BUILDER.comment("Wing's lift force is multiplied by this number.")
                .define("Base lift", 150.0);
            BASE_WING_DRAG = SERVER_BUILDER.comment("Wing's drag force is multiplied by this number.")
                .define("Base drag", 150.0);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Stirling Engine");
            STIRLING_GENERATED_SU = SERVER_BUILDER.comment("Change this value to modify the amount of stress units produced by stirling engine. Value of 16 corresponds to 4096 SU.")
                .defineInRange("Generated stress units", 16.0, 1.0, 64.0);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Tilt Adapter");
            TILT_ADAPTER_MAX_ANGLE = SERVER_BUILDER.comment("Maximum angle the tilt adapter can rotate to, in degrees.")
                .defineInRange("tiltAdapterMaxAngle", 90.0d, 0.0d, 180.0d);
            TILT_ADAPTER_ANGLE_RANGE = SERVER_BUILDER.comment("Maximum absolute output angle in degrees, reached at full redstone differential.")
                .defineInRange("Maximum angle range", 90.0, 0.0, 180.0);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Burners");
            BURNERS_POWER_HEATED_MIXERS = SERVER_BUILDER.comment("If true - both solid and liquid burners can provide heat to heated mixers allowing for pre-nether brass.")
                .define("Burners power heated mixers", true);
            BURNERS_HEAT_STEAM_ENGINES = SERVER_BUILDER.comment("Allow propulsion burners to heat Create steam engines.")
                .define("Burners heat steam engines", true);
            BURNERS_SUPERHEAT_STEAM_ENGINES = SERVER_BUILDER.comment("Allow seething burners to count as superheated for steam engines.")
                .define("Burners superheat steam engines", true);
            BLAZE_BURNERS_HEAT_STIRLING_ENGINES = SERVER_BUILDER.comment("Allow vanilla blaze burners under stirling engines to provide heat.")
                .define("Blaze burners heat stirling engines", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Cable");
            CABLE_ENERGY_TRANSFER = SERVER_BUILDER.comment("Maximum FE moved per tick by a single cable block.")
                .defineInRange("Energy transfer", 1_000, 1, 100000000);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Fuel Configuration");
            FUEL_PROPERTIES = SERVER_BUILDER.comment(
                    "Fuel table entries as '<namespace:fluid>=<efficiency_percent>,<burn_rate_percent>'.",
                    "Example: createpropulsion:turpentine=80,120")
                .defineListAllowEmpty("fuelProperties", PropulsionConfig::defaultFuelProperties, () -> "",
                    value -> value instanceof String);
            FUEL_EFFICIENCY_RATES = SERVER_BUILDER.comment(
                    "Fuel efficiency entries as '<namespace:fluid>=<efficiency_percent>'.",
                    "Example: createdieselgenerators:gasoline=125")
                .defineListAllowEmpty("fuelEfficiencyRates", PropulsionConfig::defaultFuelEfficiencyRates, () -> "",
                    value -> value instanceof String);
            FUEL_BURN_RATE_RATES = SERVER_BUILDER.comment(
                    "Fuel burn rate entries as '<namespace:fluid>=<burn_rate_percent>'.",
                    "Example: createdieselgenerators:gasoline=80")
                .defineListAllowEmpty("fuelBurnRateRates", PropulsionConfig::defaultFuelBurnRateRates, () -> "",
                    value -> value instanceof String);
            CORAL_FUEL_CONVERSION_RATES = SERVER_BUILDER.comment(
                    "Coral conversion entries as '<namespace:fluid>=<fe_per_mb>'.",
                    "Example: createpropulsion:coral=16")
                .defineListAllowEmpty("coralFuelConversionRates", PropulsionConfig::defaultCoralFuelConversionRates, () -> "",
                    value -> value instanceof String);

        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("thrusterDyeColors");
        SERVER_BUILDER.comment("Particle color overrides when a dye is applied to a thruster. Values are RRGGBB hex strings.");
        for (String[] e : new String[][] {
            { "white",       "FFFFFF" },
            { "orange",      "FF8000" },
            { "magenta",     "FF00FF" },
            { "light_blue",  "00BFFF" },
            { "yellow",      "FFFF00" },
            { "lime",        "7FFF00" },
            { "pink",        "FF69B4" },
            { "gray",        "808080" },
            { "light_gray",  "C0C0C0" },
            { "cyan",        "00FFFF" },
            { "purple",      "BF00FF" },
            { "blue",        "5555FF" },
            { "brown",       "C86400" },
            { "green",       "00C800" },
            { "red",         "FF0000" },
            { "black",       "2A2A2A" },
        }) {
            THRUSTER_DYE_COLORS.put("minecraft:" + e[0] + "_dye",
                SERVER_BUILDER.define(e[0], e[1]));
        }
        SERVER_BUILDER.pop();

        PropulsionDefaultStress.INSTANCE.registerAll(SERVER_BUILDER);

        SERVER_SPEC = SERVER_BUILDER.build();
        //#endregion

        //#region Client
        CLIENT_BUILDER.push("Stirling Engine");
            STIRLING_REVOLUTION_PERIOD = CLIENT_BUILDER.comment("Revolution period of the simulated shaft (affects only piston movement).")
                .define("Revolution period", 0.2);
            STIRLING_CRANK_RADIUS = CLIENT_BUILDER.comment("Radius of the simulated crank.")
                .define("Crank radius", 0.125);
            STIRLING_CONROD_LENGTH = CLIENT_BUILDER.comment("Length of the simulated conrod.")
                .define("Conrod length", 0.5);
        CLIENT_BUILDER.pop();
        CLIENT_BUILDER.push("Debug");
            DEBUG_THRUSTER = CLIENT_BUILDER.comment("Render thruster debug overlays (plume ray, obstruction hits, damage zones).")
                .define("Thruster", false);
            DEBUG_BURNER = CLIENT_BUILDER.comment("Render burner debug overlays.")
                .define("Burner", false);
            DEBUG_MAGNET = CLIENT_BUILDER.comment("Enable magnet debug overlays.")
                .define("Magnet", false);
        CLIENT_BUILDER.pop();


        CLIENT_SPEC = CLIENT_BUILDER.build();
        //#endregion
    }

    private static List<String> defaultFuelProperties() {
        return List.of(
                "createpropulsion:turpentine=80,120",
                "minecraft:lava=75,100",
                "createdieselgenerators:plant_oil=55,170",
                "immersiveengineering:plantoil=55,170",
                "createdieselgenerators:ethanol=70,140",
                "immersiveengineering:ethanol=70,140",
                "mekanismgenerators:bioethanol=75,135",
                "northstar:biofuel=80,125",
                "createdieselgenerators:biodiesel=90,110",
                "immersiveengineering:biodiesel=90,110",
                "immersiveengineering:high_power_biodiesel=105,95",
                "createdieselgenerators:diesel=100,100",
                "tfmg:diesel=100,100",
                "stellaris:diesel=100,100",
                "tfmg:naphtha=95,105",
                "tfmg:kerosene=115,90",
                "createdieselgenerators:gasoline=125,80",
                "tfmg:gasoline=125,80",
                "tfmg:lpg=120,85",
                "northstar:hydrocarbon=130,75",
                "stellaris:fuel=115,100",
                "mekanism:hydrogen=120,80",
                "createaddition:bioethanol=75,135",
                "createaddition:seed_oil=55,170",
                "northstar:methane=105,95",
                "northstar:liquid_hydrogen=125,80",
                "immersivepetroleum:diesel_sulfur=100,100"
        );
    }

    private static List<String> defaultCoralFuelConversionRates() {
        return List.of("createpropulsion:coral=16");
    }

    private static List<String> defaultFuelEfficiencyRates() {
        return List.of(
                "createpropulsion:turpentine=80",
                "minecraft:lava=75",
                "createdieselgenerators:plant_oil=55",
                "immersiveengineering:plantoil=55",
                "createdieselgenerators:ethanol=70",
                "immersiveengineering:ethanol=70",
                "mekanismgenerators:bioethanol=75",
                "northstar:biofuel=80",
                "createdieselgenerators:biodiesel=90",
                "immersiveengineering:biodiesel=90",
                "immersiveengineering:high_power_biodiesel=105",
                "createdieselgenerators:diesel=100",
                "tfmg:diesel=100",
                "stellaris:diesel=100",
                "tfmg:naphtha=95",
                "tfmg:kerosene=115",
                "createdieselgenerators:gasoline=125",
                "tfmg:gasoline=125",
                "tfmg:lpg=120",
                "northstar:hydrocarbon=130",
                "stellaris:fuel=115",
                "mekanism:hydrogen=120",
                "createaddition:bioethanol=75",
                "createaddition:seed_oil=55",
                "northstar:methane=105",
                "northstar:liquid_hydrogen=125",
                "immersivepetroleum:diesel_sulfur=100"
        );
    }

    private static List<String> defaultFuelBurnRateRates() {
        return List.of(
                "createpropulsion:turpentine=120",
                "minecraft:lava=100",
                "createdieselgenerators:plant_oil=170",
                "immersiveengineering:plantoil=170",
                "createdieselgenerators:ethanol=140",
                "immersiveengineering:ethanol=140",
                "mekanismgenerators:bioethanol=135",
                "northstar:biofuel=125",
                "createdieselgenerators:biodiesel=110",
                "immersiveengineering:biodiesel=110",
                "immersiveengineering:high_power_biodiesel=95",
                "createdieselgenerators:diesel=100",
                "tfmg:diesel=100",
                "stellaris:diesel=100",
                "tfmg:naphtha=105",
                "tfmg:kerosene=90",
                "createdieselgenerators:gasoline=80",
                "tfmg:gasoline=80",
                "tfmg:lpg=85",
                "northstar:hydrocarbon=75",
                "stellaris:fuel=100",
                "mekanism:hydrogen=80",
                "createaddition:bioethanol=135",
                "createaddition:seed_oil=170",
                "northstar:methane=95",
                "northstar:liquid_hydrogen=80",
                "immersivepetroleum:diesel_sulfur=100"
        );
    }

    public static Integer getDyeColor(String dyeId) {
        ModConfigSpec.ConfigValue<String> cv = THRUSTER_DYE_COLORS.get(dyeId);
        if (cv == null) return null;
        try {
            return Integer.parseUnsignedInt(cv.get().trim(), 16);
        } catch (NumberFormatException | IllegalStateException ignored) {
            return null;
        }
    }

    public static boolean isDyeConfigured(String itemId) {
        return THRUSTER_DYE_COLORS.containsKey(itemId);
    }

    public static List<? extends String> getCoralFuelConversionRatesOrDefault() {
        try {
            return CORAL_FUEL_CONVERSION_RATES.get();
        } catch (IllegalStateException ignored) {
            return defaultCoralFuelConversionRates();
        }
    }

    public static List<? extends String> getFuelBurnRateRatesOrDefault() {
        try {
            return FUEL_BURN_RATE_RATES.get();
        } catch (IllegalStateException ignored) {
            return defaultFuelBurnRateRates();
        }
    }

    public static List<? extends String> getFuelEfficiencyRatesOrDefault() {
        try {
            return FUEL_EFFICIENCY_RATES.get();
        } catch (IllegalStateException ignored) {
            return defaultFuelEfficiencyRates();
        }
    }
}
