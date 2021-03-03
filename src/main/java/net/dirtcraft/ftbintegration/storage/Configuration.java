package net.dirtcraft.ftbintegration.storage;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;

import java.util.HashSet;
import java.util.stream.Stream;

@ConfigSerializable
public class Configuration {
    private final FtbIntegration plugin = FtbIntegration.INSTANCE;

    @Setting(value = "Use-Blacklisted-Items",
            comment = "Items not allowed to be used in others claims without permission.")
    private final HashSet<ItemType> itemBlacklist = new HashSet<>();

    @Setting(value = "Edit-Whitelisted-Blocks",
            comment = "Blocks allowed to be edited in others claims without explicit permission.")
    private final HashSet<BlockType> blockEditWhitelist = new HashSet<>();

    @Setting(value = "Interact-Whitelisted-Blocks",
            comment = "Blocks allowed to be interacted with in others claims without explicit permission.")
    private final HashSet<BlockType> blockInteractWhitelist = new HashSet<>();

    public Stream<ItemType> getItemBlacklist(){
        return itemBlacklist.stream();
    }

    public Stream<BlockType> getInteractWhitelist(){
        return blockInteractWhitelist.stream();
    }

    public Stream<BlockType> getEditWhitelist(){
        return blockEditWhitelist.stream();
    }

    public boolean isBlockEditAllowed(Block block) {
        return blockEditWhitelist.contains(block);
    }

    public boolean isBlockInteractAllowed(Block block) {
        return blockInteractWhitelist.contains(block);
    }

    public boolean isItemUseAllowed(Item item){
        return !itemBlacklist.contains(item);
    }

    public boolean addItemBlacklist(Item item){
        boolean r = itemBlacklist.add((ItemType) item);
        plugin.saveConfig();
        return r;
    }

    public boolean removeItemBlacklist(Item item){
        boolean r = itemBlacklist.remove(item);
        plugin.saveConfig();
        return r;
    }

    public boolean addEditWhitelist(Block block){
        boolean r = blockEditWhitelist.add((BlockType) block);
        plugin.saveConfig();
        return r;
    }

    public boolean removeEditWhitelist(Block block){
        boolean r = blockEditWhitelist.remove(block);
        plugin.saveConfig();
        return r;
    }

    public boolean addInteractWhitelist(Block block){
        boolean r = blockInteractWhitelist.add((BlockType) block);
        plugin.saveConfig();
        return r;
    }

    public boolean removeInteractWhitelist(Block block){
        boolean r = blockInteractWhitelist.remove(block);
        plugin.saveConfig();
        return r;
    }
}
