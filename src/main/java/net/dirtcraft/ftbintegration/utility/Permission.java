package net.dirtcraft.ftbintegration.utility;

public class Permission {
    private static final String BASE = "ftbintegration";
    private static final String CLAIMS_BASE = resolvePermission(BASE, "claims");
    private static final String FLAG_BASE =  resolvePermission(CLAIMS_BASE, "flags");
    private static final String BADGE_BASE = resolvePermission(BASE, "badges");

    //Admin Nodes
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

    /* Permissions Quick-List
    Claiming In General:
     - ftbintegration.claims.claim.worldId
     - ftbintegration.claims.debug
     - ftbintegration.claims.bypass

    FTB:U Exposed:
     - ftbutilities.other_player.claims.unclaim
     - ftbutilities.other_player.claims.see_info

    Staff Badge:
      - ftbintegration.badges.staff

    Flags:
     - ftbintegration.claims.flags.mobspawn */

    private static String resolvePermission(String... node){
        return String.join(".", node);
    }

    public static String getClaimNode(String world){
        return resolvePermission(CLAIM_CHUNK, world);
    }
}
