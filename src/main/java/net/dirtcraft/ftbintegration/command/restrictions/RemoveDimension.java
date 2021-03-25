package net.dirtcraft.ftbintegration.command.restrictions;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;

public class RemoveDimension implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        World specified = args.<World>getOne("world").orElse(src instanceof Locatable? ((Locatable) src).getWorld() : null);
        if (specified == null) throw new CommandException(Text.of("You must specify a world if you are not locatable!"));
        boolean success = FtbIntegration.INSTANCE.getConfig().delistDimension(specified);
        String template;
        if (success) template = "&aSuccessfully delisted &7\"&c%s&7\"";
        else template = "&cFailed to delist &7\"&c%s&7\" &8(Is it listed?)";
        src.sendMessages(SpongeHelper.formatText(template, specified.getName()));
        return CommandResult.success();
    }
}