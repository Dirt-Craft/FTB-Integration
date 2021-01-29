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

public class Base implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command!"));
        PlayerData data = PlayerData.get((Player) src);
        boolean bypass = data.toggleBypassClaims();
        Text message;
        if (bypass){
            String content = "&aYou can now bypass claim protections!";
            message = TextSerializers.FORMATTING_CODE.deserialize(content);
        } else {
            String content = "&cYou can no longer bypass claim protections.";
            message = TextSerializers.FORMATTING_CODE.deserialize(content);
        }
        ((Player)src).sendMessage(message);
        return CommandResult.success();
    }
}
