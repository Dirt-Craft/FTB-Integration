package net.dirtcraft.ftbintegration.command;

import net.dirtcraft.ftbintegration.FtbIntegration;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import javax.annotation.Nonnull;

public class Reload implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        FtbIntegration.INSTANCE.loadConfig();
        return CommandResult.success();
    }
}
