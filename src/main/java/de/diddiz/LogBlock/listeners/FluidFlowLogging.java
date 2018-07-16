package de.diddiz.LogBlock.listeners;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;
import static de.diddiz.util.BukkitUtils.getNonFluidProofBlocks;

public class FluidFlowLogging extends LoggingListener {
    public FluidFlowLogging(LogBlock lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getBlock().getWorld());
        if (wcfg != null) {
            final Block to = event.getToBlock();
            final Material typeFrom = event.getBlock().getType();
            final Material typeTo = to.getType();
            final boolean canFlow = typeTo == Material.AIR || getNonFluidProofBlocks().contains(typeTo);
            if (typeFrom == Material.LAVA) {
                if (canFlow && wcfg.isLogging(Logging.LAVAFLOW)) {
                    if (isSurroundedByWater(to) && event.getBlock().getData() <= 2) {
                        consumer.queueBlockReplace(new Actor("LavaFlow"), to.getState(), Material.COBBLESTONE, (byte) 0);
                    } else if (typeTo == Material.AIR) {
                        consumer.queueBlockPlace(new Actor("LavaFlow"), to.getLocation(), Material.LAVA, (byte) (event.getBlock().getData() + 1));
                    } else {
                        consumer.queueBlockReplace(new Actor("LavaFlow"), to.getState(), Material.LAVA, (byte) (event.getBlock().getData() + 1));
                    }
                } else if (typeTo == Material.WATER) {
                    if (event.getFace() == BlockFace.DOWN) {
                        consumer.queueBlockReplace(new Actor("LavaFlow"), to.getState(), Material.STONE, (byte) 0);
                    } else {
                        consumer.queueBlockReplace(new Actor("LavaFlow"), to.getState(), Material.COBBLESTONE, (byte) 0);
                    }
                }
            } else if (typeFrom == Material.WATER && wcfg.isLogging(Logging.WATERFLOW)) {
                if (typeTo == Material.AIR) {
                    consumer.queueBlockPlace(new Actor("WaterFlow"), to.getLocation(), Material.WATER, (byte) (event.getBlock().getData() + 1));
                } else if (getNonFluidProofBlocks().contains(typeTo)) {
                    consumer.queueBlockReplace(new Actor("WaterFlow"), to.getState(), Material.WATER, (byte) (event.getBlock().getData() + 1));
                } else if (typeTo == Material.LAVA) {
                    if (to.getData() == 0) {
                        consumer.queueBlockReplace(new Actor("WaterFlow"), to.getState(), Material.OBSIDIAN, (byte) 0);
                    } else if (event.getFace() == BlockFace.DOWN) {
                        consumer.queueBlockReplace(new Actor("LavaFlow"), to.getState(), Material.STONE, (byte) 0);
                    }
                }
                if (typeTo == Material.AIR || getNonFluidProofBlocks().contains(typeTo)) {
                    for (final BlockFace face : new BlockFace[]{BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH}) {
                        final Block lower = to.getRelative(face);
                        if (lower.getType() == Material.LAVA) {
                            consumer.queueBlockReplace(new Actor("WaterFlow"), lower.getState(), lower.getData() == 0 ? Material.OBSIDIAN : Material.COBBLESTONE, (byte) 0);
                        }
                    }
                }
            }
        }
    }

    private static boolean isSurroundedByWater(Block block) {
        for (final BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH}) {
            if (block.getRelative(face).getType() == Material.WATER) {
                return true;
            }
        }
        return false;
    }
}
