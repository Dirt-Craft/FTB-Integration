package net.dirtcraft.ftbintegration.storage;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class Permission {
    private static final String BASE = "ftbintegration";
    private static final String CLAIMS_BASE = resolvePermission(BASE, "claims");
    private static final String FLAG_BASE =  resolvePermission(CLAIMS_BASE, "flags");
    private static final String BADGE_BASE = resolvePermission(BASE, "badges");
    private static final String CONFIG_BASE = resolvePermission(BASE, "config");
    private static final String RESTRICT_BASE = resolvePermission(CONFIG_BASE, "restrict");

    //Admin Nodes
    public static final String SEE_INFO = resolvePermission(BASE, "tool", "info");
    public static final String RELOAD_CONFIG = resolvePermission(BASE, "reload");
    public static final String BYPASS = resolvePermission(CLAIMS_BASE, "bypass");
    public static final String DEBUG = resolvePermission(CLAIMS_BASE, "debug");

    //Claim Nodes
    public static final String CLAIM_CHUNK = resolvePermission(CLAIMS_BASE, "claim", "base");
    public static final String CLAIM_OTHER = resolvePermission(CLAIMS_BASE, "claim", "others");
    public static final String FLAG_MOB_SPAWN = resolvePermission(FLAG_BASE, "mobspawn");

    //Badge Nodes
    public static final String STAFF_BADGE = resolvePermission(BADGE_BASE, "staff");
    public static final String BADGE_SET = resolvePermission(BADGE_BASE, "get");
    public static final String BADGE_OTHERS = resolvePermission(BADGE_BASE, "other");
    public static final String BADGE_CLEAR = resolvePermission(BADGE_BASE, "clear");
    public static final String BADGE_GET = resolvePermission(BADGE_BASE, "get");

    //Claims Meta
    public static final String CHUNK_CLAIM_MODIFY_GROUP = resolvePermission(CLAIMS_BASE, "chunks", "modify", "group");
    public static final String CHUNK_CLAIM_META = resolvePermission(CLAIMS_BASE, "chunks", "max");
    public static final String CHUNK_LOADER_META = resolvePermission(CLAIMS_BASE, "loaders", "max");

    //Configuration
    public static final String RESTRICT_MODIFY = resolvePermission(RESTRICT_BASE, "modify");
    public static final String RESTRICT_VIEW = resolvePermission(RESTRICT_BASE, "view");


    /* Permissions Quick-List
    Claiming In General:
     - ftbintegration.claims.claim.worldId
     - ftbintegration.claims.debug
     - ftbintegration.claims.bypass
     - ftbintegration.tool.info

    FTB:U Exposed:
     - ftbutilities.other_player.claims.unclaim
     - ftbutilities.other_player.claims.see_info

    Staff Badge:
      - ftbintegration.badges.staff

    Flags:
     - ftbintegration.claims.flags.mobspawn

    Meta:
     - ftbintegration.claims.chunks.modify.group
     - ftbintegration.claims.chunks.max
     - ftbintegration.claims.loaders.max

    Config:
     - ftbintegration.config.restrict.modify
     - ftbintegration.config.restrict.view
     */


    public static String resolveClaimDimension(String world){
        return resolvePermission(CLAIM_CHUNK, world);
    }

    public static String resolveBlockEdit(Block block){
        return resolvePermission("ftbutilities.claims.block.edit", formatId(block));
    }

    public static String resolveBlockInteract(Block block){
        return resolvePermission("ftbutilities.claims.block.interact", formatId(block));
    }

    public static String resolveItemUse(Item item){
        return resolvePermission("ftbutilities.claims.item", formatId(item));
    }

    private static String resolvePermission(String... node){
        return String.join(".", node);
    }

    private static String formatId(@Nullable IForgeRegistryEntry<?> item) {
        if (item == null || item.getRegistryName() == null) return "minecraft.air" ;
        else return item.getRegistryName().toString().toLowerCase().replace(':', '.');
    }
}
