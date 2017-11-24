package de.diddiz.LogBlock;

import org.bukkit.Material;

public class ChestAccess {
    final Material itemType;
    final short itemAmount, itemData;

    public ChestAccess(Material itemType, short itemAmount, short itemData) {
        this.itemType = itemType;
        this.itemAmount = itemAmount;
        this.itemData = itemData >= 0 ? itemData : 0;
    }
}
