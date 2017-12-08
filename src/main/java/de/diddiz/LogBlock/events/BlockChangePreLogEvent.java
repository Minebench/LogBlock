package de.diddiz.LogBlock.events;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.ChestAccess;
import de.diddiz.util.BukkitUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.material.Sign;

public class BlockChangePreLogEvent extends PreLogEvent {

    private static final HandlerList handlers = new HandlerList();
    private Location location;
    private Material typeBefore, typeAfter;
    private byte data;
    private String signText;
    private ChestAccess chestAccess;

    @Deprecated
    public BlockChangePreLogEvent(Actor owner, Location location, int typeBefore, int typeAfter, byte data,
                                  String signText, ChestAccess chestAccess) {
        this(owner, location, Material.getMaterial(typeBefore), Material.getMaterial(typeAfter), data, signText, chestAccess);
    }
    
    public BlockChangePreLogEvent(Actor owner, Location location, Material typeBefore, Material typeAfter, byte data,
                                  String signText, ChestAccess chestAccess) {
        super(owner);
        this.location = location;
        this.typeBefore = typeBefore;
        this.typeAfter = typeAfter;
        this.data = data;
        this.signText = signText;
        this.chestAccess = chestAccess;
    }

    public Location getLocation() {

        return location;
    }

    public void setLocation(Location location) {

        this.location = location;
    }

    
    public Material getTypeBefore() {

        return typeBefore;
    }

    public void setTypeBefore(Material typeBefore) {

        this.typeBefore = typeBefore;
    }

    public Material getTypeAfter() {

        return typeAfter;
    }

    public void setTypeAfter(Material typeAfter) {

        this.typeAfter = typeAfter;
    }

    public byte getData() {

        return data;
    }

    public void setData(byte data) {

        this.data = data;
    }

    public String getSignText() {

        return signText;
    }

    public void setSignText(String[] signText) {

        if (signText != null) {
            // Check for block
            Validate.isTrue(isValidSign(), "Must be valid sign block");

            // Check for problems
            Validate.noNullElements(signText, "No null lines");
            Validate.isTrue(signText.length == 4, "Sign text must be 4 strings");

            this.signText = signText[0] + "\0" + signText[1] + "\0" + signText[2] + "\0" + signText[3];
        } else {
            this.signText = null;
        }
    }

    private boolean isValidSign() {

        if (BukkitUtils.equalTypes(Material.SIGN_POST, typeAfter) && typeBefore == Material.AIR) {
            return true;
        }
        if (BukkitUtils.equalTypes(Material.SIGN_POST, typeBefore) && typeAfter == Material.AIR) {
            return true;
        }
        if (BukkitUtils.equalTypes(Material.SIGN_POST, typeAfter) && typeBefore == typeAfter) {
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
