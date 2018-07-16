package de.diddiz.LogBlock;

import static de.diddiz.LogBlock.Actor.actorFromString;
import de.diddiz.LogBlock.config.Config;
import de.diddiz.LogBlock.events.BlockChangePreLogEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static de.diddiz.LogBlock.config.Config.*;
import static de.diddiz.util.Utils.mysqlTextEscape;
import static de.diddiz.util.BukkitUtils.*;
import static org.bukkit.Bukkit.getLogger;

public class Consumer extends TimerTask {
    private final Queue<Row> queue = new LinkedBlockingQueue<Row>();
    private final Set<Actor> failedPlayers = new HashSet<Actor>();
    private final LogBlock logblock;
    private final Map<Actor, Integer> playerIds = new HashMap<Actor, Integer>();
    private final Lock lock = new ReentrantLock();

    Consumer(LogBlock logblock) {
        this.logblock = logblock;
        try {
            Class.forName("PlayerLeaveRow");
        } catch (final ClassNotFoundException ex) {
        }
    }
    
    /**
     * Logs any block change. Don't try to combine broken and placed blocks. Queue two block changes or use the queueBLockReplace methods.
     *
     * @param actor Actor responsible for making the change
     * @param before State of the block before the change
     * @param after State of the block after the change
     */
    public void queueBlock(Actor actor, BlockState before, BlockState after) {
        queueBlock(actor, before, after, null);
    }

    /**
     * Logs any block change. Don't try to combine broken and placed blocks. Queue two block changes or use the queueBLockReplace methods.
     *
     * @param actor Actor responsible for making the change
     * @param loc Location of the block change
     * @param typeBefore Type of the block before the change
     * @param typeAfter Type of the block after the change
     * @param data Data of the block after the change
     * @deprecated Use {@link #queueBlock(Actor, BlockState, BlockState)}
     */
    @Deprecated
    public void queueBlock(Actor actor, Location loc, Material typeBefore, Material typeAfter, byte data) {
        queueBlock(actor, loc, typeBefore, typeAfter, data, null, null);
    }

    /**
     * Logs a block break. The type afterwards is assumed to be 0 (air).
     *
     * @param actor Actor responsible for breaking the block
     * @param before Blockstate of the block before actually being destroyed.
     */
    public void queueBlockBreak(Actor actor, BlockState before) {
        queueBlockBreak(actor, new Location(before.getWorld(), before.getX(), before.getY(), before.getZ()), before.getType(), before.getRawData());
    }

    /**
     * Logs a block break. The block type afterwards is assumed to be 0 (air).
     *
     * @param actor Actor responsible for the block break
     * @param loc Location of the broken block
     * @param typeBefore Type of the block before the break
     * @param dataBefore Data of the block before the break
     * @deprecated Use {@link #queueBlockBreak(Actor, BlockState)}
     */
    @Deprecated
    public void queueBlockBreak(Actor actor, Location loc, Material typeBefore, byte dataBefore) {
        queueBlock(actor, loc, typeBefore, Material.AIR, dataBefore);
    }

    /**
     * Logs a block place. The block type before is assumed to be 0 (air).
     *
     * @param actor Actor responsible for placing the block
     * @param after Blockstate of the block after actually being placed.
     */
    public void queueBlockPlace(Actor actor, BlockState after) {
        queueBlockPlace(actor, new Location(after.getWorld(), after.getX(), after.getY(), after.getZ()), after.getBlock().getType(), after.getBlock().getData());
    }

    /**
     * Logs a block place. The block type before is assumed to be 0 (air).
     *
     * @param actor Actor responsible for placing the block
     * @param loc Location of the placed block
     * @param type Type of the placed block
     * @param data Data of the placed block
     * @deprecated Use {@link #queueBlockPlace(Actor, BlockState)}
     */
    @Deprecated
    public void queueBlockPlace(Actor actor, Location loc, Material type, byte data) {
        queueBlock(actor, loc, Material.AIR, type, data);
    }

    /**
     * Logs a block being replaced from the before and after {@link org.bukkit.block.BlockState}s
     *
     * @param actor Actor responsible for replacing the block
     * @param before Blockstate of the block before actually being destroyed.
     * @param after  Blockstate of the block after actually being placed.
     */
    public void queueBlockReplace(Actor actor, BlockState before, BlockState after) {
        queueBlockReplace(actor, new Location(before.getWorld(), before.getX(), before.getY(), before.getZ()), before.getType(), before.getRawData(), after.getType(), after.getRawData());
    }

    /**
     * Logs a block being replaced from the before {@link org.bukkit.block.BlockState} and the type and data after
     *
     * @param actor  Actor responsible for replacing the block
     * @param before Blockstate of the block before being replaced.
     * @param typeAfter Type of the block after being replaced
     * @param dataAfter Data of the block after being replaced
     * @deprecated Use {@link #queueBlockReplace(Actor, BlockState, BlockState)}
     */
    @Deprecated
    public void queueBlockReplace(Actor actor, BlockState before, Material typeAfter, byte dataAfter) {
        queueBlockReplace(actor, new Location(before.getWorld(), before.getX(), before.getY(), before.getZ()), before.getType(), before.getRawData(), typeAfter, dataAfter);
    }

