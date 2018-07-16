package de.diddiz.util;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

import static de.diddiz.util.MaterialName.materialName;

/**
 * TODO: Integrate all this stuff into a material mapping enum so that we don't have to convert the DB with 1.13?
 * TODO: Check if all the sets actually need to contain the legacy materials
  */
public class BukkitUtils {
    private static final Map<Material, Set<Material>> blockEquivalents;

    private static final Set<Material> doublePlants;
    private static final Set<Material> relativeBreakable;
    private static final Set<Material> relativeTopBreakable;
    private static final Set<Material> fallingEntityKillers;
    private static final Set<Material> killedByFallingEntities;
    private static final Set<Material> nonFluidProofBlocks;
    
    private static final Set<Material> cropBlocks;
    private static final Set<Material> containerBlocks;

    private static final Map<EntityType, Material> projectileItems;

    private static final Map<Integer, Material> materialIdMap;

    static {
        materialIdMap = new HashMap<>();
        for (Material material : Material.values()) {
            materialIdMap.put(material.getId(), material);
        }

        blockEquivalents = new EnumMap<>(Material.class);
        addEquivalents(EnumSet.of(
                Material.DIRT,
                Material.COARSE_DIRT,
                Material.GRASS_BLOCK,
                Material.GRASS_PATH,
                Material.FARMLAND,
                Material.MYCELIUM,
                Material.PODZOL,
                Material.LEGACY_GRASS,
                Material.LEGACY_DIRT,
                Material.LEGACY_MYCEL,
                Material.LEGACY_SOIL
        ));
        addEquivalents(EnumSet.of(
                Material.WATER,
                Material.ICE,
                Material.LEGACY_WATER,
                Material.LEGACY_STATIONARY_WATER,
                Material.LEGACY_ICE
        ));
        addEquivalents(EnumSet.of(
                Material.LAVA,
                Material.LEGACY_LAVA,
                Material.LEGACY_STATIONARY_LAVA
        ));
        addEquivalents(EnumSet.of(
                Material.FURNACE,
                Material.LEGACY_FURNACE,
                Material.LEGACY_BURNING_FURNACE
        ));
        addEquivalents(EnumSet.of(
                Material.REDSTONE_ORE,
                Material.LEGACY_REDSTONE_ORE,
                Material.LEGACY_GLOWING_REDSTONE_ORE
        ));
        addEquivalents(EnumSet.of(
                Material.LEGACY_REDSTONE_TORCH_OFF,
                Material.LEGACY_REDSTONE_TORCH_ON
        ));
        addEquivalents(EnumSet.of(
                Material.REPEATER,
                Material.LEGACY_DIODE_BLOCK_OFF,
                Material.LEGACY_DIODE_BLOCK_ON
        ));
        addEquivalents(EnumSet.of(
                Material.COMPARATOR,
                Material.LEGACY_REDSTONE_COMPARATOR_OFF,
                Material.LEGACY_REDSTONE_COMPARATOR_ON
        ));
        addEquivalents(EnumSet.of(
                Material.COMMAND_BLOCK,
                Material.CHAIN_COMMAND_BLOCK,
                Material.REPEATING_COMMAND_BLOCK,
                Material.LEGACY_COMMAND,
                Material.LEGACY_COMMAND_CHAIN,
                Material.LEGACY_COMMAND_REPEATING
        ));
        addEquivalents(EnumSet.of(
                Material.ACACIA_TRAPDOOR,
                Material.BIRCH_TRAPDOOR,
                Material.SPRUCE_TRAPDOOR,
                Material.DARK_OAK_TRAPDOOR,
                Material.OAK_TRAPDOOR,
                Material.JUNGLE_TRAPDOOR,
                Material.IRON_TRAPDOOR,
                Material.LEGACY_TRAP_DOOR,
                Material.LEGACY_IRON_TRAPDOOR
        ));
        addEquivalents(EnumSet.of(
                Material.ACACIA_FENCE_GATE,
                Material.BIRCH_FENCE_GATE,
                Material.SPRUCE_FENCE_GATE,
                Material.DARK_OAK_FENCE_GATE,
                Material.OAK_FENCE_GATE,
                Material.JUNGLE_FENCE_GATE,
                Material.LEGACY_FENCE_GATE
        ));

        EnumSet<Material> pressurePlates = EnumSet.of(
                Material.STONE_PRESSURE_PLATE,
                Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Material.LEGACY_STONE_PLATE,
                Material.LEGACY_WOOD_PLATE,
                Material.LEGACY_IRON_PLATE,
                Material.LEGACY_GOLD_PLATE
        );
        pressurePlates.addAll(Tag.WOODEN_PRESSURE_PLATES.getValues());
        addEquivalents(pressurePlates);

        addEquivalents(EnumSet.of(
                Material.CHEST,
                Material.TRAPPED_CHEST,
                Material.LEGACY_CHEST,
                Material.LEGACY_TRAPPED_CHEST
        ));
        addEquivalents(EnumSet.of(
                Material.SIGN,
                Material.WALL_SIGN,
                Material.LEGACY_SIGN_POST,
                Material.LEGACY_WALL_SIGN
        ));
        addEquivalents(EnumSet.of(
                Material.BLACK_SHULKER_BOX,
                Material.BLUE_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX,
                Material.BROWN_SHULKER_BOX,
                Material.CYAN_SHULKER_BOX,
                Material.GRAY_SHULKER_BOX,
                Material.GREEN_SHULKER_BOX,
                Material.LIGHT_BLUE_SHULKER_BOX,
                Material.MAGENTA_SHULKER_BOX,
                Material.LIME_SHULKER_BOX,
                Material.ORANGE_SHULKER_BOX,
                Material.PINK_SHULKER_BOX,
                Material.PURPLE_SHULKER_BOX,
                Material.RED_SHULKER_BOX,
                Material.WHITE_SHULKER_BOX,
                Material.YELLOW_SHULKER_BOX,
                Material.LEGACY_WHITE_SHULKER_BOX,
                Material.LEGACY_ORANGE_SHULKER_BOX,
                Material.LEGACY_MAGENTA_SHULKER_BOX,
                Material.LEGACY_LIGHT_BLUE_SHULKER_BOX,
                Material.LEGACY_YELLOW_SHULKER_BOX,
                Material.LEGACY_LIME_SHULKER_BOX,
                Material.LEGACY_PINK_SHULKER_BOX,
                Material.LEGACY_GRAY_SHULKER_BOX,
                Material.LEGACY_SILVER_SHULKER_BOX,
                Material.LEGACY_CYAN_SHULKER_BOX,
                Material.LEGACY_PURPLE_SHULKER_BOX,
                Material.LEGACY_BLUE_SHULKER_BOX,
                Material.LEGACY_BROWN_SHULKER_BOX,
                Material.LEGACY_GREEN_SHULKER_BOX,
                Material.LEGACY_RED_SHULKER_BOX,
                Material.LEGACY_BLACK_SHULKER_BOX
        ));
        addEquivalents(EnumSet.of(
                Material.WHITE_GLAZED_TERRACOTTA,
                Material.ORANGE_GLAZED_TERRACOTTA,
                Material.MAGENTA_GLAZED_TERRACOTTA,
                Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
                Material.YELLOW_GLAZED_TERRACOTTA,
                Material.LIME_GLAZED_TERRACOTTA,
                Material.PINK_GLAZED_TERRACOTTA,
                Material.GRAY_GLAZED_TERRACOTTA,
                Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
                Material.CYAN_GLAZED_TERRACOTTA,
                Material.PURPLE_GLAZED_TERRACOTTA,
                Material.BLUE_GLAZED_TERRACOTTA,
                Material.BROWN_GLAZED_TERRACOTTA,
                Material.GREEN_GLAZED_TERRACOTTA,
                Material.RED_GLAZED_TERRACOTTA,
                Material.BLACK_GLAZED_TERRACOTTA,
                Material.LEGACY_WHITE_GLAZED_TERRACOTTA,
                Material.LEGACY_ORANGE_GLAZED_TERRACOTTA,
                Material.LEGACY_MAGENTA_GLAZED_TERRACOTTA,
                Material.LEGACY_LIGHT_BLUE_GLAZED_TERRACOTTA,
                Material.LEGACY_YELLOW_GLAZED_TERRACOTTA,
                Material.LEGACY_LIME_GLAZED_TERRACOTTA,
                Material.LEGACY_PINK_GLAZED_TERRACOTTA,
                Material.LEGACY_GRAY_GLAZED_TERRACOTTA,
                Material.LEGACY_SILVER_GLAZED_TERRACOTTA,
                Material.LEGACY_CYAN_GLAZED_TERRACOTTA,
                Material.LEGACY_PURPLE_GLAZED_TERRACOTTA,
                Material.LEGACY_BLUE_GLAZED_TERRACOTTA,
                Material.LEGACY_BROWN_GLAZED_TERRACOTTA,
                Material.LEGACY_GREEN_GLAZED_TERRACOTTA,
                Material.LEGACY_RED_GLAZED_TERRACOTTA,
                Material.LEGACY_BLACK_GLAZED_TERRACOTTA
        ));

        EnumSet<Material> banners = EnumSet.of(
                Material.LEGACY_BANNER,
                Material.LEGACY_WALL_BANNER,
                Material.LEGACY_STANDING_BANNER
        );
        banners.addAll(Tag.BANNERS.getValues());
        addEquivalents(banners);

        addEquivalents(EnumSet.copyOf(Tag.DOORS.getValues()));
        addEquivalents(EnumSet.of(
                Material.OAK_FENCE_GATE,
                Material.ACACIA_FENCE_GATE,
                Material.BIRCH_FENCE_GATE,
                Material.DARK_OAK_FENCE_GATE,
                Material.JUNGLE_FENCE_GATE,
                Material.SPRUCE_FENCE_GATE,
                Material.LEGACY_FENCE_GATE,
                Material.LEGACY_ACACIA_FENCE_GATE,
                Material.LEGACY_BIRCH_FENCE_GATE,
                Material.LEGACY_DARK_OAK_FENCE_GATE,
                Material.LEGACY_JUNGLE_FENCE_GATE,
                Material.LEGACY_SPRUCE_FENCE_GATE
        ));

        // Plants that consist of two blocks
        doublePlants = EnumSet.of(
                Material.SUNFLOWER,
                Material.LILAC,
                Material.TALL_GRASS,
                Material.LARGE_FERN,
                Material.ROSE_BUSH,
                Material.PEONY,
                Material.TALL_SEAGRASS
        );

        // Blocks that break when they are attached to a block
        relativeBreakable = EnumSet.of(
                // Legacy materials to be backwards compatible with old databases
                Material.LEGACY_WALL_SIGN,
                Material.LEGACY_WALL_BANNER,
                Material.LEGACY_LADDER,
                Material.LEGACY_STONE_BUTTON,
                Material.LEGACY_WOOD_BUTTON,
                Material.LEGACY_REDSTONE_TORCH_ON,
                Material.LEGACY_REDSTONE_TORCH_OFF,
                Material.LEGACY_LEVER,
                Material.LEGACY_TORCH,
                Material.LEGACY_TRIPWIRE_HOOK,
                Material.LEGACY_COCOA,

                Material.WALL_SIGN,
                Material.LADDER,
                Material.STONE_BUTTON,
                Material.REDSTONE_WALL_TORCH,
                Material.LEVER,
                Material.WALL_TORCH,
                Material.TRIPWIRE_HOOK,
                Material.COCOA,
                Material.BLACK_WALL_BANNER,
                Material.BLUE_WALL_BANNER,
                Material.LIGHT_GRAY_WALL_BANNER,
                Material.BROWN_WALL_BANNER,
                Material.CYAN_WALL_BANNER,
                Material.GRAY_WALL_BANNER,
                Material.GREEN_WALL_BANNER,
                Material.LIGHT_BLUE_WALL_BANNER,
                Material.MAGENTA_WALL_BANNER,
                Material.LIME_WALL_BANNER,
                Material.ORANGE_WALL_BANNER,
                Material.PINK_WALL_BANNER,
                Material.PURPLE_WALL_BANNER,
                Material.RED_WALL_BANNER,
                Material.WHITE_WALL_BANNER,
                Material.YELLOW_WALL_BANNER,
                Material.DEAD_FIRE_CORAL_BLOCK
        );
        relativeBreakable.addAll(Tag.BUTTONS.getValues());
        relativeBreakable.addAll(Tag.CORAL_FANS.getValues());

        // Blocks that break when they are on top of a block
        relativeTopBreakable = EnumSet.of(
                // Legacy materials to be backwards compatible with old databases
                Material.LEGACY_SAPLING,
                Material.LEGACY_LONG_GRASS,
                Material.LEGACY_DEAD_BUSH,
                Material.LEGACY_YELLOW_FLOWER,
                Material.LEGACY_RED_ROSE,
                Material.LEGACY_BROWN_MUSHROOM,
                Material.LEGACY_RED_MUSHROOM,
                Material.LEGACY_CROPS,
                Material.LEGACY_POTATO,
                Material.LEGACY_CARROT,
                Material.LEGACY_BEETROOT_BLOCK,
                Material.LEGACY_WATER_LILY,
                Material.LEGACY_CACTUS,
                Material.LEGACY_SUGAR_CANE_BLOCK,
                Material.LEGACY_FLOWER_POT,
                Material.LEGACY_POWERED_RAIL,
                Material.LEGACY_DETECTOR_RAIL,
                Material.LEGACY_ACTIVATOR_RAIL,
                Material.LEGACY_RAILS,
                Material.LEGACY_REDSTONE_WIRE,
                Material.LEGACY_SIGN_POST,
                Material.LEGACY_STONE_PLATE,
                Material.LEGACY_WOOD_PLATE,
                Material.LEGACY_IRON_PLATE,
                Material.LEGACY_GOLD_PLATE,
                Material.LEGACY_SNOW,
                Material.LEGACY_DIODE_BLOCK_ON,
                Material.LEGACY_DIODE_BLOCK_OFF,
                Material.LEGACY_REDSTONE_COMPARATOR_ON,
                Material.LEGACY_REDSTONE_COMPARATOR_OFF,
                Material.LEGACY_WOODEN_DOOR,
                Material.LEGACY_IRON_DOOR_BLOCK,
                Material.LEGACY_CARPET,
                Material.LEGACY_DOUBLE_PLANT,
                Material.LEGACY_CHORUS_PLANT,
                Material.LEGACY_CHORUS_FLOWER,
                Material.LEGACY_STANDING_BANNER,
                Material.LEGACY_CHEST,
                Material.LEGACY_TRAPPED_CHEST,
                Material.LEGACY_ENDER_CHEST,
                Material.LEGACY_FENCE_GATE,

                Material.GRASS,
                Material.DEAD_BUSH,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID,
                Material.ALLIUM,
                Material.AZURE_BLUET,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.WHITE_TULIP,
                Material.PINK_TULIP,
                Material.OXEYE_DAISY,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM,
                Material.WHEAT,
                Material.POTATOES,
                Material.CARROTS,
                Material.BEETROOTS,
                Material.LILY_PAD,
                Material.CACTUS,
                Material.SUGAR_CANE,
                Material.FLOWER_POT,
                Material.POWERED_RAIL,
                Material.DETECTOR_RAIL,
                Material.ACTIVATOR_RAIL,
                Material.REDSTONE_WIRE,
                Material.SIGN,
                Material.STONE_PRESSURE_PLATE,
                Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Material.SNOW,
                Material.REPEATER,
                Material.COMPARATOR,
                Material.CHORUS_PLANT,
                Material.CHORUS_FLOWER,
                Material.CHEST,
                Material.TRAPPED_CHEST,
                Material.ENDER_CHEST
        );
        relativeTopBreakable.addAll(doublePlants);
        relativeTopBreakable.addAll(Tag.RAILS.getValues());
        relativeTopBreakable.addAll(Tag.DOORS.getValues());
        relativeTopBreakable.addAll(Tag.CARPETS.getValues());
        relativeTopBreakable.addAll(Tag.WOODEN_PRESSURE_PLATES.getValues());
        relativeTopBreakable.addAll(Tag.SAPLINGS.getValues());
        relativeTopBreakable.addAll(Tag.FLOWER_POTS.getValues());
        relativeTopBreakable.addAll(Tag.ITEMS_BANNERS.getValues());
        relativeTopBreakable.addAll(Tag.CORAL_FANS.getValues());

        // Blocks that break falling entities
        fallingEntityKillers = EnumSet.of(
        // Legacy materials to be backwards compatible with old databases
                Material.LEGACY_SIGN_POST,
                Material.LEGACY_WALL_SIGN,
                Material.LEGACY_STONE_PLATE,
                Material.LEGACY_WOOD_PLATE,
                Material.LEGACY_IRON_PLATE,
                Material.LEGACY_GOLD_PLATE,
                Material.LEGACY_SAPLING,
                Material.LEGACY_YELLOW_FLOWER,
                Material.LEGACY_RED_ROSE,
                Material.LEGACY_CROPS,
                Material.LEGACY_CARROT,
                Material.LEGACY_POTATO,
                Material.LEGACY_BEETROOT,
                Material.LEGACY_NETHER_WARTS,
                Material.LEGACY_RED_MUSHROOM,
                Material.LEGACY_BROWN_MUSHROOM,
                Material.LEGACY_STEP,
                Material.LEGACY_WOOD_STEP,
                Material.LEGACY_TORCH,
                Material.LEGACY_FLOWER_POT,
                Material.LEGACY_POWERED_RAIL,
                Material.LEGACY_DETECTOR_RAIL,
                Material.LEGACY_ACTIVATOR_RAIL,
                Material.LEGACY_RAILS,
                Material.LEGACY_LEVER,
                Material.LEGACY_WOOD_BUTTON,
                Material.LEGACY_STONE_BUTTON,
                Material.LEGACY_REDSTONE_WIRE,
                Material.LEGACY_REDSTONE_TORCH_ON,
                Material.LEGACY_REDSTONE_TORCH_OFF,
                Material.LEGACY_DIODE_BLOCK_ON,
                Material.LEGACY_DIODE_BLOCK_OFF,
                Material.LEGACY_REDSTONE_COMPARATOR_ON,
                Material.LEGACY_REDSTONE_COMPARATOR_OFF,
                Material.LEGACY_CARPET,
                Material.LEGACY_DAYLIGHT_DETECTOR,
                Material.LEGACY_DAYLIGHT_DETECTOR_INVERTED,
                Material.LEGACY_CHORUS_PLANT,
                Material.LEGACY_STANDING_BANNER,
                Material.LEGACY_WALL_BANNER,
                Material.LEGACY_ENCHANTMENT_TABLE,

                Material.SIGN,
                Material.WALL_SIGN,
                Material.STONE_PRESSURE_PLATE,
                Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID,
                Material.ALLIUM,
                Material.AZURE_BLUET,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.WHITE_TULIP,
                Material.PINK_TULIP,
                Material.OXEYE_DAISY,
                Material.WHEAT,
                Material.CARROTS,
                Material.POTATOES,
                Material.BEETROOTS,
                Material.NETHER_WART,
                Material.RED_MUSHROOM,
                Material.BROWN_MUSHROOM,
                Material.TORCH,
                Material.WALL_TORCH,
                Material.LEVER,
                Material.REDSTONE_WIRE,
                Material.REDSTONE_TORCH,
                Material.REDSTONE_WALL_TORCH,
                Material.REPEATER,
                Material.COMPARATOR,
                Material.DAYLIGHT_DETECTOR,
                Material.TURTLE_EGG,
                Material.KELP_PLANT,
                Material.CHORUS_PLANT,
                Material.ENCHANTING_TABLE
        );
        fallingEntityKillers.addAll(getBlockEquivalents(Material.ACACIA_FENCE_GATE));
        fallingEntityKillers.addAll(Tag.WOODEN_PRESSURE_PLATES.getValues());
        fallingEntityKillers.addAll(Tag.CARPETS.getValues());
        fallingEntityKillers.addAll(Tag.CORAL_FANS.getValues());
        fallingEntityKillers.addAll(Tag.CORALS.getValues());
        fallingEntityKillers.addAll(Tag.SAPLINGS.getValues());
        fallingEntityKillers.addAll(Tag.SLABS.getValues());
        fallingEntityKillers.addAll(Tag.FLOWER_POTS.getValues());
        fallingEntityKillers.addAll(Tag.RAILS.getValues());
        fallingEntityKillers.addAll(Tag.BUTTONS.getValues());
        fallingEntityKillers.addAll(Tag.BANNERS.getValues());


        killedByFallingEntities = EnumSet.of(
                Material.TURTLE_EGG,
                Material.SEAGRASS,
                Material.VINE
        );
        killedByFallingEntities.addAll(doublePlants);

        // Blocks that liquid can flow through
        nonFluidProofBlocks = EnumSet.of(
                // Legacy materials to be backwards compatible with old databases
                Material.LEGACY_POWERED_RAIL,
                Material.LEGACY_DETECTOR_RAIL,
                Material.LEGACY_LONG_GRASS,
                Material.LEGACY_DEAD_BUSH,
                Material.LEGACY_YELLOW_FLOWER,
                Material.LEGACY_RED_ROSE,
                Material.LEGACY_BROWN_MUSHROOM,
                Material.LEGACY_RED_MUSHROOM,
                Material.LEGACY_TORCH,
                Material.LEGACY_FIRE,
                Material.LEGACY_REDSTONE_WIRE,
                Material.LEGACY_CROPS,
                Material.LEGACY_RAILS,
                Material.LEGACY_LEVER,
                Material.LEGACY_REDSTONE_TORCH_OFF,
                Material.LEGACY_REDSTONE_TORCH_ON,
                Material.LEGACY_SNOW,
                Material.LEGACY_SUGAR_CANE,
                Material.LEGACY_CAKE_BLOCK,
                Material.LEGACY_DIODE_BLOCK_OFF,
                Material.LEGACY_DIODE_BLOCK_ON,
                Material.LEGACY_PUMPKIN_STEM,
                Material.LEGACY_MELON_STEM,
                Material.LEGACY_VINE,
                Material.LEGACY_WATER_LILY,
                Material.LEGACY_NETHER_WARTS,
                Material.LEGACY_TRIPWIRE_HOOK,
                Material.LEGACY_TRIPWIRE,
                Material.LEGACY_FLOWER_POT,
                Material.LEGACY_CARROT,
                Material.LEGACY_POTATO,
                Material.LEGACY_WOOD_BUTTON,
                Material.LEGACY_SKULL,
                Material.LEGACY_REDSTONE_COMPARATOR_OFF,
                Material.LEGACY_REDSTONE_COMPARATOR_ON,
                Material.LEGACY_ACTIVATOR_RAIL,
                Material.LEGACY_CARPET,
                Material.LEGACY_DOUBLE_PLANT,
                Material.LEGACY_BEETROOT_BLOCK,

                Material.POWERED_RAIL,
                Material.DETECTOR_RAIL,
                Material.GRASS,
                Material.DEAD_BUSH,
                Material.DANDELION,
                Material.POPPY,
                Material.BLUE_ORCHID,
                Material.ALLIUM,
                Material.AZURE_BLUET,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.WHITE_TULIP,
                Material.PINK_TULIP,
                Material.OXEYE_DAISY,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM,
                Material.TORCH,
                Material.FIRE,
                Material.REDSTONE_WIRE,
                Material.WHEAT,
                Material.SUGAR_CANE,
                Material.LEVER,
                Material.REDSTONE_TORCH,
                Material.REDSTONE_WALL_TORCH,
                Material.SNOW,
                Material.CAKE,
                Material.REPEATER,
                Material.PUMPKIN_STEM,
                Material.MELON_STEM,
                Material.VINE,
                Material.LILY_PAD,
                Material.NETHER_WART,
                Material.TRIPWIRE_HOOK,
                Material.TRIPWIRE,
                Material.FLOWER_POT,
                Material.CARROT,
                Material.POTATO,
                Material.SKELETON_SKULL,
                Material.SKELETON_WALL_SKULL,
                Material.WITHER_SKELETON_SKULL,
                Material.WITHER_SKELETON_WALL_SKULL,
                Material.PLAYER_HEAD,
                Material.PLAYER_WALL_HEAD,
                Material.CREEPER_HEAD,
                Material.CREEPER_WALL_HEAD,
                Material.DRAGON_HEAD,
                Material.DRAGON_WALL_HEAD,
                Material.COMPARATOR,
                Material.ACTIVATOR_RAIL,
                Material.SUNFLOWER,
                Material.LILAC,
                Material.TALL_GRASS,
                Material.LARGE_FERN,
                Material.ROSE_BUSH,
                Material.PEONY,
                Material.BEETROOTS
        );

        nonFluidProofBlocks.addAll(Tag.SAPLINGS.getValues());
        nonFluidProofBlocks.addAll(Tag.FLOWER_POTS.getValues());
        nonFluidProofBlocks.addAll(Tag.RAILS.getValues());
        nonFluidProofBlocks.addAll(Tag.CARPETS.getValues());
        nonFluidProofBlocks.addAll(Tag.BUTTONS.getValues());
    
        // Crop Blocks (that can be trampled)
        cropBlocks = EnumSet.of(
                // Legacy materials to be backwards compatible with old databases
                Material.LEGACY_CROPS,
                Material.LEGACY_MELON_STEM,
                Material.LEGACY_PUMPKIN_STEM,
                Material.LEGACY_CARROT,
                Material.LEGACY_POTATO,
                Material.LEGACY_BEETROOT_BLOCK,
                Material.WHEAT,
                Material.MELON_STEM,
                Material.PUMPKIN_STEM,
                Material.CARROTS,
                Material.POTATOES,
                Material.BEETROOTS
        );

        // Container Blocks
        containerBlocks = EnumSet.of(
                // Legacy materials to be backwards compatible with old databases
                Material.LEGACY_CHEST,
                Material.LEGACY_TRAPPED_CHEST,
                Material.LEGACY_DISPENSER,
                Material.LEGACY_DROPPER,
                Material.LEGACY_HOPPER,
                Material.LEGACY_BREWING_STAND,
                Material.LEGACY_FURNACE,
                Material.LEGACY_BURNING_FURNACE,
                Material.LEGACY_BEACON,
                Material.CHEST,
                Material.TRAPPED_CHEST,
                Material.DISPENSER,
                Material.DROPPER,
                Material.HOPPER,
                Material.BREWING_STAND,
                Material.FURNACE,
                Material.BEACON
        );
        containerBlocks.addAll(getBlockEquivalents(Material.WHITE_SHULKER_BOX));

        // Doesn't actually have a block inventory
        // containerBlocks.add(Material.ENDER_CHEST);

        // It doesn't seem like you could injure people with some of these, but they exist, so....
        projectileItems = new EnumMap<>(EntityType.class);
        projectileItems.put(EntityType.ARROW, Material.ARROW);
        projectileItems.put(EntityType.EGG, Material.EGG);
        projectileItems.put(EntityType.ENDER_PEARL, Material.ENDER_PEARL);
        projectileItems.put(EntityType.SMALL_FIREBALL, Material.FIRE_CHARGE);    // Fire charge
        projectileItems.put(EntityType.FIREBALL, Material.FIRE_CHARGE);        // Fire charge
        projectileItems.put(EntityType.FISHING_HOOK, Material.FISHING_ROD);
        projectileItems.put(EntityType.SNOWBALL, Material.SNOWBALL);
        projectileItems.put(EntityType.SPLASH_POTION, Material.SPLASH_POTION);
        projectileItems.put(EntityType.LINGERING_POTION, Material.LINGERING_POTION);
        projectileItems.put(EntityType.THROWN_EXP_BOTTLE, Material.EXPERIENCE_BOTTLE);
        projectileItems.put(EntityType.WITHER_SKULL, Material.WITHER_SKELETON_SKULL);

    }

