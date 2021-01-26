package net.dirtcraft.ftbutilities.spongeintegration.data;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
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
    private ClaimedChunk lastInteractClaim;
    private boolean lastInteractResult;
    private int lastInteractTick;
    private GameProfile gameProfile;

    public static PlayerData from(User user){
        return PlayerDataManager.INSTANCE.getData(user);
    }

    public PlayerData(User user){
        gameProfile = (GameProfile) user.getProfile();
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

    private String formatId(@Nullable IForgeRegistryEntry item) {
        return item != null && item.getRegistryName() != null ? item.getRegistryName().toString().toLowerCase().replace(':', '.') : "minecraft.air";
    }

    public boolean hasBlockEditingPermission(Block block) {
        return PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.block.edit." + formatId(block), null);
    }

    public boolean hasBlockInteractionPermission(Block block) {
        return PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.block.interact." + formatId(block), null);
    }

    public boolean hasItemUsePermission(Item block) {
        return PermissionAPI.hasPermission(gameProfile, "ftbutilities.claims.item." + formatId(block), null);
    }

    public boolean hasAnimalAttackPermission() {
        return PermissionAPI.hasPermission(gameProfile, FTBUtilitiesPermissions.CLAIMS_ATTACK_ANIMALS, null);
    }

    public ForgePlayer getForgePlayer(){
        return ClaimedChunks.instance.universe.getPlayer(gameProfile);
    }
}
