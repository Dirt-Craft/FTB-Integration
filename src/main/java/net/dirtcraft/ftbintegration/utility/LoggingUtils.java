package net.dirtcraft.ftbintegration.utility;

import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.core.mixins.generic.AccessorFinalIDObject;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LoggingUtils {
    private static Logger LOGGER = FtbIntegration.LOG;

    public static void logFailure(Cause cause, Location<World> location, String id) {
        if (!FtbIntegration.INSTANCE.getConfig().shouldLog()) return;
        LOGGER.info(String.format("Block event failed @ %s, team: %s via %s [%s]", getLocation(location), getTeam(ClaimedChunkHelper.getChunk(location)), getCause(cause), id));
    }

    public static void logFailure(Cause cause, Location<World> location, ClaimedChunk chunk, String id) {
        if (!FtbIntegration.INSTANCE.getConfig().shouldLog()) return;
        LOGGER.info(String.format("Block event failed @ %s, team: %s via %s [%s]", getLocation(location), getTeam(chunk), getCause(cause), id));
    }

    private static String getLocation(Location<World> loc) {
        return String.format("%s{%d, %d, %d}", loc.getExtent().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private static String getTeam(ClaimedChunk chunk) {
        if (chunk == null) return "wilderness";
        else return ((AccessorFinalIDObject)chunk.getTeam()).getTeamIdString();
    }

    private static String getCause(Cause cause) {
        Object c = cause.root();
        if (c instanceof TileEntity) {
            TileEntity source = (TileEntity) c;
            return String.format("TE{%d. %d, %d}", source.getPos().getX(), source.getPos().getY(), source.getPos().getZ());
        } else if (c instanceof User) {
            User source = (User) c;
            return String.format("%s{%f. %f, %f}", source.getName(), source.getPosition().getX(), source.getPosition().getY(), source.getPosition().getZ());
        } else return String.format("%s{%s}", c.getClass(), c);
    }
}
