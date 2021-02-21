package net.dirtcraft.ftbutilities.spongeintegration.command;

import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerData;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class IgnoreClaim implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may execute this command!"));
        Player player = (Player) src;
        PlayerData data = PlayerData.get(player);
        boolean bypass = data.toggleBypassClaims();
        if (bypass){
            String response = "&7You can now &l&aBypass &7claims.";
            Text message = TextSerializers.FORMATTING_CODE.deserialize(response);
            player.sendMessage(message);
        } else {
            String response = "&7You can no longer &l&cBypass &7claims.";
            Text message = TextSerializers.FORMATTING_CODE.deserialize(response);
            player.sendMessage(message);

        }
        return CommandResult.success();
    }
}
