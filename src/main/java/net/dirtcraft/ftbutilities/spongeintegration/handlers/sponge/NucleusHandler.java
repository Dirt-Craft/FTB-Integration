package net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge;

import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import io.github.nucleuspowered.nucleus.api.events.NucleusHomeEvent;
import net.dirtcraft.ftbutilities.spongeintegration.utility.SpongeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class NucleusHandler {
    @Listener
    public void onSetHome(NucleusHomeEvent.Create event) {
        Optional<Location<World>> optLoc = event.getLocation();
        if (!optLoc.isPresent() || !event.getUser().getPlayer().isPresent()) return;
        BlockPos blockPos = SpongeHelper.getBlockPos(optLoc.get());
        IBlockState blockState = SpongeHelper.getBlockState(optLoc.get());
        EntityPlayer player = (EntityPlayer) event.getUser().getPlayer().get();

        if (ClaimedChunks.blockBlockInteractions(player, blockPos, blockState)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onModifyHome(NucleusHomeEvent.Modify event) {
        Optional<Location<World>> optLoc = event.getLocation();
        if (!optLoc.isPresent() || !event.getUser().getPlayer().isPresent()) return;
        BlockPos blockPos = SpongeHelper.getBlockPos(optLoc.get());
        IBlockState blockState = SpongeHelper.getBlockState(optLoc.get());
        EntityPlayer player = (EntityPlayer) event.getUser().getPlayer().get();

        if (ClaimedChunks.blockBlockInteractions(player, blockPos, blockState)) {
            event.setCancelled(true);
        }
    }
}
