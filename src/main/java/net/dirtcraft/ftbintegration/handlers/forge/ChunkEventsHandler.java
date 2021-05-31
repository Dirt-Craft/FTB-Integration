package net.dirtcraft.ftbintegration.handlers.forge;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.feed_the_beast.ftbutilities.events.chunks.ChunkModifiedEvent;
import com.mojang.authlib.GameProfile;
import net.dirtcraft.ftbintegration.core.api.FlagTeamInfo;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.data.PlayerDataManager;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Teleporter;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ChunkEventsHandler {

    @SubscribeEvent
    public void onClaimChunk(ChunkModifiedEvent.Claim claimChunkEvent){
        GameProfile profile = claimChunkEvent.getPlayer().getProfile();
        if (profile.getId() == null) return;
        PlayerData data = PlayerDataManager.getInstance().get(profile);
        if (data == null) return;

        int dim = claimChunkEvent.getChunkDimPos().dim;
        net.minecraft.world.World world = DimensionManager.getWorld(dim);
        if (!(world instanceof World)) return;

        if (!data.canClaimInDimension((World) world)) {
            claimChunkEvent.setCanceled(true);
            data.getUser().flatMap(User::getPlayer).ifPresent(player -> {
                player.sendMessage(SpongeHelper.formatText("&cYou are not allowed to claim in &6\"&7%s&6\"&c dimension.", ((World) world).getName()));
            });
        }
    }

    @SubscribeEvent
    public void onChunkEnter(EntityEvent.EnteringChunk event){
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        PlayerData data = PlayerData.get(player);
        if (data == null) return;
        ClaimedChunk chunk = ClaimedChunkHelper.getChunk(player.getLocation());
        if (chunk == null || !(chunk.getTeam() instanceof FlagTeamInfo)) data.setClaimStandingIn(chunk == null? null: chunk.getTeam());
        else {
            FlagTeamInfo extTeam = (FlagTeamInfo) chunk.getTeam();
            EntityPlayerMP p = (EntityPlayerMP) player;
            if (extTeam.allowEntry(data.getForgePlayer()) || data.canBypassClaims()) data.setClaimStandingIn(chunk.getTeam());
            else if (extTeam.ejectEntrantSpawn() || !canEnterChunk(event.getOldChunkX(), event.getOldChunkZ(), data.getForgePlayer(), p.dimension)){
                player.setLocation(player.getWorld().getSpawnLocation());
            } else {
                int y = (int) p.posY;
                int x = (event.getOldChunkX() << 4) + 8;
                int z = (event.getOldChunkZ() << 4) + 8;
                player.setLocation(player.getWorld().getLocation(x, y, z));
            }
        }
    }

    private boolean canEnterChunk(int cx, int cz, ForgePlayer player, int world){
        ClaimedChunk chunks = ClaimedChunks.instance.getChunk(new ChunkDimPos(cx, cz, world));
        return chunks == null || chunks.getTeam() instanceof FlagTeamInfo
                && ((FlagTeamInfo) chunks.getTeam()).allowEntry(player);
    }
}
