package de.diddiz.util;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.List;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;
import static de.diddiz.LogBlock.config.Config.mb4;

public class LoggingUtil {

    public static void smartLogFallables(Consumer consumer, Actor actor, Block origin) {

        WorldConfig wcfg = getWorldConfig(origin.getWorld());
        if (wcfg == null) {
            return;
        }

        //Handle falling blocks
        Block checkBlock = origin.getRelative(BlockFace.UP);
        int up = 0;
        final int highestBlock = checkBlock.getWorld().getHighestBlockYAt(checkBlock.getLocation());
        while (checkBlock.getType().hasGravity()) {

            // Record this block as falling
            consumer.queueBlockBreak(actor, checkBlock.getState());

            // Guess where the block is going (This could be thrown of by explosions, but it is better than nothing)
            Location loc = origin.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            while (y > 0 && BukkitUtils.canFall(loc.getWorld(), x, (y - 1), z)) {
                y--;
            }
            // If y is 0 then the sand block fell out of the world :(
            if (y != 0) {
                Location finalLoc = new Location(loc.getWorld(), x, y, z);
                // Run this check to avoid false positives
                if (!BukkitUtils.getFallingEntityKillers().contains(finalLoc.getBlock().getType())) {
                    finalLoc.add(0, up, 0); // Add this here after checking for block breakers
                    if (finalLoc.getBlock().getType() == Material.AIR || finalLoc.getBlock().getType().hasGravity()) {
                        consumer.queueBlockPlace(actor, finalLoc, checkBlock.getType(), checkBlock.getData());
                    } else {
                        consumer.queueBlockReplace(actor, finalLoc, finalLoc.getBlock().getType(), finalLoc.getBlock().getData(), checkBlock.getType(), checkBlock.getData());
                    }
                    up++;
                }
            }
            if (checkBlock.getY() >= highestBlock) {
                break;
            }
            checkBlock = checkBlock.getRelative(BlockFace.UP);
        }
    }

    public static void smartLogBlockBreak(Consumer consumer, Actor actor, Block origin) {

        WorldConfig wcfg = getWorldConfig(origin.getWorld());
        if (wcfg == null) {
            return;
        }

        Block checkBlock = origin.getRelative(BlockFace.UP);
        if (BukkitUtils.getRelativeTopBreakabls().contains(checkBlock.getType())) {
            if (wcfg.isLogging(Logging.SIGNTEXT) && checkBlock.getType() == Material.SIGN) {
                consumer.queueSignBreak(actor, (Sign) checkBlock.getState());
            } else if (Tag.DOORS.isTagged(checkBlock.getType()) || BukkitUtils.getDoublePlants().contains(checkBlock.getType())) {
                Block otherBlock = checkBlock;
                // If the block or plant block is the top half of it then the player simply punched it
                // this will be handled later.
                if (!BukkitUtils.isTop(otherBlock.getBlockData())) {
                    otherBlock = otherBlock.getRelative(BlockFace.UP);
                    // Fall back check just in case the top half wasn't a door
                    if (otherBlock.getType() == checkBlock.getType()) {
                        consumer.queueBlockBreak(actor, otherBlock.getState());
                    }
                    consumer.queueBlockBreak(actor, checkBlock.getState());
                }
            } else {
                consumer.queueBlockBreak(actor, checkBlock.getState());
            }
        }

        List<Location> relativeBreakables = BukkitUtils.getBlocksNearby(origin, BukkitUtils.getRelativeBreakables());
        if (relativeBreakables.size() != 0) {
            for (Location location : relativeBreakables) {
                final Block block = location.getBlock();
                final Material blockType = block.getType();
                final BlockState blockState = block.getState();
                final BlockData data = blockState.getBlockData();

                if (data instanceof Directional && block.getRelative(((Directional) data).getFacing()).equals(origin)) {
                    if (blockType == Material.WALL_SIGN && wcfg.isLogging(Logging.SIGNTEXT)) {
                        consumer.queueSignBreak(actor, (Sign) blockState);
                    } else {
                        consumer.queueBlockBreak(actor, blockState);
                    }
                } else {
                    consumer.queueBlockBreak(actor, blockState);
                }
            }
        }

        // Special door check
        if (Tag.DOORS.isTagged(origin.getType())) {
            Block doorBlock = origin;

            // Up or down?
            if (!BukkitUtils.isTop(doorBlock.getBlockData())) {
                doorBlock = doorBlock.getRelative(BlockFace.UP);
            } else {
                doorBlock = doorBlock.getRelative(BlockFace.DOWN);
            }

            if (Tag.DOORS.isTagged(doorBlock.getType())) {
                consumer.queueBlockBreak(actor, doorBlock.getState());
            }
        } else if (BukkitUtils.getDoublePlants().contains(origin.getType())) { // Special double plant check
            Block plantBlock = origin;

            // Up or down?
            if (!BukkitUtils.isTop(plantBlock.getBlockData())) {
                plantBlock = plantBlock.getRelative(BlockFace.UP);
            } else {
                plantBlock = plantBlock.getRelative(BlockFace.DOWN);
            }

            if (plantBlock.getType() == origin.getType()) {
                consumer.queueBlockBreak(actor, plantBlock.getState());
            }
        }

        // Do this down here so that the block is added after blocks sitting on it
        consumer.queueBlockBreak(actor, origin.getState());
    }

    public static String checkText(String text) {
        if (text == null) {
            return text;
        }
        if (mb4) {
            return text;
        }
        return text.replaceAll("[^\\u0000-\\uFFFF]", "?");
    }
}
