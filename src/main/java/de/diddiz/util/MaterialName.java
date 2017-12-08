package de.diddiz.util;

import de.diddiz.LogBlock.LogBlock;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static de.diddiz.util.Utils.isInt;
import static de.diddiz.util.Utils.isShort;
import static org.bukkit.Bukkit.getLogger;

public class MaterialName {
    private static final String[] COLORS = {"white", "orange", "magenta", "light blue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final Map<Material, String> materialNames = new HashMap<>();
    private static final Map<Material, Map<Short, String>> materialDataNames = new HashMap<>();
    private static final Map<String, Integer> nameTypes = new HashMap<String, Integer>();

    static {
        // Add all known materials
        for (final Material mat : Material.values()) {
            materialNames.put(mat, toReadable(mat.toString()));
        }
        // Load config
        final File file = new File(LogBlock.getInstance().getDataFolder(), "materials.yml");
        final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        if (cfg.getKeys(false).isEmpty()) {
            // Generate defaults
            cfg.options().header("Add block or item names you want to be overridden or also names for custom blocks");
            cfg.set("stone.1", "granite");
            cfg.set("stone.2", "polished granite");
            cfg.set("stone.3", "diorite");
            cfg.set("stone.4", "polished diorite");
            cfg.set("stone.5", "andesite");
            cfg.set("stone.6", "polished andesite");
            cfg.set("wood.0", "oak wood");
            cfg.set("wood.1", "spruce wood");
            cfg.set("wood.2", "birch wood");
            cfg.set("wood.3", "jungle wood");
            cfg.set("wood.4", "acacia wood");
            cfg.set("wood.5", "dark oak wood");
            cfg.set("dirt.1", "coarse dirt");
            cfg.set("dirt.2", "podzol");
            cfg.set("sapling.1", "redwood sapling");
            cfg.set("sapling.2", "birch sapling");
            cfg.set("sapling.3", "jungle sapling");
            cfg.set("sapling.4", "acacia sapling");
            cfg.set("sapling.5", "dark oak sapling");
            cfg.set("stationary_water", "water");
            cfg.set("stationary_lava", "lava");
            cfg.set("sand.1", "red sand");
            cfg.set("log.0", "oak log");
            cfg.set("log.1", "spruce log");
            cfg.set("log.2", "birch log");
            cfg.set("log.3", "jungle log");
            cfg.set("log.4", "oak log");
            cfg.set("log.5", "spruce log");
            cfg.set("log.6", "birch log");
            cfg.set("log.7", "jungle log");
            cfg.set("log.8", "oak log");
            cfg.set("log.9", "spruce log");
            cfg.set("log.10", "birch log");
            cfg.set("log.11", "jungle log");
            cfg.set("log.12", "oak log");
            cfg.set("log.13", "spruce log");
            cfg.set("log.14", "birch log");
            cfg.set("log.15", "jungle log");
            cfg.set("leaves.1", "spruce leaves");
            cfg.set("leaves.2", "birch leaves");
            cfg.set("leaves.3", "jungle leaves");
            cfg.set("leaves.4", "oak leaves");
            cfg.set("leaves.5", "spruce leaves");
            cfg.set("leaves.6", "birch leaves");
            cfg.set("leaves.7", "jungle leaves");
            cfg.set("leaves.8", "oak leaves");
            cfg.set("leaves.9", "spruce leaves");
            cfg.set("leaves.10", "birch leaves");
            cfg.set("leaves.11", "jungle leaves");
            cfg.set("leaves.12", "oak leaves");
            cfg.set("leaves.13", "spruce leaves");
            cfg.set("leaves.14", "birch leaves");
            cfg.set("leaves.15", "jungle leaves");
            cfg.set("sponge.1", "wet sponge");
            cfg.set("yellow_flower.0", "dandelion");
            cfg.set("red_rose.0", "poppy");
            cfg.set("red_rose.1", "blue orchid");
            cfg.set("red_rose.2", "allium");
            cfg.set("red_rose.3", "azure bluet");
            cfg.set("red_rose.4", "red tulip");
            cfg.set("red_rose.5", "orange tulip");
            cfg.set("red_rose.6", "white tulip");
            cfg.set("red_rose.7", "pink tulip");
            cfg.set("red_rose.8", "oxeye daisy");
            cfg.set("sandstone.1", "chiseled sandstone");
            cfg.set("sandstone.2", "smooth sandstone");
            cfg.set("long_grass.0", "dead bush");
            cfg.set("long_grass.1", "tall grass");
            cfg.set("long_grass.2", "fern");
            cfg.set("smooth_brick.0", "stone brick");
            cfg.set("smooth_brick.1", "mossy stone brick");
            cfg.set("smooth_brick.2", "cracked stone brick");
            cfg.set("smooth_brick.3", "chiseled stone brick");
            cfg.set("wood_double_step.0", "oak double step");
            cfg.set("wood_double_step.1", "spruce double step");
            cfg.set("wood_double_step.2", "birch double step");
            cfg.set("wood_double_step.3", "jungle double step");
            cfg.set("wood_double_step.4", "acacia double step");
            cfg.set("wood_double_step.5", "dark oak double step");
            cfg.set("wood_step.0", "oak step");
            cfg.set("wood_step.1", "spruce step");
            cfg.set("wood_step.2", "birch step");
            cfg.set("wood_step.3", "jungle step");
            cfg.set("wood_step.4", "acacia step");
            cfg.set("wood_step.5", "dark oak step");
            cfg.set("wood_step.8", "oak step");
            cfg.set("wood_step.9", "spruce step");
            cfg.set("wood_step.10", "birch step");
            cfg.set("wood_step.11", "jungle step");
            cfg.set("wood_step.12", "acacia step");
            cfg.set("wood_step.13", "dark oak step");
            cfg.set("cobble_wall.1", "mossy cobble wall");
            cfg.set("quartz_block.1", "chiseled quartz block");
            cfg.set("quartz_block.2", "pillar quartz block");
            cfg.set("quartz_block.3", "pillar quartz block");
            cfg.set("quartz_block.4", "pillar quartz block");
            cfg.set("leaves_2.0", "acacia leaves");
            cfg.set("leaves_2.1", "dark oak leaves");
            cfg.set("leaves_2.4", "acacia leaves");
            cfg.set("leaves_2.5", "dark oak leaves");
            cfg.set("leaves_2.8", "acacia leaves");
            cfg.set("leaves_2.9", "dark oak leaves");
            cfg.set("leaves_2.12", "acacia leaves");
            cfg.set("leaves_2.13", "dark oak leaves");
            cfg.set("log_2.0", "acacia log");
            cfg.set("log_2.1", "dark oak log");
            cfg.set("log_2.4", "acacia log");
            cfg.set("log_2.5", "dark oak log");
            cfg.set("log_2.8", "acacia log");
            cfg.set("log_2.9", "dark oak log");
            cfg.set("log_2.12", "acacia log");
            cfg.set("log_2.13", "dark oak log");
            cfg.set("prismarine.1", "prismarine brick");
            cfg.set("prismarine.2", "dark prismarine");
            cfg.set("double_stone_slab2.0", "red sandstone double step");
            cfg.set("double_stone_slab2.8", "smooth red sandstone double step");
            cfg.set("double_plant.0", "sunflower");
            cfg.set("double_plant.1", "lilac");
            cfg.set("double_plant.2", "double tall grass");
            cfg.set("double_plant.3", "large fern");
            cfg.set("double_plant.4", "rose bush");
            cfg.set("double_plant.5", "peony");
            cfg.set("double_plant.8", "sunflower");
            cfg.set("double_plant.9", "lilac");
            cfg.set("double_plant.10", "double tall grass");
            cfg.set("double_plant.11", "large fern");
            cfg.set("double_plant.12", "rose bush");
            cfg.set("double_plant.13", "peony");
            cfg.set("coal.1", "charcoal");
            for (byte i = 0; i < 10; i++) {
                cfg.set("double_step." + i, toReadable(Material.DOUBLE_STEP.getNewData(i)));
            }
            cfg.set("double_step.8", "stone double step");
            cfg.set("double_step.9", "sandstone double step");
            cfg.set("double_step.15", "quartz double step");
            for (byte i = 0; i < 8; i++) {
                cfg.set("step." + i, toReadable(Material.STEP.getNewData(i)));
                // The second half of this data list should read the same as the first half
                cfg.set("step." + (i + 7), toReadable(Material.STEP.getNewData(i)));
            }
            for (byte i = 0; i < 16; i++) {
                cfg.set("ink_sack." + i, toReadable(Material.INK_SACK.getNewData(i)));
                cfg.set("wool." + i, COLORS[i] + " wool");
                cfg.set("stained_clay." + i, COLORS[i] + " stained terracotta");
                cfg.set("stained_glass." + i, COLORS[i] + " stained glass");
                cfg.set("stained_glass_pane." + i, COLORS[i] + " stained glass pane");
                cfg.set("carpet." + i, COLORS[i] + " carpet");
                cfg.set("concrete." + i, COLORS[i] + " concrete");
                cfg.set("concrete_powder." + i, COLORS[i] + " concrete powder");
            }
            for (byte i = 0; i < 6; i++) {
                cfg.set("wood_double_step." + i, toReadable(Material.WOOD_DOUBLE_STEP.getNewData(i)));
                cfg.set("wood_step." + i, toReadable(Material.WOOD_STEP.getNewData(i)));
                cfg.set("wood_step." + i + 8, toReadable(Material.WOOD_STEP.getNewData(i)));
            }
            try {
                cfg.save(file);
            } catch (final IOException ex) {
                getLogger().log(Level.WARNING, "Unable to save material.yml: ", ex);
            }
        }
        if (cfg.getString("concrete_powder.1") == null) {
            getLogger().info("[Logblock-names] Logblock's default materials.yml file has been updated with more names");
            getLogger().info("[Logblock-names] Consider deleting your current materials.yml file to allow it to be recreated");
        }
        for (final String entry : cfg.getKeys(false)) {
            try {
                Material mat = Material.valueOf(entry.toUpperCase());
                if (cfg.isString(entry)) {
                    materialNames.put(mat, cfg.getString(entry));
                    nameTypes.put(cfg.getString(entry), Integer.valueOf(entry));
                } else if (cfg.isConfigurationSection(entry)) {
                    final Map<Short, String> dataNames = new HashMap<Short, String>();
                    materialDataNames.put(mat, dataNames);
                    final ConfigurationSection sec = cfg.getConfigurationSection(entry);
                    for (final String data : sec.getKeys(false)) {
                        if (isShort(data)) {
                            if (sec.isString(data)) {
                                dataNames.put(Short.valueOf(data), sec.getString(data));
                                nameTypes.put(sec.getString(data), Integer.valueOf(entry));
                            } else {
                                getLogger().warning("Parsing materials.yml: '" + data + "' is not a string.");
                            }
                        } else {
                            getLogger().warning("Parsing materials.yml: '" + data + "' is no valid material data");
                        }
                    }
                } else {
                    getLogger().warning("Parsing materials.yml: '" + entry + "' is neither a string nor a section.");
                }
            } catch (IllegalArgumentException e){
                getLogger().warning("Parsing materials.yml: '" + entry + "' is no valid material id");
            }
        }
    }

    /**
     * Returns the name of a material based on its id
     *
     * @param type The type of the material
     * @return Name of the material, or if it's unknown, the id.
     * @deprecated Use {@link #materialName(Material)}
     */
    @Deprecated
    public static String materialName(int type) {
        Material mat = Material.getMaterial(type);
        return mat == null ? null : materialNames.containsKey(mat) ? materialNames.get(mat) : toReadable(mat.toString());
    }
    
    /**
     * Returns the name of a material
     *
     * @param type The material
     * @return Name of the material, or if it's unknown, the id.
     */
    public static String materialName(Material type) {
        return materialNames.containsKey(type) ? materialNames.get(type) : toReadable(type.toString());
    }

    /**
     * Returns the name of a material based on its id and data
     *
     * @param type The type of the material
     * @param data The data of the material
     * @return Name of the material regarding it's data, or if it's unknown, the basic name.
     * @deprecated Use {@link #materialName(Material, short)}
     */
    @Deprecated
    public static String materialName(int type, short data) {
        final Map<Short, String> dataNames = materialDataNames.get(type);
        if (dataNames != null) {
            if (dataNames.containsKey(data)) {
                return dataNames.get(data);
            }
        }
        return materialName(type);
    }
    
    /**
     * Returns the name of a material and data
     *
     * @param type The material
     * @param data The data
     * @return Name of the material regarding it's data, or if it's unknown, the basic name.
     */
    public static String materialName(Material type, short data) {
        final Map<Short, String> dataNames = materialDataNames.get(type.getId());
        if (dataNames != null) {
            if (dataNames.containsKey(data)) {
                return dataNames.get(data);
            }
        }
        return materialName(type);
    }

    public static Material typeFromName(String name) {
        Integer answer = nameTypes.get(toReadable(name));
        if (answer != null) {
            return Material.getMaterial(answer);
        }
        final Material mat = Material.matchMaterial(name);
        if (mat == null) {
            throw new IllegalArgumentException("No material matching: '" + name + "'");
        }
        return mat;
    }

    private static String toReadable(MaterialData matData) {
        return matData.toString().toLowerCase().replace('_', ' ').replaceAll("[^a-z ]", "");
    }

    private static String toReadable(String matData) {
        return matData.toLowerCase().replace('_', ' ').replaceAll("[^a-z ]", "");
    }
}
