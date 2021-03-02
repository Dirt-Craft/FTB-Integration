package net.dirtcraft.ftbintegration.storage;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import javax.annotation.Nullable;
import java.util.HashSet;

@ConfigSerializable
public class Configuration {
    private final FtbIntegration plugin = FtbIntegration.INSTANCE;

    @Setting(value = "Use-Blacklisted-Items",
            comment = "Items not allowed to be used in others claims without permission.")
    private final HashSet<String> itemBlacklist = new HashSet<>();

    @Setting(value = "Edit-Whitelisted-Blocks",
            comment = "Blocks allowed to be edited in others claims without explicit permission.")
    private final HashSet<String> blockEditWhitelist = new HashSet<>();

    @Setting(value = "Interact-Whitelisted-Blocks",
            comment = "Blocks allowed to be interacted with in others claims without explicit permission.")
    private final HashSet<String> blockInteractWhitelist = new HashSet<>();

    public boolean isBlockEditAllowed(Block block) {
        String identifier = formatId(block);
        return blockEditWhitelist.contains(identifier);
    }

    public boolean isBlockInteractAllowed(Block block) {
        String identifier = formatId(block);
        return blockInteractWhitelist.contains(identifier);
    }

    public boolean isItemUseAllowed(Item item){
        String identifier = formatId(item);
        return !itemBlacklist.contains(identifier);
    }

    public boolean addItemBlacklist(Item item){
        String identifier = formatId(item);
        boolean r = itemBlacklist.add(identifier);
        plugin.saveConfig();
        return r;
    }

    public boolean removeItemBlacklist(Item item){
        String identifier = formatId(item);
        boolean r = itemBlacklist.remove(identifier);
        plugin.saveConfig();
        return r;
    }

    public boolean addEditWhitelist(Block block){
        String identifier = formatId(block);
        boolean r = blockEditWhitelist.add(identifier);
        plugin.saveConfig();
        return r;
    }

    public boolean removeEditWhitelist(Block block){
        String identifier = formatId(block);
        boolean r = blockEditWhitelist.remove(identifier);
        plugin.saveConfig();
        return r;
    }

    public boolean addInteractWhitelist(Block block){
        String identifier = formatId(block);
        boolean r = blockInteractWhitelist.add(identifier);
        plugin.saveConfig();
        return r;
    }

    public boolean removeInteractWhitelist(Block block){
        String identifier = formatId(block);
        boolean r = blockInteractWhitelist.add(identifier);
        plugin.saveConfig();
        return r;
    }

    private static String formatId(@Nullable IForgeRegistryEntry<?> item) {
        if (item == null || item.getRegistryName() == null) return "minecraft:air" ;
        else return item.getRegistryName().toString().toLowerCase();
    }
}
