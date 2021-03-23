package net.dirtcraft.ftbintegration.command;

import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;

public class IgnoreClaim implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may execute this command!"));
        Player player = args.<Player>getOne("target").orElse((Player) src);
        PlayerData data = PlayerData.get(player);
        boolean bypass = data.toggleBypassClaims();
        String name = src == player? "You": player.getName();
        Text message;
        if (bypass) message = SpongeHelper.formatText("&7%s can now &l&aBypass &7claims.", name);
        else message = SpongeHelper.formatText("&7%s can no longer &l&cBypass &7claims.", name);
        src.sendMessage(message);
        return CommandResult.success();
    }
}
