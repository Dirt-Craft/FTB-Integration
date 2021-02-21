package net.dirtcraft.ftbutilities.spongeintegration.data;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.mojang.authlib.GameProfile;
import net.dirtcraft.ftbutilities.spongeintegration.core.mixins.badges.FTBUtilitiesUniverseDataAccessor;
import net.dirtcraft.ftbutilities.spongeintegration.data.sponge.PlayerSettings;
import net.dirtcraft.ftbutilities.spongeintegration.utility.Permission;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.server.permission.PermissionAPI;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.SpongeImpl;

import javax.annotation.Nullable;

public class PlayerData {
    private final User user;
    private final GameProfile gameProfile;
    private ForgePlayer fPlayer;
    private ForgeTeam claimStandingIn;
    private ClaimedChunk lastInteractClaim;
    private boolean lastInteractResult;
    private boolean bypassClaims;
    private boolean debugClaims;
    private int lastInteractTick;

    public static PlayerData getOrCreate(User user){
        return PlayerDataManager.INSTANCE.getOrCreate(user);
    }

    public static PlayerData get(User user){
        return PlayerDataManager.INSTANCE.get(user);
    }

    public PlayerData(User user){
        this.gameProfile = (GameProfile) user.getProfile();
        this.user = user;
        this.bypassClaims = user.get(PlayerSettings.CAN_BYPASS).orElse(false);
        this.debugClaims = user.get(PlayerSettings.IS_DEBUG).orElse(false);
        if (getBadge() != null) FTBUtilitiesUniverseDataAccessor.getBADGE_CACHE().put(user.getUniqueId(), getBadge());
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
        return formatTeam(claimStandingIn);
    }

    public void setClaimStandingIn(ForgeTeam team){
        claimStandingIn = team;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasBlockEditingPermission(Block block, ClaimedChunk chunk) {
        return canBypassClaims() || PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.block.edit." + formatId(block) + "." + formatClaim(chunk), null);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasBlockInteractionPermission(Block block, ClaimedChunk chunk) {
        return canBypassClaims() || PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.block.interact." + formatId(block) + "." + formatClaim(chunk), null);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasItemUsePermission(Item block, ClaimedChunk chunk) {
        return canBypassClaims() || PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.item." + formatId(block) + "." + formatClaim(chunk), null);
    }

    public boolean hasAnimalAttackPermission(ClaimedChunk chunk) {
        return canBypassClaims() || PermissionAPI.hasPermission(gameProfile, FTBUtilitiesPermissions.CLAIMS_ATTACK_ANIMALS + "." + formatClaim(chunk), null);
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

    public boolean toggleBypassClaims(){
        bypassClaims = !bypassClaims;
        user.offer(PlayerSettings.CAN_BYPASS, bypassClaims);
        return bypassClaims;
    }

    public boolean canBypassClaims(){
        return bypassClaims;
    }

    public boolean toggleDebugClaims(){
        debugClaims = !debugClaims;
        user.offer(PlayerSettings.IS_DEBUG, debugClaims);
        return debugClaims;
    }

    public boolean canDebugClaims(){
        return debugClaims;
    }

    public @Nullable String getBadge(){
        if (!user.hasPermission(Permission.STAFF_BADGE)) return null;
        return "https://i.imgur.com/G0pEx1j.png";
    }

    private String formatId(@Nullable IForgeRegistryEntry<?> item) {
        return item != null && item.getRegistryName() != null ? item.getRegistryName().toString().toLowerCase().replace(':', '.') : "minecraft.air";
    }

    private String formatClaim(ClaimedChunk chunk){
        return formatTeam(chunk == null? null: chunk.getTeam());
    }

    private String formatTeam(ForgeTeam team){
        if (team == null) return "wilderness";
        else if (team.owner == null) return "server";
        else return team.getId();
    }
}
