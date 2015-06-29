package de.diddiz.LogBlock.listeners;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;

public class PseudoBlockBreakLogging extends LoggingListener {
    public PseudoBlockBreakLogging(LogBlock lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorStandBreak(EntityDeathEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getEntity().getWorld());
        if(wcfg != null && wcfg.isLogging(Logging.BLOCKPLACE)) {
            if(event.getEntity().getType() == EntityType.ARMOR_STAND) {
                Actor actor = new Actor("Unknown");
                EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
                if(lastDamage instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
                    if(damager instanceof Projectile) {
                        Actor.actorFromProjectileSource(((Projectile) damager).getShooter());
                    } else {
                        actor = Actor.actorFromEntity(damager);
                    }
                } else if(lastDamage instanceof EntityDamageByBlockEvent) {
                    Block damager = ((EntityDamageByBlockEvent) lastDamage).getDamager();
                    actor = new Actor("Block:" + damager.getType().toString());
                }
                consumer.queueBlockPlace(actor, event.getEntity().getLocation(), 416, (byte) 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getEntity().getWorld());
        if(wcfg != null && wcfg.isLogging(Logging.BLOCKBREAK)) {
            int id = 0;
            switch(event.getEntity().getType()) {
                case PAINTING:
                    id = Material.PAINTING.getId();
                    break;
                case ITEM_FRAME:
                    id = Material.ITEM_FRAME.getId();
                    break;
                case LEASH_HITCH:
                    id = Material.LEASH.getId();
                    break;
            }
            if(id != 0) {
                Actor actor = Actor.actorFromEntity(event.getRemover());
                if(event.getRemover() instanceof Projectile) {
                    actor = Actor.actorFromProjectileSource(((Projectile) event.getRemover()).getShooter());
                }
                consumer.queueBlockPlace(actor, event.getEntity().getLocation(), id, (byte) event.getEntity().getFacing().ordinal());
            }
        }
    }
}
