package net.dirtcraft.ftbintegration.command;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import net.dirtcraft.ftbintegration.core.api.ChatTeam;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

public class TeamChat implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may execute this command!"));
        Player player = (Player)src;
        PlayerData data = PlayerData.get(player);
        if (data == null) throw new CommandException(Text.of("Could not retrieve player data!"));
        ForgeTeam team = data.getForgePlayer().team;
        if (!(team instanceof ChatTeam)) throw new CommandException(Text.of("Could not retrieve player team!"));
        String message = args.<String>getOne("message").orElse(null);

        if (message != null) {
            ChatTeam.TeamChatChannel channel = ((ChatTeam)team).getChannel();
            channel.send(src, SpongeHelper.getText(message));
        } else if (player.getMessageChannel() instanceof ChatTeam.TeamChatChannel) {
            player.setMessageChannel(MessageChannel.TO_ALL);
            player.sendMessage(SpongeHelper.getText("&cTeam chat disabled!"));
        } else {
            ChatTeam.TeamChatChannel channel = ((ChatTeam)team).getChannel();
            player.setMessageChannel(channel);
            player.sendMessage(SpongeHelper.getText("&aTeam chat enabled!"));
        }
        return CommandResult.success();
    }
}
