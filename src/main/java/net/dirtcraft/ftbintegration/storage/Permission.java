package net.dirtcraft.ftbintegration.storage;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class Permission {
    private static final String BASE = "ftbintegration";
    public static final String CLAIMS_BASE = resolvePermission(BASE, "claims");
    private static final String FLAG_BASE =  resolvePermission(CLAIMS_BASE, "flags");
    private static final String BADGE_BASE = resolvePermission(BASE, "badges");
    private static final String CONFIG_BASE = resolvePermission(BASE, "config");
    private static final String RESTRICT_BASE = resolvePermission(CONFIG_BASE, "restrict");
    private static final String CHUNKS_BASE = resolvePermission(BASE, "chunks");
    private static final String CHUNKS_CLAIM_BASE = resolvePermission(CHUNKS_BASE, "claim");
    private static final String CHUNKS_LOADER_BASE = resolvePermission(CHUNKS_BASE, "loader");

    public static final String CHUNKS_BALANCE_BASE = resolvePermission(CHUNKS_BASE, "balance.self");
    public static final String CHUNKS_BALANCE_OTHERS = resolvePermission(CHUNKS_BASE, "balance.others");

    //Admin Nodes
    public static final String SEE_INFO = resolvePermission(BASE, "tool", "info");
    public static final String RELOAD_CONFIG = resolvePermission(BASE, "reload");
    public static final String BYPASS = resolvePermission(CLAIMS_BASE, "bypass");
    public static final String DEBUG = resolvePermission(CLAIMS_BASE, "debug");

    //Claim Nodes
    public static final String CLAIM_BASE = resolvePermission(CLAIMS_BASE, "claim");
    public static final String CLAIM_CHUNK = resolvePermission(CLAIM_BASE, "base");
    public static final String UNCLAIM_DIM = resolvePermission(CLAIM_BASE, "dimension");
    public static final String CLAIM_OTHER = resolvePermission(CLAIM_BASE, "others");
    public static final String FLAG_MOB_SPAWN = resolvePermission(FLAG_BASE, "mobspawn");
    public static final String FLAG_EJECT_SPAWN = resolvePermission(FLAG_BASE, "ejectspawn");
    public static final String FLAG_ENTRY = resolvePermission(FLAG_BASE, "entry");

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

    //Chunk Claim Bonus
    public static final String CHUNKS_CLAIM_ADD = resolvePermission(CHUNKS_CLAIM_BASE, "add");
    public static final String CHUNKS_CLAIM_SET = resolvePermission(CHUNKS_CLAIM_BASE, "set");
    public static final String CHUNKS_CLAIM_REMOVE = resolvePermission(CHUNKS_CLAIM_BASE, "remove");
    public static final String CHUNKS_CLAIM_OTHERS = resolvePermission(CHUNKS_CLAIM_BASE, "others");

    //Chunk Loader Bonus
    public static final String CHUNKS_LOADER_ADD = resolvePermission(CHUNKS_LOADER_BASE, "add");
    public static final String CHUNKS_LOADER_SET = resolvePermission(CHUNKS_LOADER_BASE, "set");
    public static final String CHUNKS_LOADER_REMOVE = resolvePermission(CHUNKS_LOADER_BASE, "remove");
    public static final String CHUNKS_LOADER_OTHERS = resolvePermission(CHUNKS_LOADER_BASE, "others");

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
     - ftbutilities.claims.bypass_limits (admin)

    Staff Badge:
      - ftbintegration.badges.staff

    Flags:
     - ftbintegration.claims.flags.mobspawn

ftbintegration.claims.claim.dimension

    Config:
     - ftbintegration.config.restrict.modify
     - ftbintegration.config.restrict.view

    Chunk Claim Bonus:
     - ftbintegration.chunks.claim.set
     - ftbintegration.chunks.claim.add
     - ftbintegration.chunks.claim.remove
     - ftbintegration.chunks.claim.others

    Chunk Loader Bonus:
     - ftbintegration.chunks.loader.set
     - ftbintegration.chunks.loader.add
     - ftbintegration.chunks.loader.remove
     - ftbintegration.chunks.loader.others

    Meta:
     - ftbintegration.claims.chunks.modify.group
     - ftbintegration.claims.chunks.max
     - ftbintegration.claims.loaders.max
     */


    public static String resolveClaimDimension(String world){
        return resolvePermission(CLAIM_BASE, world);
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
