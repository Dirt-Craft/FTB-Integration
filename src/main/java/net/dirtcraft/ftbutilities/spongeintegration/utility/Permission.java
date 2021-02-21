package net.dirtcraft.ftbutilities.spongeintegration.utility;

public class Permission {
    private static final String BASE = "ftbintegration";
    private static final String CLAIMS_BASE = resolvePermission(BASE, "claims");
    private static final String FLAG_BASE =  resolvePermission(CLAIMS_BASE, "flags");

    //Admin Nodes
    public static final String BYPASS = resolvePermission(CLAIMS_BASE, "bypass");
    public static final String DEBUG = resolvePermission(CLAIMS_BASE, "debug");

    //Claim Nodes
    public static final String CLAIM_CHUNK = resolvePermission(CLAIMS_BASE, "claim");
    public static final String FLAG_MOB_SPAWN = resolvePermission(FLAG_BASE, "mobspawn");


    private static String resolvePermission(String... node){
        return String.join(".", node);
    }

    public static String getClaimNode(String world){
        return resolvePermission(CLAIM_CHUNK, world);
    }
}