    private static void addEquivalents(EnumSet<Material> materials) {
        for (Material material : materials) {
            blockEquivalents.put(material, materials);
        }
    }
    
    private static final BlockFace[] relativeBlockFaces = new BlockFace[]{
            BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN
    };

    /**
     * Returns a list of block locations around the block that are of the type specified by the integer list parameter
     *
     * @param block The central block to get the blocks around
     * @param type The type of blocks around the center block to return
     * @return List of block locations around the block that are of the type specified by the integer list parameter
     */
    public static List<Location> getBlocksNearby(org.bukkit.block.Block block, Set<Material> type) {
        ArrayList<Location> blocks = new ArrayList<Location>();
        for (BlockFace blockFace : relativeBlockFaces) {
            if (type.contains(block.getRelative(blockFace).getType())) {
                blocks.add(block.getRelative(blockFace).getLocation());
            }
        }
        return blocks;
    }

    public static boolean isTop(BlockData blockData) {
        return blockData instanceof Bisected && ((Bisected) blockData).getHalf() == Bisected.Half.TOP;
    }

    public static Material getInventoryHolderType(InventoryHolder holder) {
        if (holder instanceof DoubleChest) {
            return getInventoryHolderType(((DoubleChest) holder).getLeftSide());
        } else if (holder instanceof BlockState) {
            return ((BlockState) holder).getType();
        } else {
            return null;
        }
    }

