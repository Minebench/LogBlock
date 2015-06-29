package de.diddiz.LogBlock.listeners;

import com.sun.deploy.util.ArrayUtil;
import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

import java.util.ArrayList;
import java.util.List;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;

public class PseudoBlockPlaceLogging extends LoggingListener {
    public PseudoBlockPlaceLogging(LogBlock lb) {
        super(lb);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorStandPlace(CreatureSpawnEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getEntity().getWorld());
        if(wcfg != null && wcfg.isLogging(Logging.BLOCKPLACE)) {
            if(event.getEntity().getType() == EntityType.ARMOR_STAND) {
                if(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT) {
                    Actor actor = new Actor("Unknown");
                    List<Entity> nearbyPlayers = new ArrayList<Entity>();
                    for(Entity e : event.getEntity().getNearbyEntities(5, 5, 5)) {
                        if(e instanceof Player) {
                            nearbyPlayers.add(e);
                        }
                    }
                    if(nearbyPlayers.size() == 1) {
                        actor = Actor.actorFromEntity(nearbyPlayers.get(0));
                    } else if(nearbyPlayers.size() > 1) {
                        actor = new Actor("One of " + ArrayUtil.arrayToString(nearbyPlayers.toArray(new String[0])));
                    }
                    consumer.queueBlockPlace(actor, event.getLocation(), 416, (byte) 0);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onHangingPlace(HangingPlaceEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getEntity().getWorld());
        if(wcfg != null && wcfg.isLogging(Logging.BLOCKPLACE)) {
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
                consumer.queueBlockPlace(Actor.actorFromEntity(event.getPlayer()), event.getEntity().getLocation(), id, (byte) event.getEntity().getFacing().ordinal());
            }
        }
    }
}
