package net.dirtcraft.ftbutilities.spongeintegration.data;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.server.permission.PermissionAPI;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.SpongeImpl;

import javax.annotation.Nullable;

public class PlayerData {
    private User user;
    private ForgePlayer fPlayer;
    private GameProfile gameProfile;
    private ForgeTeam chunkStandingIn;
    private ClaimedChunk lastInteractClaim;
    private boolean lastInteractResult;
    private int lastInteractTick;

    public static PlayerData getOrCreate(User user){
        return PlayerDataManager.INSTANCE.getOrCreate(user);
    }

    public static PlayerData get(User user){
        return PlayerDataManager.INSTANCE.get(user);
    }

    public PlayerData(User user){
        this.user = user;
        this.gameProfile = (GameProfile) user.getProfile();
    }

    public void setLastInteractData(ClaimedChunk claim) {
        this.lastInteractResult = true;
        this.lastInteractClaim = claim;
        this.lastInteractTick = SpongeImpl.getServer().getTickCounter();
    }

    public boolean checkLastInteraction(ClaimedChunk claim, User user) {
        if (this.lastInteractResult && user != null && ((SpongeImpl.getServer().getTickCounter() - this.lastInteractTick) <= 2)) {
            return claim == null || lastInteractClaim != null && claim.getTeam().equals(this.lastInteractClaim.getTeam());
        }
        return false;
    }

    public String getClaimStandingIn(){
        if (chunkStandingIn == null) return "wilderness";
        else if (chunkStandingIn.owner == null) return "server";
        else return chunkStandingIn.getId();
    }

    public void setClaimStandingIn(ForgeTeam team){
        chunkStandingIn = team;
    }

    public boolean hasBlockEditingPermission(Block block, ClaimedChunk chunk) {
        return PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.block.edit." + formatId(block) + "." + formatClaim(chunk), null);
    }

    public boolean hasBlockInteractionPermission(Block block, ClaimedChunk chunk) {
        return PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.block.interact." + formatId(block) + "." + formatClaim(chunk), null);
    }

    public boolean hasItemUsePermission(Item block, ClaimedChunk chunk) {
        return PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.item." + formatId(block) + "." + formatClaim(chunk), null);
    }

    public boolean hasAnimalAttackPermission(ClaimedChunk chunk) {
        return PermissionAPI.hasPermission(gameProfile, FTBUtilitiesPermissions.CLAIMS_ATTACK_ANIMALS + "." + formatClaim(chunk), null);
    }

    public ForgePlayer getForgePlayer(){
        if (fPlayer == null) fPlayer = ClaimedChunks.instance.universe.getPlayer(gameProfile);
        return fPlayer;
    }

    public ForgeTeam getForgeTeam(){
        return getForgePlayer().team;
    }

    public User getUser(){
        return user;
    }

    public GameProfile getGameProfile(){
        return gameProfile;
    }

    public boolean isOnline(){
        return user.isOnline();
    }

    private String formatId(@Nullable IForgeRegistryEntry item) {
        return item != null && item.getRegistryName() != null ? item.getRegistryName().toString().toLowerCase().replace(':', '.') : "minecraft.air";
    }

    private String formatClaim(ClaimedChunk chunk){
        if (chunk == null) return "wilderness";
        else if (chunk.getTeam() == null) return "server";
        else return chunk.getTeam().getId();
    }
}