    public static Location getInventoryHolderLocation(InventoryHolder holder) {
        if (holder instanceof DoubleChest) {
            return getInventoryHolderLocation(((DoubleChest) holder).getLeftSide());
        } else if (holder instanceof BlockState) {
            return ((BlockState) holder).getLocation();
        } else {
            return null;
        }
    }

    public static ItemStack[] compareInventories(ItemStack[] items1, ItemStack[] items2) {
        final ItemStackComparator comperator = new ItemStackComparator();
        final ArrayList<ItemStack> diff = new ArrayList<ItemStack>();
        final int l1 = items1.length, l2 = items2.length;
        int c1 = 0, c2 = 0;
        while (c1 < l1 || c2 < l2) {
            if (c1 >= l1) {
                diff.add(items2[c2]);
                c2++;
                continue;
            }
            if (c2 >= l2) {
                items1[c1].setAmount(items1[c1].getAmount() * -1);
                diff.add(items1[c1]);
                c1++;
                continue;
            }
            final int comp = comperator.compare(items1[c1], items2[c2]);
            if (comp < 0) {
                items1[c1].setAmount(items1[c1].getAmount() * -1);
                diff.add(items1[c1]);
                c1++;
            } else if (comp > 0) {
                diff.add(items2[c2]);
                c2++;
            } else {
                final int amount = items2[c2].getAmount() - items1[c1].getAmount();
                if (amount != 0) {
                    items1[c1].setAmount(amount);
                    diff.add(items1[c1]);
                }
                c1++;
                c2++;
            }
        }
        return diff.toArray(new ItemStack[diff.size()]);
    }

