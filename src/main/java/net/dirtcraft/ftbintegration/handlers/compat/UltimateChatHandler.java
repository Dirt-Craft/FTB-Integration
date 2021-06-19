package net.dirtcraft.ftbintegration.handlers.compat;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import net.dirtcraft.ftbintegration.core.api.ChatTeam;
import net.dirtcraft.ftbintegration.data.PlayerData;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

public class UltimateChatHandler {
    private final String channel;

    public UltimateChatHandler(String channel){
        this.channel = channel;
    }

    @Listener
    public void onChat(SendChannelMessageEvent event) {
        if (!event.getChannel().matchChannel(channel)) return;
        CommandSource p = event.getSender();
        PlayerData data;
        if (!(p instanceof Player) || (data = PlayerData.get((Player)p)) == null || !(data.getForgePlayer().team instanceof ChatTeam)) return;
        ChatTeam team = (ChatTeam) data.getForgePlayer().team;
        team.getChannel().send(p, event.getMessage());
        event.setCancelled(true);
    }
}