    /**
     * Logs a block being replaced from the type and data before and the {@link org.bukkit.block.BlockState} after
     *
     * @param actor Actor responsible for replacing the block
     * @param typeBefore Type of the block before being replaced
     * @param dataBefore Data of the block before being replaced
     * @param after Blockstate of the block after actually being placed.
     * @deprecated Use {@link #queueBlockReplace(Actor, BlockState, BlockState)}
     */
    @Deprecated
    public void queueBlockReplace(Actor actor, Material typeBefore, byte dataBefore, BlockState after) {
        queueBlockReplace(actor, new Location(after.getWorld(), after.getX(), after.getY(), after.getZ()), typeBefore, dataBefore, after.getType(), after.getRawData());
    }
    
    /**
     * Logs a block being replaced from the type and data before and type and data after
     *
     * @param actor Actor responsible for replacing the block
     * @param loc Location of the placed block
     * @param typeBefore Type of the block before being replaced
     * @param dataBefore Data of the block before being replaced
     * @param typeAfter Type of the block after being replaced
     * @param dataAfter Data of the block after being replaced
     * @deprecated Use {@link #queueBlockReplace(Actor, BlockState, BlockState)}
     */
    @Deprecated
    public void queueBlockReplace(Actor actor, Location loc, Material typeBefore, byte dataBefore, Material typeAfter, byte dataAfter) {
        if (dataBefore == 0 && (typeBefore != typeAfter)) {
            queueBlock(actor, loc, typeBefore, typeAfter, dataAfter);
        } else {
            queueBlockBreak(actor, loc, typeBefore, dataBefore);
            queueBlockPlace(actor, loc, typeAfter, dataAfter);
        }
    }

    /**
     * Logs an actor interacting with a container block's inventory
     *
     * @param actor The actor interacting with the container
     * @param container The respective container. Must be an instance of an InventoryHolder.
     * @param itemType Type of the item taken/stored
     * @param itemAmount Amount of the item taken/stored
     * @param itemData Data of the item taken/stored
     */
    public void queueChestAccess(Actor actor, BlockState container, Material itemType, short itemAmount, short itemData) {
        if (!(container instanceof InventoryHolder)) {
            throw new IllegalArgumentException("Container must be instanceof InventoryHolder");
        }
        queueChestAccess(actor, new Location(container.getWorld(), container.getX(), container.getY(), container.getZ()), container.getType(), itemType, itemAmount, itemData);
    }

    /**
     * Logs an actor interacting with a container block's inventory
     *
     * @param actor The actor interacting with the container
     * @param loc The location of the container block
     * @param type Type id of the container.
     * @param itemType Type of the item taken/stored
     * @param itemAmount Amount of the item taken/stored
     * @param itemData Data of the item taken/stored
     */
    public void queueChestAccess(Actor actor, Location loc, Material type, Material itemType, short itemAmount, short itemData) {
        queueBlock(actor, loc, type, type, (byte) 0, null, new ChestAccess(itemType, itemAmount, itemData));
    }

    /**
     * Logs a container block break. The block type before is assumed to be o (air). All content is assumed to be taken.
     *
     * @param actor The actor breaking the container
     * @param container Must be an instance of InventoryHolder
     */
    public void queueContainerBreak(Actor actor, BlockState container) {
        if (!(container instanceof InventoryHolder)) {
            return;
        }
        queueContainerBreak(actor, new Location(container.getWorld(), container.getX(), container.getY(), container.getZ()), container.getType(), container.getRawData(), ((InventoryHolder) container).getInventory());
    }

    /**
     * Logs a container block break. The block type before is assumed to be o (air). All content is assumed to be taken.
     *
     * @param actor The actor responsible for breaking the container
     * @param loc The location of the inventory block
     * @param type The type of the container block
     * @param data The data of the container block
     * @param inv The inventory of the container block
     */
    public void queueContainerBreak(Actor actor, Location loc, Material type, byte data, Inventory inv) {
        final ItemStack[] items = compressInventory(inv.getContents());
        for (final ItemStack item : items) {
            queueChestAccess(actor, loc, type, item.getType(), (short) (item.getAmount() * -1), rawData(item));
        }
        queueBlockBreak(actor, loc, type, data);
    }

    /**
     * @param killer Can't be null
     * @param victim Can't be null
     */
    public void queueKill(Entity killer, Entity victim) {
        if (killer == null || victim == null) {
            return;
        }
        Material weapon = Material.AIR;
        Actor killerActor = Actor.actorFromEntity(killer);
        // If it's a projectile kill we want to manually assign the weapon, so check for player before converting a projectile to its source
        if (killer instanceof LivingEntity && ((LivingEntity) killer).getEquipment().getItemInMainHand() != null) {
            weapon = ((LivingEntity) killer).getEquipment().getItemInMainHand().getType();
        }
        if (killer instanceof Projectile) {
            weapon = itemTypeFromProjectileEntity(killer);
            ProjectileSource ps = ((Projectile) killer).getShooter();
            if (ps == null) {
                killerActor = Actor.actorFromEntity(killer);
            } else {
                killerActor = Actor.actorFromProjectileSource(ps);
            }
        }

        queueKill(victim.getLocation(), killerActor, Actor.actorFromEntity(victim), weapon);
    }

    /**
     * This form should only be used when the killer is not an entity e.g. for fall or suffocation damage
     *
     * @param killer Can't be null
     * @param victim Can't be null
     */
    public void queueKill(Actor killer, Entity victim) {
        if (killer == null || victim == null) {
            return;
        }
        queueKill(victim.getLocation(), killer, Actor.actorFromEntity(victim), Material.AIR);
    }