    public static ItemStack[] compressInventory(ItemStack[] items) {
        final ArrayList<ItemStack> compressed = new ArrayList<ItemStack>();
        for (final ItemStack item : items) {
            if (item != null) {
                final Material type = item.getType();
                final short data = rawData(item);
                boolean found = false;
                for (final ItemStack item2 : compressed) {
                    if (type == item2.getType() && data == rawData(item2)) {
                        item2.setAmount(item2.getAmount() + item.getAmount());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    compressed.add(new ItemStack(type, item.getAmount(), data));
                }
            }
        }
        Collections.sort(compressed, new ItemStackComparator());
        return compressed.toArray(new ItemStack[compressed.size()]);
    }
    
    public static boolean equalTypes(Material type1, Material type2) {
        if (type1 == type2) {
            return true;
        }
        if (blockEquivalents.containsKey(type1)) {
            return blockEquivalents.get(type1).contains(type2);
        }
        return false;
    }

    public static String friendlyWorldname(String worldName) {
        return new File(worldName).getName();
    }

    public static Map<Material, Set<Material>> getBlockEquivalents() {
        return blockEquivalents;
    }
    
    public static Set<Material> getBlockEquivalents(Material type) {
        return blockEquivalents.get(type);
    }

    public static Set<Material> getRelativeBreakables() {
        return relativeBreakable;
    }

    public static Set<Material> getRelativeTopBreakabls() {
        return relativeTopBreakable;
    }

    /**
     * @deprecated Use {@link Material#hasGravity()}
     */
    @Deprecated
    public static Set<Material> getRelativeTopFallables() {
        Set<Material> relativeTopFallables = EnumSet.noneOf(Material.class);
        for (Material material : Material.values()) {
            if (material.hasGravity()) {
                relativeTopFallables.add(material);
            }
        }
        return relativeTopFallables;
    }

    public static Set<Material> getDoublePlants() {
        return doublePlants;
    }

    public static Set<Material> getKilledByFallingEntities() {
        return killedByFallingEntities;
    }

    public static Set<Material> getFallingEntityKillers() {
        return fallingEntityKillers;
    }
    
    public static Set<Material> getNonFluidProofBlocks() {
        return nonFluidProofBlocks;
    }

    public static Set<Material> getCropBlocks() {
        return cropBlocks;
    }

    public static Set<Material> getContainerBlocks() {
        return containerBlocks;
    }

    public static String entityName(Entity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).getName();
        }
        if (entity instanceof TNTPrimed) {
            return "TNT";
        }
        return entity.getClass().getSimpleName().substring(5);
    }

    public static void giveTool(Player player, Material type) {
        final Inventory inv = player.getInventory();
        if (inv.contains(type)) {
            player.sendMessage(ChatColor.RED + "You have already a " + materialName(type));
        } else {
            final int free = inv.firstEmpty();
            if (free >= 0) {
                if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                    inv.setItem(free, player.getInventory().getItemInMainHand());
                }
                player.getInventory().setItemInMainHand(new ItemStack(type, 1));
                player.sendMessage(ChatColor.GREEN + "Here's your " + materialName(type));
            } else {
                player.sendMessage(ChatColor.RED + "You have no empty slot in your inventory");
            }
        }
    }

    public static short rawData(ItemStack item) {
        return item.getType() != null ? item.getData() != null ? item.getDurability() : 0 : 0;
    }

    public static int saveSpawnHeight(Location loc) {
        final World world = loc.getWorld();
        final Chunk chunk = world.getChunkAt(loc);
        if (!world.isChunkLoaded(chunk)) {
            world.loadChunk(chunk);
        }
        final int x = loc.getBlockX();
        final int z = loc.getBlockZ();
        int y = loc.getBlockY();
        boolean lower = world.getBlockAt(x, y, z).getType() == Material.AIR;
        boolean upper = world.getBlockAt(x, y + 1, z).getType() == Material.AIR;
        while ((!lower || !upper) && y != world.getMaxHeight()) {
            lower = upper;
            upper = world.getBlockAt(x, ++y, z).getType() == Material.AIR;
        }
        while (world.getBlockAt(x, y - 1, z).getType() == Material.AIR && y != 0) {
            y--;
        }
        return y;
    }

    public static int modifyContainer(BlockState b, ItemStack item) {
        if (b instanceof InventoryHolder) {
            final Inventory inv = ((InventoryHolder) b).getInventory();
            if (item.getAmount() < 0) {
                item.setAmount(-item.getAmount());
                final ItemStack tmp = inv.removeItem(item).get(0);
                return tmp != null ? tmp.getAmount() : 0;
            } else if (item.getAmount() > 0) {
                final ItemStack tmp = inv.addItem(item).get(0);
                return tmp != null ? tmp.getAmount() : 0;
            }
        }
        return 0;
    }

    public static boolean canFall(World world, int x, int y, int z) {
        Material mat = world.getBlockAt(x, y, z).getType();

        // Air
        if (mat == Material.AIR) {
            return true;
        } else if (mat == Material.WATER || mat == Material.LAVA) { // Fluids
            return true;
        } else if (getFallingEntityKillers().contains(mat) || mat == Material.FIRE || mat == Material.VINE || mat == Material.KELP_PLANT) { // Misc.
            return true;
        }
        return false;
    }

    public static class ItemStackComparator implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack a, ItemStack b) {
            final Material aType = a.getType(), bType = b.getType();
            int compared = aType.compareTo(bType);
            if (compared < 0) {
                return -1;
            }
            if (compared > 0) {
                return 1;
            }
            final short aData = rawData(a), bData = rawData(b);
            if (aData < bData) {
                return -1;
            }
            if (aData > bData) {
                return 1;
            }
            return 0;
        }
    }

    @Deprecated
    public static int itemIDfromProjectileEntity(Entity e) {
        return itemTypeFromProjectileEntity(e).getId();
    }
    
    public static Material itemTypeFromProjectileEntity(Entity e) {
        Material type = projectileItems.get(e.getType());
        return type != null ? type : Material.AIR;
    }

    @Deprecated
    public static Material getMaterialById(int id) {
        return materialIdMap.get(id);
    }
}
