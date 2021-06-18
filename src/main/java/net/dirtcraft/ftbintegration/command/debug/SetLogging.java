package net.dirtcraft.ftbintegration.command.debug;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class SetLogging implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        boolean logging = !FtbIntegration.INSTANCE.getConfig().shouldLog();
        FtbIntegration.INSTANCE.getConfig().setLogging(logging);
        src.sendMessage(SpongeHelper.formatText("set logging to %s", String.valueOf(logging)));
        return CommandResult.success();
    }
}