    /**
     * @param world      World the victim was inside.
     * @param killer Name of the killer. Can be null.
     * @param victim Name of the victim. Can't be null.
     * @param weapon     Item id of the weapon. 0 for no weapon.
     * @deprecated Use {@link #queueKill(org.bukkit.Location, de.diddiz.LogBlock.Actor, de.diddiz.LogBlock.Actor, Material)}
     * instead
     */
    @Deprecated // TODO: Remove. Was deprecated and keeping backwards compatibility to IDs is not necessary
    public void queueKill(World world, Actor killer, Actor victim, Material weapon) {
        queueKill(new Location(world, 0, 0, 0), killer, victim, weapon);
    }

    /**
     * @param location Location of the victim.
     * @param killer   Killer Actor. Can be null.
     * @param victim   Victim Actor. Can't be null.
     * @param weapon   Item id of the weapon. 0 for no weapon.
     */
    public void queueKill(Location location, Actor killer, Actor victim, Material weapon) {
        if (victim == null || !isLogged(location.getWorld())) {
            return;
        }
        queue.add(new KillRow(location, killer, victim, weapon));
    }

    /**
     * Logs an actor breaking a sign along with its contents
     *
     * @param actor Actor responsible for breaking the sign
     * @param loc Location of the broken sign
     * @param type  Type of the sign. Must be 63 or 68.
     * @param data Data of the sign being broken
     * @param lines The four lines on the sign.
     */
    public void queueSignBreak(Actor actor, Location loc, Material type, byte data, String[] lines) {
        if (type.getData() != org.bukkit.material.Sign.class || lines == null || lines.length != 4) {
            return;
        }
        queueBlock(actor, loc, type, Material.AIR, data, lines[0] + "\0" + lines[1] + "\0" + lines[2] + "\0" + lines[3], null);
    }

