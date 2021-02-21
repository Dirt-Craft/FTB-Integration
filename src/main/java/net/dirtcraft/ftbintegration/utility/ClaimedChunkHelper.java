package net.dirtcraft.ftbintegration.utility;

import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.FTBUtilitiesConfig;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesPlayerData;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesUniverseData;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.item.ItemType;
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

    private static boolean canPvP(EntityPlayer player, EntityPlayer target){
        if (FTBUtilitiesConfig.world.safe_spawn && player.world.provider.getDimension() == 0 && FTBUtilitiesUniverseData.isInSpawn(ClaimedChunks.instance.universe.server, new ChunkDimPos(target))) {
            return false;
        } else if (FTBUtilitiesConfig.world.enable_pvp.isDefault()) {
            return FTBUtilitiesPlayerData.get(ClaimedChunks.instance.universe.getPlayer(player)).enablePVP() && FTBUtilitiesPlayerData.get(ClaimedChunks.instance.universe.getPlayer(target)).enablePVP();
        }
        return FTBUtilitiesConfig.world.enable_pvp.isTrue();
    }

    public static boolean canAttackEntity(PlayerData owner, DamageSource aggressor, Entity target) {
        if (!isActive()) return true;
        else if (target instanceof EntityPlayer && aggressor instanceof EntityPlayer) return canPvP((EntityPlayer) aggressor, (EntityPlayer) target);
        else if (!(target instanceof IMob)) {
            ClaimedChunk chunk = getChunk(target.getLocation());
            return owner == null || chunk == null
                    || chunk.getTeam().hasStatus(owner.getForgePlayer(), chunk.getData().getAttackEntitiesStatus())
                    || owner.hasAnimalAttackPermission(chunk);
        }

        return true;
    }

    public static boolean blockBlockEditing(PlayerData player, ClaimedChunk chunk, Location<World> location) {
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getEditBlocksStatus())
                && !player.hasBlockEditingPermission(SpongeHelper.getBlockState(location).getBlock(), chunk);
    }

    public static boolean blockBlockEditing(PlayerData player, Location<World> location) {
        ClaimedChunk chunk = getChunk(location);
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getEditBlocksStatus())
                && !player.hasBlockEditingPermission(SpongeHelper.getBlockState(location).getBlock(), chunk);
    }

    public static boolean blockBlockInteractions(PlayerData player, ClaimedChunk chunk, Location<World> location) {
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getInteractWithBlocksStatus())
                && !player.hasBlockInteractionPermission(SpongeHelper.getBlockState(location).getBlock(), chunk);
    }

    public static boolean blockBlockInteractions(PlayerData player, Location<World> location) {
        ClaimedChunk chunk = getChunk(location);
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getInteractWithBlocksStatus())
                && !player.hasBlockInteractionPermission(SpongeHelper.getBlockState(location).getBlock(), chunk);
    }

    public static boolean blockItemUse(PlayerData player, ClaimedChunk chunk, ItemType item) {

        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getUseItemsStatus())
                && !player.hasItemUsePermission((Item) item, chunk);
    }

    public static boolean blockItemUse(PlayerData player, Location<World> location, ItemType item) {
        ClaimedChunk chunk = getChunk(location);
        return player != null && chunk != null
                && !chunk.getTeam().hasStatus(player.getForgePlayer(), chunk.getData().getUseItemsStatus())
                && !player.hasItemUsePermission((Item) item, chunk);
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
