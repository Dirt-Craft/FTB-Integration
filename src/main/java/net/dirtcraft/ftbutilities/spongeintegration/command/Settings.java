package net.dirtcraft.ftbutilities.spongeintegration.command;

import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerData;
import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerDataManager;
import net.dirtcraft.ftbutilities.spongeintegration.data.sponge.PlayerSettings;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Settings implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("This command may only be executed by player sources."));
        Player p = (Player) src;

        PlayerData data = PlayerDataManager.getInstance().get(p);
        boolean dataBypass = p.get(PlayerSettings.CAN_BYPASS).orElse(false);
        boolean dataDebug = p.get(PlayerSettings.IS_DEBUG).orElse(false);
        String message = "Player Settings:" +
                "\n&7Bypass: " + (data.canBypassClaims()? "&a" : "&c") + dataBypass +
                "\n&7Debug: " + (data.canDebugClaims()? "&a" : "&c") + dataDebug;

        p.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));


        return CommandResult.success();
    }
}
