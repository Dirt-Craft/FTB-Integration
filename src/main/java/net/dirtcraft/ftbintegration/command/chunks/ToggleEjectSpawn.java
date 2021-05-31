package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import net.dirtcraft.ftbintegration.core.api.FlagTeamInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import javax.annotation.Nonnull;

public class ToggleEjectSpawn implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String id = args.requireOne("team-id");
        boolean val = args.requireOne("value");
        ForgeTeam team = Universe.get().getTeam(id);
        if (!(team instanceof FlagTeamInfo)) return CommandResult.empty();
        else ((FlagTeamInfo) team).setEjectEntrantSpawn(val);
        return CommandResult.success();
    }
}
