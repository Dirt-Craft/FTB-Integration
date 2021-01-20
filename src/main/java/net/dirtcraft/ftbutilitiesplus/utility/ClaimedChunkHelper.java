package net.dirtcraft.ftbutilitiesplus.utility;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;

import javax.annotation.Nonnull;

public class ClaimedChunkHelper {

    public static boolean isActive() {
        return ClaimedChunks.isActive();
    }

    public static boolean isSameTeam(Location<org.spongepowered.api.world.World> a, Location<org.spongepowered.api.world.World> b){
        return getTeam(a) == getTeam(b);
    }

    public static ForgeTeam getTeam(Location<org.spongepowered.api.world.World> location){
        BlockPos pos = SpongeHelper.getBlockPos(location);
        World world = SpongeHelper.getWorld(location);
        ClaimedChunk chunk = ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
        return chunk == null? null : chunk.getTeam();
    }

    public static ClaimedChunk getChunk(@Nonnull User user, @Nonnull Location<org.spongepowered.api.world.World> location){
        GameProfile player = SpongeHelper.getGameProfile(user);
        BlockPos pos = SpongeHelper.getBlockPos(location);
        World world = SpongeHelper.getWorld(location);
        return ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
    }

    public static boolean blockBlockEditing(@Nonnull User user, @Nonnull Location<org.spongepowered.api.world.World> location){
        GameProfile player = SpongeHelper.getGameProfile(user);
        BlockPos pos = SpongeHelper.getBlockPos(location);
        World world = SpongeHelper.getWorld(location);

        return blockBlockEditing(player, pos, world);
    }

    public static boolean blockBlockInteractions(@Nonnull User user, @Nonnull Location<org.spongepowered.api.world.World> location){
        GameProfile player = SpongeHelper.getGameProfile(user);
        BlockPos pos = SpongeHelper.getBlockPos(location);
        World world = SpongeHelper.getWorld(location);

        return blockBlockInteractions(player, pos, world);
    }

    private static boolean blockBlockEditing(GameProfile player, BlockPos pos, World world) {
        if (!isActive() || world == null) return false;
        ClaimedChunk chunk = ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
        return chunk != null && !chunk.getTeam().hasStatus(ClaimedChunks.instance.universe.getPlayer(player), chunk.getData().getEditBlocksStatus());
    }

    private static boolean blockBlockInteractions(GameProfile player, BlockPos pos, World world) {
        if (!isActive() || world == null) return false;
        ClaimedChunk chunk = ClaimedChunks.instance.getChunk(new ChunkDimPos(pos, world.provider.getDimension()));
        return chunk != null && !chunk.getTeam().hasStatus(ClaimedChunks.instance.universe.getPlayer(player), chunk.getData().getInteractWithBlocksStatus());
    }
}
