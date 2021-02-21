package net.dirtcraft.ftbutilities.spongeintegration.command;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import net.dirtcraft.ftbutilities.spongeintegration.core.api.FlagTeamInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import javax.annotation.Nonnull;

public class ToggleSpawns implements CommandExecutor {
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String id = args.requireOne("teamid");
        boolean val = args.requireOne("value");
        ForgeTeam team = Universe.get().getTeam(id);
        if (!(team instanceof FlagTeamInfo)) return CommandResult.empty();
        else ((FlagTeamInfo) team).setBlockMobSpawns(val);
        return CommandResult.success();
    }
}
