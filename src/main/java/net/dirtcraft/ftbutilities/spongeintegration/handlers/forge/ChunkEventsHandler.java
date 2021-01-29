package net.dirtcraft.ftbutilities.spongeintegration.handlers.forge;

import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.events.chunks.ChunkModifiedEvent;
import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerData;
import net.dirtcraft.ftbutilities.spongeintegration.utility.ClaimedChunkHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

public class ChunkEventsHandler {

    @SubscribeEvent
    public void onClaimChunk(ChunkModifiedEvent.Claim claimChunkEvent){
        EntityPlayerMP forgePlayer = claimChunkEvent.getPlayer().getNullablePlayer();
        if (forgePlayer == null) return;
        Player player = (Player) forgePlayer;
        int dim = claimChunkEvent.getChunkDimPos().dim;
        net.minecraft.world.World[] worlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
        net.minecraft.world.World dimension = worlds.length < dim? null : worlds[dim];
        World world = dimension == null? null: (World) dimension;
        if (world == null) return;

        String permission = String.format("ftbutilities.claims.claim.%s", world.getName());
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
