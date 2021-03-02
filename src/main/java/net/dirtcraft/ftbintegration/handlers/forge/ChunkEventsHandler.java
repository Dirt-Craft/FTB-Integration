package net.dirtcraft.ftbintegration.handlers.forge;

import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.events.chunks.ChunkModifiedEvent;
import com.mojang.authlib.GameProfile;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.data.PlayerDataManager;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.World;

public class ChunkEventsHandler {

    @SubscribeEvent
    public void onClaimChunk(ChunkModifiedEvent.Claim claimChunkEvent){
        GameProfile profile = claimChunkEvent.getPlayer().getProfile();
        if (profile.getId() == null) return;
        PlayerData data = PlayerDataManager.getInstance().get(profile);
        if (data == null) return;
        Player player = data.getUser()
                .flatMap(User::getPlayer)
                .orElse(null);
        if (player == null) return;

        int dim = claimChunkEvent.getChunkDimPos().dim;
        net.minecraft.world.World[] worlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
        net.minecraft.world.World dimension = worlds.length < dim? null : worlds[dim];
        World world = dimension == null? null: (World) dimension;
        if (world == null) return;

        String permission = Permission.resolveClaimDimension(world.getName());
        if (!player.hasPermission(permission)) claimChunkEvent.setCanceled(true);
    }

    @SubscribeEvent
    public void onChunkEnter(EntityEvent.EnteringChunk enteringChunkEvent){
        if (!(enteringChunkEvent.getEntity() instanceof Player)) return;
        Player player = (Player) enteringChunkEvent.getEntity();
        PlayerData data = PlayerData.get(player);
        if (data == null) return;
        ClaimedChunk chunk = ClaimedChunkHelper.getChunk(player.getLocation());
        data.setClaimStandingIn(chunk == null? null: chunk.getTeam());
    }
}
