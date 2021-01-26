package net.dirtcraft.ftbutilities.spongeintegration.utility;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ClaimedChunkHelper {

    public static boolean isActive() {
        return ClaimedChunks.isActive();
    }

    public static boolean isSameTeam(ClaimedChunk a, ClaimedChunk b){
        return a == b || a != null && b != null && a.getTeam() == b.getTeam();
    }

    public static boolean blockBlockEditing(PlayerData player, ClaimedChunk chunk, Location<World> location) {
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getEditBlocksStatus())
                && !player.hasBlockEditingPermission(SpongeHelper.getBlockState(location).getBlock());
    }

    public static boolean blockBlockEditing(PlayerData player, Location<World> location) {
        ClaimedChunk chunk = getChunk(location);
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getEditBlocksStatus())
                && !player.hasBlockEditingPermission(SpongeHelper.getBlockState(location).getBlock());
    }

    public static boolean blockBlockInteractions(PlayerData player, ClaimedChunk chunk, Location<World> location) {
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getInteractWithBlocksStatus())
                && !player.hasBlockInteractionPermission(SpongeHelper.getBlockState(location).getBlock());
    }

    public static boolean blockBlockInteractions(PlayerData player, Location<World> location) {
        ClaimedChunk chunk = getChunk(location);
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getInteractWithBlocksStatus())
                && !player.hasBlockInteractionPermission(SpongeHelper.getBlockState(location).getBlock());
    }

    public static boolean blockItemUse(PlayerData player, ClaimedChunk chunk, Location<World> location) {
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getUseItemsStatus())
                && !player.hasBlockInteractionPermission(SpongeHelper.getBlockState(location).getBlock());
    }

    public static boolean blockItemUse(PlayerData player, Location<World> location) {
        ClaimedChunk chunk = getChunk(location);
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getUseItemsStatus())
                && !player.hasBlockInteractionPermission(SpongeHelper.getBlockState(location).getBlock());
    }

    public static ForgeTeam getTeam(Location<World> location){
        if (location == null) return null;
        BlockPos pos = SpongeHelper.getBlockPos(location);
        net.minecraft.world.World world = SpongeHelper.getWorld(location);
        if (!isActive() || world == null) return null;
        ClaimedChunk chunk = ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
        return chunk == null? null : chunk.getTeam();
    }

    public static ClaimedChunk getChunk(Location<World> location){
        if (location == null) return null;
        BlockPos pos = SpongeHelper.getBlockPos(location);
        net.minecraft.world.World world = SpongeHelper.getWorld(location);
        if (!isActive() || world == null) return null;
        return ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
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
