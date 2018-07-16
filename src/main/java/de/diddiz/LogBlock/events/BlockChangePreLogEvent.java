package de.diddiz.LogBlock.events;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.ChestAccess;
import de.diddiz.util.BukkitUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.HandlerList;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BlockChangePreLogEvent extends PreLogEvent {

    private static final HandlerList handlers = new HandlerList();
    private BlockState before, after;
    private ChestAccess chestAccess;
    
    public BlockChangePreLogEvent(Actor owner, BlockState before, BlockState after, ChestAccess chestAccess) {
        super(owner);
        this.before = before;
        this.after = after;
        this.chestAccess = chestAccess;
    }

    public Location getLocation() {

        return before.getLocation();
    }

    @Deprecated
    public void setLocation(Location location) {
        
        throw new UnsupportedOperationException("Setting the location of a BlockChangePreLogEvent is no longer supported!");
    }
    
    public BlockState getBefore() {
        
        return before;
    }
    
    public void setBefore(BlockState before) {
        
        this.before = before;
    }
    
    public BlockState getAfter() {
        
        return after;
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public void setAfter(BlockState after) {
        
        this.after = after;
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public Material getTypeBefore() {

        return before.getType();
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public void setTypeBefore(Material typeBefore) {

        before.setType(typeBefore);
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public Material getTypeAfter() {

        return before.getType();
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public void setTypeAfter(Material typeAfter) {

        after.setType(typeAfter);
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public byte getData() {

        return before.getData().getData();
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public void setData(byte data) {

        this.before.getData().setData(data);
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public String getSignText() {

        if (before instanceof Sign) {
            return Arrays.stream(((Sign) before).getLines()).collect(Collectors.joining("\0"));
        } else if (after instanceof Sign) {
            return Arrays.stream(((Sign) after).getLines()).collect(Collectors.joining("\0"));
        }
        return null;
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    public void setSignText(String[] signText) {

        if (signText != null) {
            // Check for problems
            Validate.noNullElements(signText, "No null lines");
            Validate.isTrue(signText.length == 4, "Sign text must be 4 strings");
    
            if (before instanceof Sign) {
                System.arraycopy(signText, 0, ((Sign) before).getLines(), 0, 4);
            }
            if (after instanceof Sign) {
                System.arraycopy(signText, 0, ((Sign) after).getLines(), 0, 4);
            }
        } else {
            if (before instanceof Sign) {
                Arrays.fill(((Sign) before).getLines(), "");
            }
            if (after instanceof Sign) {
                Arrays.fill(((Sign) after).getLines(), "");
            }
        }
    }
    
    /**
     * @deprecated Directly use the BlockStates
     */
    @Deprecated
    private boolean isValidSign() {

        if (after instanceof Sign && before.getType() == Material.AIR) {
            return true;
        }
        if (before instanceof Sign && after.getType() == Material.AIR) {
            return true;
        }
        if (after instanceof Sign && before instanceof Sign) {
            return true;
        }
        return false;
    }

    public ChestAccess getChestAccess() {

        return chestAccess;
    }

    public void setChestAccess(ChestAccess chestAccess) {

        this.chestAccess = chestAccess;
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
