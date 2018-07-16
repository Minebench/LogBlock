package de.diddiz.LogBlock;

import de.diddiz.LogBlock.config.Config;
import de.diddiz.util.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.Button;
import org.bukkit.material.Cake;
import org.bukkit.material.Comparator;
import org.bukkit.material.Diode;
import org.bukkit.material.Door;
import org.bukkit.material.Gate;
import org.bukkit.material.Lever;
import org.bukkit.material.PressurePlate;
import org.bukkit.material.TrapDoor;
import org.bukkit.material.Tripwire;

import java.sql.ResultSet;
import java.sql.SQLException;

import static de.diddiz.util.LoggingUtil.checkText;
import static de.diddiz.util.MaterialName.materialName;

public class BlockChange implements LookupCacheElement {
    public final long id, date;
    public final Location loc;
    public final Actor actor;
    public final String playerName;
    public final BlockState type, replaced;
    public final ChestAccess ca;

    public BlockChange(long date, Location loc, Actor actor, BlockState type, BlockState replaced, ChestAccess ca) {
        id = 0;
        this.date = date;
        this.loc = loc;
        this.actor = actor;
        this.ca = ca;
        this.playerName = actor == null ? null : actor.getName();
        this.type = type;
        this.replaced = replaced;
    }

    public BlockChange(ResultSet rs, QueryParams p) throws SQLException {
        id = p.needId ? rs.getInt("id") : 0;
        date = p.needDate ? rs.getTimestamp("date").getTime() : 0;
        loc = p.needCoords ? new Location(p.world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z")) : null;
        actor = p.needPlayer ? new Actor(rs) : null;
        playerName = p.needPlayer ? rs.getString("playername") : null;
        replaced = p.needType ? Material.getMaterial(rs.getInt("replaced")) : Material.AIR;
        type = p.needType ? Material.getMaterial(rs.getInt("type")) : Material.AIR;
        data = p.needData ? rs.getByte("data") : (byte) 0;
        signtext = p.needSignText ? rs.getString("signtext") : null;
        ca = p.needChestAccess && rs.getShort("itemtype") != 0 && rs.getShort("itemamount") != 0 ? new ChestAccess(Material.getMaterial(rs.getShort("itemtype")), rs.getShort("itemamount"), rs.getShort("itemdata")) : null;
    }

    @Override
    public String toString() {
        final StringBuilder msg = new StringBuilder();
        if (date > 0) {
            msg.append(Config.formatter.format(date)).append(" ");
        }
        if (actor != null) {
            msg.append(actor.getName()).append(" ");
        }
        if (signtext != null) {
            final String action = type == Material.AIR ? "destroyed " : "created ";
            if (!signtext.contains("\0")) {
                msg.append(action).append(signtext);
            } else {
                msg.append(action).append(materialName(type != Material.AIR ? type : replaced)).append(" [").append(signtext.replace("\0", "] [")).append("]");
            }
        } else if (type == replaced) {
            if (type == Material.AIR) {
                msg.append("did an unspecified action");
            } else if (ca != null) {
                if (ca.itemType == Material.AIR || ca.itemAmount == 0) {
                    msg.append("looked inside ").append(materialName(type));
                } else if (ca.itemAmount < 0) {
                    msg.append("took ").append(-ca.itemAmount).append("x ").append(materialName(ca.itemType, ca.itemData)).append(" from ").append(materialName(type));
                } else {
                    msg.append("put ").append(ca.itemAmount).append("x ").append(materialName(ca.itemType, ca.itemData)).append(" into ").append(materialName(type));
                }
            } else if (BukkitUtils.getContainerBlocks().contains(type)) {
                msg.append("opened ").append(materialName(type));
            } else if (BukkitUtils.equalTypes(Material.WOODEN_DOOR, type))
            // This is a problem that will have to be addressed in LB 2,
            // there is no way to tell from the top half of the block if
            // the door is opened or closed.
            {
                msg.append("moved ").append(materialName(type));
            }
            // Trapdoor
            else if (BukkitUtils.equalTypes(Material.TRAP_DOOR, type)) {
                msg.append((data < 8 || data > 11) ? "opened" : "closed").append(" ").append(materialName(type));
            }
            // Fence gate
            else if (BukkitUtils.equalTypes(Material.FENCE_GATE, type)) {
                msg.append(data > 3 ? "opened" : "closed").append(" ").append(materialName(type));
            } else if (BukkitUtils.equalTypes(Material.LEVER, type)) {
                msg.append("switched ").append(materialName(type));
            } else if (BukkitUtils.equalTypes(Material.STONE_BUTTON, type)) {
                msg.append("pressed ").append(materialName(type));
            } else if (BukkitUtils.equalTypes(Material.CAKE_BLOCK, type)) {
                msg.append("ate a piece of ").append(materialName(type));
            } else if (type == Material.NOTE_BLOCK || BukkitUtils.equalTypes(Material.DIODE_BLOCK_ON, type) || BukkitUtils.equalTypes(Material.REDSTONE_COMPARATOR_ON, type)) {
                msg.append("changed ").append(materialName(type));
            } else if (BukkitUtils.equalTypes(Material.STONE_PLATE, type)) {
                msg.append("stepped on ").append(materialName(type));
            } else if (type == Material.TRIPWIRE) {
                msg.append("ran into ").append(materialName(type));
            }
        } else if (type == Material.AIR) {
            msg.append("destroyed ").append(materialName(replaced, data));
        } else if (replaced == Material.AIR) {
            msg.append("created ").append(materialName(type, data));
        } else {
            msg.append("replaced ").append(materialName(replaced, (byte) 0)).append(" with ").append(materialName(type, data));
        }
        if (loc != null) {
            msg.append(" at ").append(loc.getBlockX()).append(":").append(loc.getBlockY()).append(":").append(loc.getBlockZ());
        }
        return msg.toString();
    }

    @Override
    public Location getLocation() {
        return loc;
    }

    @Override
    public String getMessage() {
        return toString();
    }
}
