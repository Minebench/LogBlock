package de.diddiz.util;

import org.bukkit.Material;

import java.util.List;

public class Block {
    private Material type;
    private int data;

    /**
     * @param type The type of the block
     * @param data  The data for the block, -1 for any data
     */
    public Block(Material type, int data) {
        this.type = type;
        this.data = data;
    }

    @Deprecated
    public int getBlock() {
        return this.type.getId();
    }
    
    public Material getType() {
        return type;
    }
    
    public int getData() {
        return this.data;
    }

    public static boolean inList(List<Block> types, Material blockType) {
        for (Block block : types) {
            if (block.getType() == blockType) {
                return true;
            }
        }
        return false;
    }
}
