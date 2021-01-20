package net.dirtcraft.ftbutilitiesplus.utility;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.mojang.authlib.GameProfile;
import net.dirtcraft.ftbutilitiesplus.data.PlayerData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.server.permission.PermissionAPI;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClaimedChunkHelper {

    public static boolean isActive() {
        return ClaimedChunks.isActive();
    }

    public static boolean isSameTeam(Location<org.spongepowered.api.world.World> a, Location<org.spongepowered.api.world.World> b){
        return getTeam(a) == getTeam(b);
    }

    public static boolean isSameTeam(ClaimedChunk a, ClaimedChunk b){
        return a != null && b != null && a.getTeam() == b.getTeam();
    }

    public static ForgeTeam getTeam(Location<org.spongepowered.api.world.World> location){
        if (location == null) return null;
        BlockPos pos = SpongeHelper.getBlockPos(location);
        World world = SpongeHelper.getWorld(location);
        if (!isActive() || world == null) return null;
        ClaimedChunk chunk = ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
        return chunk == null? null : chunk.getTeam();
    }

    public static ClaimedChunk getChunk(Location<org.spongepowered.api.world.World> location){
        if (location == null) return null;
        BlockPos pos = SpongeHelper.getBlockPos(location);
        World world = SpongeHelper.getWorld(location);
        if (!isActive() || world == null) return null;
        return ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
    }

    public static boolean blockBlockEditing(PlayerData player, Location<org.spongepowered.api.world.World> location) {
        ClaimedChunk chunk = getChunk(location);
        IBlockState state = SpongeHelper.getBlockState(location);
        return player != null && chunk != null
                && !player.hasBlockEditingPermission(state.getBlock())
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getEditBlocksStatus());
    }

    public static boolean blockBlockInteractions(PlayerData player, Location<org.spongepowered.api.world.World> location) {
        ClaimedChunk chunk = getChunk(location);
        IBlockState state = SpongeHelper.getBlockState(location);
        return player != null && chunk != null
                && !player.hasBlockInteractionPermission(state.getBlock())
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getInteractWithBlocksStatus());
    }

    public static boolean blockItemUse(PlayerData player, Location<org.spongepowered.api.world.World> location) {
        ClaimedChunk chunk = getChunk(location);
        IBlockState state = SpongeHelper.getBlockState(location);
        return player != null && chunk != null
                && !player.hasBlockInteractionPermission(state.getBlock())
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getUseItemsStatus());
    }

    public static ClaimedChunk getChunk(Cause cause) {
        BlockSnapshot blockSource = cause.first(BlockSnapshot.class).orElse(null);
        LocatableBlock locatableBlock = null;
        TileEntity tileEntitySource = null;
        Entity entitySource = null;
        if (blockSource == null) {
            locatableBlock = cause.first(LocatableBlock.class).orElse(null);
            if (locatableBlock == null) {
                entitySource = cause.first(Entity.class).orElse(null);
            }
            if (locatableBlock == null && entitySource == null) {
                tileEntitySource = cause.first(TileEntity.class).orElse(null);
            }
        }

        ClaimedChunk sourceClaim = null;
        if (blockSource != null) {
            getChunk(blockSource.getLocation().get());
        } else if (locatableBlock != null) {
            sourceClaim = getChunk(locatableBlock.getLocation());
        } else if (tileEntitySource != null) {
            sourceClaim = getChunk(tileEntitySource.getLocation());
        } else if (entitySource != null) {
            Entity entity = entitySource;
            if (entity instanceof Player) {
                Player player = (Player) entity;
                sourceClaim = getChunk(player.getLocation());
            } else {
                sourceClaim = getChunk(entity.getLocation());
            }
        }

        return sourceClaim;
    }


}
