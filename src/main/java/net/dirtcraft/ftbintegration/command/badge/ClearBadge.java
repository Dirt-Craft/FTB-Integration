package net.dirtcraft.ftbintegration.command.badge;

import net.dirtcraft.ftbintegration.data.sponge.PlayerSettings;
import net.dirtcraft.ftbintegration.utility.Permission;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ClearBadge implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        Player target;
        if (args.hasAny("target")) target = args.<Player>getOne("target")
                .filter(t -> src.hasPermission(Permission.BADGE_OTHERS))
                .orElseThrow(() -> new CommandException(Text.of("You do not have permission to change someone elses badge!")));
        else target = Optional.of(src)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .orElseThrow(() -> new CommandException(Text.of("You must be a player to set your own badge!")));
        target.offer(PlayerSettings.GET_BADGE, "");
        return CommandResult.success();
    }
}
