package net.dirtcraft.ftbintegration.command;

import net.dirtcraft.ftbintegration.data.PlayerData;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;

public class DebugClaim implements CommandExecutor {
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may execute this command!"));
        Player player = (Player) src;
        PlayerData data = PlayerData.get(player);
        boolean bypass = data.toggleDebugClaims();
        if (bypass){
            String response = "&7You can now see &l&aDebug &7claim info.";
            Text message = TextSerializers.FORMATTING_CODE.deserialize(response);
            player.sendMessage(message);
        } else {
            String response = "&7You can no longer see &l&cDebug &7claim info.";
            Text message = TextSerializers.FORMATTING_CODE.deserialize(response);
            player.sendMessage(message);

        }
        return CommandResult.success();
    }
}