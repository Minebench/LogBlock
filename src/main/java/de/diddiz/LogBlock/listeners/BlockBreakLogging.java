package de.diddiz.LogBlock.listeners;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;
import de.diddiz.util.BukkitUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;
import static de.diddiz.LogBlock.config.Config.isLogging;
import static de.diddiz.util.LoggingUtil.smartLogBlockBreak;
import static de.diddiz.util.LoggingUtil.smartLogFallables;

public class BlockBreakLogging extends LoggingListener {
    public BlockBreakLogging(LogBlock lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isLogging(event.getBlock().getWorld(), Logging.BLOCKBREAK)) {
            WorldConfig wcfg = getWorldConfig(event.getBlock().getWorld());
            if (wcfg == null) {
                return;
            }

            final Actor actor = Actor.actorFromEntity(event.getPlayer());
            final Block origin = event.getBlock();
            final int typeId = origin.getTypeId();
            final Material type = origin.getType();

            if (wcfg.isLogging(Logging.SIGNTEXT) && (typeId == 63 || typeId == 68)) {
                consumer.queueSignBreak(actor, (Sign) origin.getState());
            } else if (wcfg.isLogging(Logging.CHESTACCESS) && BukkitUtils.getContainerBlocks().contains(type)) {
                consumer.queueContainerBreak(actor, origin.getState());
            } else if (type == Material.ICE) {
                // When in creative mode ice doesn't form water
                if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                    consumer.queueBlockBreak(actor, origin.getState());
                } else if (event.getPlayer().getInventory().getItemInMainHand() == null
                        || !event.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
                    consumer.queueBlockReplace(actor, origin.getState(), Material.WATER, (byte) 0);
                }
            } else {
                smartLogBlockBreak(consumer, actor, origin);
            }
            smartLogFallables(consumer, actor, origin);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (isLogging(event.getBlockClicked().getWorld(), Logging.BLOCKBREAK)) {
            consumer.queueBlockBreak(Actor.actorFromEntity(event.getPlayer()), event.getBlockClicked().getState());
        }
    }
}