    /**
     * Logs an actor breaking a sign along with its contents
     *
     * @param actor Actor responsible for breaking the sign
     * @param sign The sign being broken
     */
    public void queueSignBreak(Actor actor, org.bukkit.block.Sign sign) {
        queueSignBreak(actor, new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ()), sign.getType(), sign.getRawData(), sign.getLines());
    }

    /**
     * Logs an actor placing a sign along with its contents
     *
     * @param actor Actor placing the sign
     * @param loc Location of the placed sign
     * @param type  Type of the sign. Must be 63 or 68.
     * @param data Data of the placed sign block
     * @param lines The four lines on the sign.
     */
    public void queueSignPlace(Actor actor, Location loc, Material type, byte data, String[] lines) {
        if (type.getData() != org.bukkit.material.Sign.class || lines == null || lines.length != 4) {
            return;
        }
        queueBlock(actor, loc, Material.AIR, type, data, lines[0] + "\0" + lines[1] + "\0" + lines[2] + "\0" + lines[3], null);
    }

    /**
     * Logs an actor placing a sign along with its contents
     *
     * @param actor Actor placing the sign
     * @param sign The palced sign object
     */
    public void queueSignPlace(Actor actor, org.bukkit.block.Sign sign) {
        queueSignPlace(actor, new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ()), sign.getType(), sign.getRawData(), sign.getLines());
    }

    public void queueChat(Actor player, String message) {
        for (String ignored : Config.ignoredChat) {
            if (message.startsWith(ignored)) {
                return;
            }
        }
        if (hiddenPlayers.contains(player.getName().toLowerCase())) {
            return;
        }
        queue.add(new ChatRow(player, message));
    }

    public void queueJoin(Player player) {
        queue.add(new PlayerJoinRow(player));
    }

    public void queueLeave(Player player) {
        queue.add(new PlayerLeaveRow(player));
    }

    // Deprecated methods re-added for API compatability
    // TODO: Remove this as it breaks anyways with Materials?

    /**
     * Logs any block change. Don't try to combine broken and placed blocks.
     * Queue two block changes or use the queueBLockReplace methods.
     *
     * @deprecated Use
     * {@link #queueBlock(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, Material, byte)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlock(String playerName, Location loc, Material typeBefore, Material typeAfter, byte data) {
        queueBlock(actorFromString(playerName), loc, typeBefore, typeAfter, data);
    }

    /**
     * Logs a block break. The type afterwards is assumed to be 0 (air).
     *
     * @param before Blockstate of the block before actually being destroyed.
     * @deprecated Use
     * {@link #queueBlockBreak(de.diddiz.LogBlock.Actor, org.bukkit.block.BlockState)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlockBreak(String playerName, BlockState before) {
        queueBlockBreak(actorFromString(playerName), before);
        
    }

    /**
     * Logs a block break. The block type afterwards is assumed to be 0 (air).
     *
     * @deprecated Use {@link #queueBlockBreak(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, byte)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlockBreak(String playerName, Location loc, Material typeBefore, byte dataBefore) {
        queueBlockBreak(actorFromString(playerName), loc, typeBefore, dataBefore);
    }

    /**
     * Logs a block place. The block type before is assumed to be 0 (air).
     *
     * @param after Blockstate of the block after actually being placed.
     * @depracated Use {@link #queueBlockPlace(de.diddiz.LogBlock.Actor, org.bukkit.block.BlockState)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlockPlace(String playerName, BlockState after) {
        queueBlockPlace(actorFromString(playerName), after);
    }

    /**
     * Logs a block place. The block type before is assumed to be 0 (air).
     * @deprecated Use {@link #queueBlockPlace(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, byte)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlockPlace(String playerName, Location loc, Material type, byte data) {
        queueBlockPlace(actorFromString(playerName), loc, type, data);
    }

    /**
     * @param before Blockstate of the block before actually being destroyed.
     * @param after Blockstate of the block after actually being placed.
     * @deprecated Use {@link #queueBlockReplace(de.diddiz.LogBlock.Actor, org.bukkit.block.BlockState, org.bukkit.block.BlockState)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlockReplace(String playerName, BlockState before, BlockState after) {
        queueBlockReplace(actorFromString(playerName), before, after);
    }

    /**
     * @param before Blockstate of the block before actually being destroyed.
     * @deprecated Use {@link #queueBlockReplace(de.diddiz.LogBlock.Actor, org.bukkit.block.BlockState, Material, byte)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlockReplace(String playerName, BlockState before, Material typeAfter, byte dataAfter) {
        queueBlockReplace(actorFromString(playerName), before, typeAfter, dataAfter);
    }

    /**
     * @param after Blockstate of the block after actually being placed.
     * @deprecated {@link #queueBlockReplace(de.diddiz.LogBlock.Actor, Material, byte, org.bukkit.block.BlockState)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueBlockReplace(String playerName, Material typeBefore, byte dataBefore, BlockState after) {
        queueBlockReplace(actorFromString(playerName), typeBefore, dataBefore, after);
    }

    /**
    * @deprecated use {@link #queueBlockReplace(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, byte, Material, byte)}
    * which supports UUIDs
    */
    @Deprecated
    public void queueBlockReplace(String playerName, Location loc, Material typeBefore, byte dataBefore, Material typeAfter, byte dataAfter) {
        queueBlockReplace(actorFromString(playerName),loc,typeBefore,dataBefore,typeAfter,dataAfter);
    }

    /**
     * @param container The respective container. Must be an instance of an
     * InventoryHolder.
     * @deprecated Use {@link #queueChestAccess(de.diddiz.LogBlock.Actor, org.bukkit.block.BlockState, Material, short, short)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueChestAccess(String playerName, BlockState container, Material itemType, short itemAmount, short itemData) {
        queueChestAccess(actorFromString(playerName),container,itemType,itemAmount,itemData);
    }

    /**
     * @param type Type id of the container.
     * @deprecated Use {@link #queueChestAccess(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, Material, short, short)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueChestAccess(String playerName, Location loc, Material type, Material itemType, short itemAmount, short itemData) {
        queueChestAccess(actorFromString(playerName), loc, type, itemType, itemAmount, itemData);
    }

    /**
     * Logs a container block break. The block type before is assumed to be o
     * (air). All content is assumed to be taken.
     *
     * @param container Must be an instance of InventoryHolder
     * @deprecated Use {@link #queueContainerBreak(de.diddiz.LogBlock.Actor, org.bukkit.block.BlockState)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueContainerBreak(String playerName, BlockState container) {
        queueContainerBreak(actorFromString(playerName), container);
    }

    /**
     * Logs a container block break. The block type before is assumed to be o
     * (air). All content is assumed to be taken.
     * @deprecated Use {@link #queueContainerBreak(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, byte, org.bukkit.inventory.Inventory)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueContainerBreak(String playerName, Location loc, Material type, byte data, Inventory inv) {
        queueContainerBreak(actorFromString(playerName),loc,type,data,inv);
    }

    /**
     * This form should only be used when the killer is not an entity e.g. for
     * fall or suffocation damage
     *
     * @param killer Can't be null
     * @param victim Can't be null
     * @deprecated Use {@link #queueKill(de.diddiz.LogBlock.Actor, org.bukkit.entity.Entity)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueKill(String killer, Entity victim) {
        queueKill(actorFromString(killer),victim);
    }

    /**
     * @param world World the victim was inside.
     * @param killerName Name of the killer. Can be null.
     * @param victimName Name of the victim. Can't be null.
     * @param weapon Item id of the weapon. 0 for no weapon.
     * @deprecated Use {@link #queueKill(org.bukkit.Location, de.diddiz.LogBlock.Actor, de.diddiz.LogBlock.Actor, Material)} instead
     */
    @Deprecated
    public void queueKill(World world, String killerName, String victimName, Material weapon) {
        queueKill(world,actorFromString(killerName),actorFromString(victimName),weapon);
    }

    /**
     * @param location Location of the victim.
     * @param killerName Name of the killer. Can be null.
     * @param victimName Name of the victim. Can't be null.
     * @param weapon Item id of the weapon. 0 for no weapon.
     * @deprecated Use {@link #queueKill(org.bukkit.Location, de.diddiz.LogBlock.Actor, de.diddiz.LogBlock.Actor, Material)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueKill(Location location, String killerName, String victimName, Material weapon) {
        queueKill(location,actorFromString(killerName),actorFromString(victimName),weapon);
    }

    /**
     * @param type Type of the sign. Must be 63 or 68.
     * @param lines The four lines on the sign.
     * @deprecated Use {@link #queueSignBreak(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, byte, java.lang.String[])}
     * which supports UUIDs
     */
    @Deprecated
    public void queueSignBreak(String playerName, Location loc, Material type, byte data, String[] lines) {
        queueSignBreak(actorFromString(playerName),loc,type,data,lines);
    }

    /**
     * @deprecated Use {@link #queueSignBreak(de.diddiz.LogBlock.Actor, org.bukkit.block.Sign)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueSignBreak(String playerName, org.bukkit.block.Sign sign) {
        queueSignBreak(actorFromString(playerName),sign);
    }

    /**
     * @param type Type of the sign. Must be 63 or 68.
     * @param lines The four lines on the sign.
     * @deprecated Use {@link #queueSignPlace(de.diddiz.LogBlock.Actor, org.bukkit.Location, Material, byte, java.lang.String[])}
     * which supports UUIDs
     */
    @Deprecated
    public void queueSignPlace(String playerName, Location loc, Material type, byte data, String[] lines) {
        queueSignPlace(actorFromString(playerName),loc,type,data,lines);
    }

    /**
     * @deprecated Use {@link #queueSignPlace(de.diddiz.LogBlock.Actor, org.bukkit.block.Sign)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueSignPlace(String playerName, org.bukkit.block.Sign sign) {
        queueSignPlace(actorFromString(playerName),sign);
    }
    
    /**
     * @deprecated Use {@link #queueChat(de.diddiz.LogBlock.Actor, java.lang.String)}
     * which supports UUIDs
     */
    @Deprecated
    public void queueChat(String player, String message) {
        queueChat(actorFromString(player),message);
    }

    @Override
    public synchronized void run() {
        if (queue.isEmpty() || !lock.tryLock()) {
            return;
        }
        long startTime = System.currentTimeMillis();
        int startSize = queue.size();

        final Connection conn = logblock.getConnection();
        Statement state = null;
        if (Config.queueWarningSize > 0 && queue.size() >= Config.queueWarningSize) {
            getLogger().info("[Consumer] Queue overloaded. Size: " + getQueueSize());
        }

        int count = 0;

        try {
            if (conn == null) {
                return;
            }
            conn.setAutoCommit(false);
            state = conn.createStatement();
            final long start = System.currentTimeMillis();
            process:
            while (!queue.isEmpty() && (System.currentTimeMillis() - start < timePerRun || count < forceToProcessAtLeast)) {
                final Row r = queue.poll();
                if (r == null) {
                    continue;
                }
                for (final Actor actor : r.getActors()) {
                    if (!playerIds.containsKey(actor)) {
                        if (!addPlayer(state, actor)) {
                            if (!failedPlayers.contains(actor)) {
                                failedPlayers.add(actor);
                                getLogger().warning("[Consumer] Failed to add player " + actor.getName());
                            }
                            continue process;
                        }
                    }
                }
                if (r instanceof PreparedStatementRow) {
                    PreparedStatementRow PSRow = (PreparedStatementRow) r;
                    if (r instanceof MergeableRow) {
                        int batchCount = count;
                        // if we've reached our row target but not exceeded our time target, allow merging of up to 50% of our row limit more rows
                        if (count > forceToProcessAtLeast) {
                            batchCount = forceToProcessAtLeast / 2;
                        }
                        while (!queue.isEmpty()) {
                            MergeableRow mRow = (MergeableRow) PSRow;
                            Row s = queue.peek();
                            if (s == null) {
                                break;
                            }
                            if (!(s instanceof MergeableRow)) {
                                break;
                            }
                            MergeableRow mRow2 = (MergeableRow) s;
                            if (mRow.canMerge(mRow2)) {
                                PSRow = mRow.merge((MergeableRow) queue.poll());
                                count++;
                                batchCount++;
                                if (batchCount > forceToProcessAtLeast) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    PSRow.setConnection(conn);
                    try {
                        PSRow.executeStatements();
                    } catch (final SQLException ex) {
                        getLogger().log(Level.SEVERE, "[Consumer] SQL exception on insertion: ", ex);
                        break;
                    }
                } else {
                    for (final String insert : r.getInserts()) {
                        try {
                            state.execute(insert);
                        } catch (final SQLException ex) {
                            getLogger().log(Level.SEVERE, "[Consumer] SQL exception on " + insert + ": ", ex);
                            break process;
                        }
                    }
                }

                count++;
            }
            conn.commit();
        } catch (final SQLException ex) {
            getLogger().log(Level.SEVERE, "[Consumer] SQL exception", ex);
        } finally {
            try {
                if (state != null) {
                    state.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (final SQLException ex) {
                getLogger().log(Level.SEVERE, "[Consumer] SQL exception on close", ex);
            }
            lock.unlock();

            if (debug) {
                long timeElapsed = System.currentTimeMillis() - startTime;
                float rowPerTime = count / timeElapsed;
                getLogger().log(Level.INFO, "[Consumer] Finished consumer cycle in " + timeElapsed + " milliseconds.");
                getLogger().log(Level.INFO, "[Consumer] Total rows processed: " + count + ". row/time: " + String.format("%.4f", rowPerTime));
            }
        }
    }

    public void writeToFile() throws FileNotFoundException {
        final long time = System.currentTimeMillis();
        final Set<Actor> insertedPlayers = new HashSet<Actor>();
        int counter = 0;
        new File("plugins/LogBlock/import/").mkdirs();
        PrintWriter writer = new PrintWriter(new File("plugins/LogBlock/import/queue-" + time + "-0.sql"));
        while (!queue.isEmpty()) {
            final Row r = queue.poll();
            if (r == null) {
                continue;
            }
            for (final Actor actor : r.getActors()) {
                if (!playerIds.containsKey(actor) && !insertedPlayers.contains(actor)) {
                    // Odd query contruction is to work around innodb auto increment behaviour - bug #492
                    writer.println("INSERT IGNORE INTO `lb-players` (playername,UUID) SELECT '" + mysqlTextEscape(actor.getName()) + "','" + actor.getUUID() + "' FROM `lb-players` WHERE NOT EXISTS (SELECT NULL FROM `lb-players` WHERE UUID = '" + actor.getUUID() + "') LIMIT 1;");
                    insertedPlayers.add(actor);
                }
            }
            for (final String insert : r.getInserts()) {
                writer.println(insert);
            }
            counter++;
            if (counter % 1000 == 0) {
                writer.close();
                writer = new PrintWriter(new File("plugins/LogBlock/import/queue-" + time + "-" + counter / 1000 + ".sql"));
            }
        }
        writer.close();
    }

    int getQueueSize() {
        return queue.size();
    }

    static void hide(Player player) {
        hiddenPlayers.add(player.getName().toLowerCase());
    }

    static void unHide(Player player) {
        hiddenPlayers.remove(player.getName().toLowerCase());
    }

    static boolean toggleHide(Player player) {
        final String playerName = player.getName().toLowerCase();
        if (hiddenPlayers.contains(playerName)) {
            hiddenPlayers.remove(playerName);
            return false;
        }
        hiddenPlayers.add(playerName);
        return true;
    }

    private boolean addPlayer(Statement state, Actor actor) throws SQLException {
        // Odd query contruction is to work around innodb auto increment behaviour - bug #492
        String name = actor.getName();
        String uuid = actor.getUUID();
        state.execute("INSERT IGNORE INTO `lb-players` (playername,UUID) SELECT '" + mysqlTextEscape(name) + "','" + uuid + "' FROM `lb-players` WHERE NOT EXISTS (SELECT NULL FROM `lb-players` WHERE UUID = '" + uuid + "') LIMIT 1;");
        final ResultSet rs = state.executeQuery("SELECT playerid FROM `lb-players` WHERE UUID = '" + uuid + "'");
        if (rs.next()) {
            playerIds.put(actor, rs.getInt(1));
        }
        rs.close();
        return playerIds.containsKey(actor);
    }

    @Deprecated
    private void queueBlock(Actor actor, Location loc, Material typeBefore, Material typeAfter, byte data, String signtext, ChestAccess ca) {
    
    }
    
    
    private void queueBlock(Actor actor, BlockState before, BlockState after, ChestAccess ca) {
        if (Config.fireCustomEvents) {
            // Create and call the event
            BlockChangePreLogEvent event = new BlockChangePreLogEvent(actor, before, after, ca);
            logblock.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            // Update variables
            actor = event.getOwnerActor();
            before = event.getBefore();
            after = event.getAfter();
            ca = event.getChestAccess();
        }
        // Do this last so LogBlock still has final say in what is being added
        if (actor == null || before == null || after == null || hiddenPlayers.contains(actor.getName().toLowerCase()) || !isLogged(before.getLocation().getWorld()) || before.getType() != after.getType() && hiddenBlocks.contains(before.getType()) && hiddenBlocks.contains(after.getType())) {
            return;
        }
        queue.add(new BlockRow(before.getLocation(), actor, before, after, ca));
    }

    private String playerID(Actor actor) {
        if (actor == null) {
            return "NULL";
        }
        final Integer id = playerIds.get(actor);
        if (id != null) {
            return id.toString();
        }
        return "(SELECT playerid FROM `lb-players` WHERE UUID = '" + actor.getUUID() + "')";
    }

    private Integer playerIDAsInt(Actor actor) {
        if (actor == null) {
            return null;
        }
        return playerIds.get(actor);
    }

    private static interface Row {
        String[] getInserts();

        /**
         * @deprecated - Names are not guaranteed to be unique. Use {@link #getActors() }
         */
        String[] getPlayers();

        Actor[] getActors();
    }

    private interface PreparedStatementRow extends Row {

        abstract void setConnection(Connection connection);

        abstract void executeStatements() throws SQLException;
    }

    private interface MergeableRow extends PreparedStatementRow {
        abstract boolean isUnique();

        abstract boolean canMerge(MergeableRow row);

        abstract MergeableRow merge(MergeableRow second);
    }

    private class BlockRow extends BlockChange implements MergeableRow {
        private Connection connection;

        public BlockRow(Location loc, Actor actor, BlockState before, BlockState after, ChestAccess ca) {
            super(System.currentTimeMillis() / 1000, loc, actor, before, after, ca);
        }

        @Override
        public String[] getInserts() {
            final String table = getWorldConfig(loc.getWorld()).table;
            final String[] inserts = new String[ca != null || signtext != null ? 2 : 1];
            inserts[0] = "INSERT INTO `" + table + "` (date, playerid, replaced, type, data, x, y, z) VALUES (FROM_UNIXTIME(" + date + "), " + playerID(actor) + ", " + replaced + ", " + type + ", " + data + ", '" + loc.getBlockX() + "', " + safeY(loc) + ", '" + loc.getBlockZ() + "');";
            if (signtext != null) {
                inserts[1] = "INSERT INTO `" + table + "-sign` (id, signtext) values (LAST_INSERT_ID(), '" + mysqlTextEscape(signtext) + "');";
            } else if (ca != null) {
                inserts[1] = "INSERT INTO `" + table + "-chest` (id, itemtype, itemamount, itemdata) values (LAST_INSERT_ID(), " + ca.itemType + ", " + ca.itemAmount + ", " + ca.itemData + ");";
            }
            return inserts;
        }

        @Override
        public String[] getPlayers() {
            return new String[]{actor.getName()};
        }

        @Override
        public Actor[] getActors() {
            return new Actor[]{actor};
        }

        @Override
        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void executeStatements() throws SQLException {
            final String table = getWorldConfig(loc.getWorld()).table;

            PreparedStatement ps1 = null;
            PreparedStatement ps = null;
            try {
                ps1 = connection.prepareStatement("INSERT INTO `" + table + "` (date, playerid, replaced, type, data, x, y, z) VALUES(FROM_UNIXTIME(?), " + playerID(actor) + ", ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps1.setLong(1, date);
                ps1.setInt(2, replaced.getId());
                ps1.setInt(3, type.getId());
                ps1.setInt(4, data);
                ps1.setInt(5, loc.getBlockX());
                ps1.setInt(6, safeY(loc));
                ps1.setInt(7, loc.getBlockZ());
                ps1.executeUpdate();

                int id;
                ResultSet rs = ps1.getGeneratedKeys();
                rs.next();
                id = rs.getInt(1);

                if (signtext != null) {
                    ps = connection.prepareStatement("INSERT INTO `" + table + "-sign` (signtext, id) VALUES(?, ?)");
                    ps.setString(1, signtext);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                } else if (ca != null) {
                    ps = connection.prepareStatement("INSERT INTO `" + table + "-chest` (itemtype, itemamount, itemdata, id) values (?, ?, ?, ?)");
                    ps.setInt(1, ca.itemType.getId());
                    ps.setInt(2, ca.itemAmount);
                    ps.setInt(3, ca.itemData);
                    ps.setInt(4, id);
                    ps.executeUpdate();
                }
            } catch (final SQLException ex) {
                if (ps1 != null) {
                    getLogger().log(Level.SEVERE, "[Consumer] Troublesome query: " + ps1.toString());
                }
                if (ps != null) {
                    getLogger().log(Level.SEVERE, "[Consumer] Troublesome query: " + ps.toString());
                }
                throw ex;
            } finally {
                // individual try/catch here, though ugly, prevents resource leaks
                if (ps1 != null) {
                    try {
                        ps1.close();
                    } catch (SQLException e) {
                        // ideally should log to logger, none is available in this class
                        // at the time of this writing, so I'll leave that to the plugin
                        // maintainers to integrate if they wish
                        e.printStackTrace();
                    }
                }

                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public boolean isUnique() {
            return !(signtext == null && ca == null && playerIds.containsKey(actor));
        }

        @Override
        public boolean canMerge(MergeableRow row) {
            return !this.isUnique() && !row.isUnique() && row instanceof BlockRow && getWorldConfig(loc.getWorld()).table.equals(getWorldConfig(((BlockRow) row).loc.getWorld()).table);
        }

        @Override
        public MergeableRow merge(MergeableRow singleRow) {
            return new MultiBlockChangeRow(this, (BlockRow) singleRow);
        }
    }

    private class MultiBlockChangeRow implements MergeableRow {
        private List<BlockRow> rows = new ArrayList<BlockRow>();
        private Connection connection;
        private Set<String> players = new HashSet<String>();
        private Set<Actor> actors = new HashSet<Actor>();
        private String table;

        MultiBlockChangeRow(BlockRow first, BlockRow second) {
            if (first.isUnique() || second.isUnique()) {
                throw new IllegalArgumentException("Can't merge a unique row");
            }
            rows.add(first);
            rows.add(second);
            actors.addAll(Arrays.asList(first.getActors()));
            actors.addAll(Arrays.asList(second.getActors()));
            players.addAll(Arrays.asList(first.getPlayers()));
            players.addAll(Arrays.asList(second.getPlayers()));
            table = getWorldConfig(first.loc.getWorld()).table;
        }

        @Override
        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void executeStatements() throws SQLException {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement("INSERT INTO `" + table + "` (date, playerid, replaced, type, data, x, y, z) VALUES(FROM_UNIXTIME(?), ?, ?, ?, ?, ?, ?, ?)");
                for (BlockRow row : rows) {
                    ps.setLong(1, row.date);
                    ps.setInt(2, playerIds.get(row.actor));
                    ps.setInt(3, row.replaced.getId());
                    ps.setInt(4, row.type.getId());
                    ps.setInt(5, row.data);
                    ps.setInt(6, row.loc.getBlockX());
                    ps.setInt(7, safeY(row.loc));
                    ps.setInt(8, row.loc.getBlockZ());
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (final SQLException ex) {
                if (ps != null) {
                    getLogger().log(Level.SEVERE, "[Consumer] Troublesome query: " + ps.toString());
                }
                throw ex;
            } finally {
                // individual try/catch here, though ugly, prevents resource leaks
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public boolean isUnique() {
            return true;
        }

        @Override
        public boolean canMerge(MergeableRow row) {
            return !row.isUnique() && row instanceof BlockRow && table.equals(getWorldConfig(((BlockRow) row).loc.getWorld()).table);
        }

        @Override
        public MergeableRow merge(MergeableRow second) {
            if (second.isUnique()) {
                throw new IllegalArgumentException("Can't merge a unique row");
            }
            rows.add((BlockRow) second);
            actors.addAll(Arrays.asList(second.getActors()));
            players.addAll(Arrays.asList(second.getPlayers()));
            return this;
        }

        @Override
        public String[] getInserts() {
            List<String> l = new ArrayList<String>();
            for (BlockRow row : rows) {
                l.addAll(Arrays.asList(row.getInserts()));
            }
            return (String[]) l.toArray();
        }

        @Override
        public String[] getPlayers() {
            return (String[]) players.toArray();
        }

        @Override
        public Actor[] getActors() {
            return (Actor[]) actors.toArray();
        }
    }

    private class KillRow implements Row {
        final long date;
        final Actor killer, victim;
        final Material weapon;
        final Location loc;

        KillRow(Location loc, Actor attacker, Actor defender, Material weapon) {
            date = System.currentTimeMillis() / 1000;
            this.loc = loc;
            killer = attacker;
            victim = defender;
            this.weapon = weapon;
        }

        @Override
        public String[] getInserts() {
            return new String[]{"INSERT INTO `" + getWorldConfig(loc.getWorld()).table + "-kills` (date, killer, victim, weapon, x, y, z) VALUES (FROM_UNIXTIME(" + date + "), " + playerID(killer) + ", " + playerID(victim) + ", " + weapon + ", " + loc.getBlockX() + ", " + safeY(loc) + ", " + loc.getBlockZ() + ");"};
        }

        @Override
        public String[] getPlayers() {
            return new String[]{killer.getName(), victim.getName()};
        }

        @Override
        public Actor[] getActors() {
            return new Actor[]{killer, victim};
        }
    }

    private class ChatRow extends ChatMessage implements PreparedStatementRow {
        private Connection connection;

        ChatRow(Actor player, String message) {
            super(player, message);
        }

        @Override
        public String[] getInserts() {
            return new String[]{"INSERT INTO `lb-chat` (date, playerid, message) VALUES (FROM_UNIXTIME(" + date + "), " + playerID(player) + ", '" + mysqlTextEscape(message) + "');"};
        }

        @Override
        public String[] getPlayers() {
            return new String[]{player.getName()};
        }

        @Override
        public Actor[] getActors() {
            return new Actor[]{player};
        }

        @Override
        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void executeStatements() throws SQLException {
            boolean noID = false;
            Integer id;

            String sql = "INSERT INTO `lb-chat` (date, playerid, message) VALUES (FROM_UNIXTIME(?), ";
            if ((id = playerIDAsInt(player)) == null) {
                noID = true;
                sql += playerID(player) + ", ";
            } else {
                sql += "?, ";
            }
            sql += "?)";

            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement(sql);
                ps.setLong(1, date);
                if (!noID) {
                    ps.setInt(2, id);
                    ps.setString(3, message);
                } else {
                    ps.setString(2, message);
                }
                ps.execute();
            }
            // we intentionally do not catch SQLException, it is thrown to the caller
            finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        // should print to a Logger instead if one is ever added to this class
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class PlayerJoinRow implements Row {
        private final Actor player;
        private final long lastLogin;
        private final String ip;

        PlayerJoinRow(Player player) {
            this.player = Actor.actorFromEntity(player);
            lastLogin = System.currentTimeMillis() / 1000;
            ip = player.getAddress().toString().replace("'", "\\'");
        }

        @Override
        public String[] getInserts() {
            if (logPlayerInfo) {
                return new String[]{"UPDATE `lb-players` SET lastlogin = FROM_UNIXTIME(" + lastLogin + "), firstlogin = IF(firstlogin = 0, FROM_UNIXTIME(" + lastLogin + "), firstlogin), ip = '" + ip + "', playername = '" + mysqlTextEscape(player.getName()) + "' WHERE UUID = '" + player.getUUID() + "';"};
            }
            return new String[]{"UPDATE `lb-players` SET playername = '" + mysqlTextEscape(player.getName()) + "' WHERE UUID = '" + player.getUUID() + "';"};
        }

        @Override
        public String[] getPlayers() {
            return new String[]{player.getName()};
        }

        @Override
        public Actor[] getActors() {
            return new Actor[]{player};
        }
    }

    private class PlayerLeaveRow implements Row {
        ;
        private final long leaveTime;
        private final Actor actor;

        PlayerLeaveRow(Player player) {
            leaveTime = System.currentTimeMillis() / 1000;
            actor = Actor.actorFromEntity(player);
        }

        @Override
        public String[] getInserts() {
            if (logPlayerInfo) {
                return new String[]{"UPDATE `lb-players` SET onlinetime = onlinetime + TIMESTAMPDIFF(SECOND, lastlogin, FROM_UNIXTIME('" + leaveTime + "')), playername = '" + mysqlTextEscape(actor.getName()) + "' WHERE lastlogin > 0 && UUID = '" + actor.getUUID() + "';"};
            }
            return new String[]{"UPDATE `lb-players` SET playername = '" + mysqlTextEscape(actor.getName()) + "' WHERE UUID = '" + actor.getUUID() + "';"};
        }

        @Override
        public String[] getPlayers() {
            return new String[]{actor.getName()};
        }

        @Override
        public Actor[] getActors() {
            return new Actor[]{actor};
        }
    }
    
    private int safeY(Location loc) {    
        int safeY = loc.getBlockY();
        if (safeY<0) safeY = 0;
        if (safeY>65535) safeY=65535;
        return safeY;
    }
}
