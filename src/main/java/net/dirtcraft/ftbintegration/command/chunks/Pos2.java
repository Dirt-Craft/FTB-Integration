package net.dirtcraft.ftbintegration.command.chunks;

import net.dirtcraft.ftbintegration.data.PlayerData;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.Optional;

public class Pos2 implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        PlayerData data = Optional.of(src)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .map(PlayerData::get)
                .orElseThrow(()->new CommandException(Text.of("You must be a player to use this command!")));
        Player p = (Player) src;
        data.setSecondaryChunkPos(p.getLocation(), p);
        return CommandResult.success();
    }
}
