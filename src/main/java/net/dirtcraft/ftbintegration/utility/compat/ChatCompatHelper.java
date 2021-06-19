package net.dirtcraft.ftbintegration.utility.compat;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.core.api.ChatTeam;
import net.dirtcraft.ftbintegration.handlers.compat.UltimateChatHandler;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;

public interface ChatCompatHelper {
    ChatCompatHelper INSTANCE = INTERNAL.getInstance();

    default void registerListeners(FtbIntegration plugin){}

    default boolean addCommand(){
        return true;
    }

    default boolean toggleTeamChannel(Player player, ForgeTeam team) {
        if (player.getMessageChannel() instanceof ChatTeam.TeamChatChannel) {
            player.setMessageChannel(MessageChannel.TO_ALL);
            player.sendMessage(SpongeHelper.getText("&cTeam chat disabled!"));
            return false;
        } else {
            ChatTeam.TeamChatChannel channel = ((ChatTeam) team).getChannel();
            player.setMessageChannel(channel);
            player.sendMessage(SpongeHelper.getText("&aTeam chat enabled!"));
            return true;
        }
    }

    class UltimateChat implements ChatCompatHelper {
        private final UChat uChat = UChat.get();
        private final uChatAPI api = uChat.getAPI();
        private final String channelName = "teamchat";

        @Override
        public void registerListeners(FtbIntegration plugin) {
            UCChannel channel = new UCChannel(channelName, "tc", "");
            Sponge.getEventManager().registerListeners(plugin, new UltimateChatHandler(channelName));
            try {
                api.registerNewChannel(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean addCommand(){
            return false;
        }

        @Override
        public boolean toggleTeamChannel(Player player, ForgeTeam team) {
            return false;
        }
    }

    class INTERNAL {
        private static ChatCompatHelper getInstance(){
            try {
                Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.API.PlayerChangeChannelEvent");
                return new UltimateChat();
            } catch (Exception e) {
                return new ChatCompatHelper(){};
            }
        }
    }
}
