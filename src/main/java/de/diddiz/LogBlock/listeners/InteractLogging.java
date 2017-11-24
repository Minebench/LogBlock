package de.diddiz.LogBlock.listeners;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;
import de.diddiz.util.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;

public class InteractLogging extends LoggingListener {
    public InteractLogging(LogBlock lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getPlayer().getWorld());
        if (wcfg != null) {
            final Block clicked = event.getClickedBlock();
            if (clicked == null) {
                return;
            }
            final Material type = clicked.getType();
            final byte blockData = clicked.getData();
            final Player player = event.getPlayer();
            final Location loc = clicked.getLocation();

            switch (type) {
                case LEVER:
                case WOOD_BUTTON:
                case STONE_BUTTON:
                    if (wcfg.isLogging(Logging.SWITCHINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case FENCE_GATE:
                case WOODEN_DOOR:
                case TRAP_DOOR:
                    if (wcfg.isLogging(Logging.DOORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case CAKE_BLOCK:
                    if (wcfg.isLogging(Logging.CAKEEAT) && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getFoodLevel() < 20) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case NOTE_BLOCK:
                    if (wcfg.isLogging(Logging.NOTEBLOCKINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    if (wcfg.isLogging(Logging.DIODEINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case REDSTONE_COMPARATOR_OFF:
                case REDSTONE_COMPARATOR_ON:
                    if (wcfg.isLogging(Logging.COMPARATORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case WOOD_PLATE:
                case STONE_PLATE:
                case IRON_PLATE:
                case GOLD_PLATE:
                    if (wcfg.isLogging(Logging.PRESUREPLATEINTERACT) && event.getAction() == Action.PHYSICAL) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case TRIPWIRE:
                    if (wcfg.isLogging(Logging.TRIPWIREINTERACT) && event.getAction() == Action.PHYSICAL) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, type, blockData);
                    }
                    break;
                case SOIL:
                    if (wcfg.isLogging(Logging.CROPTRAMPLE) && event.getAction() == Action.PHYSICAL) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, type, Material.DIRT, blockData);
                        // Log the crop on top as being broken
                        Block trampledCrop = clicked.getRelative(BlockFace.UP);
                        if (BukkitUtils.getCropBlocks().contains(trampledCrop.getType())) {
                            consumer.queueBlockBreak(Actor.actorFromEntity(player), trampledCrop.getState());
                        }
                    }
                    break;
            }
        }
    }
}
