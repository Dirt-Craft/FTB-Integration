package net.dirtcraft.ftbintegration.core.mixins.chat;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import net.dirtcraft.ftbintegration.core.api.ChatTeam;
import net.dirtcraft.ftbintegration.data.PlayerData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(value = ForgeTeam.class, remap = false)
public class MixinForgeTeam implements ChatTeam {
    private final TeamChatChannel teamChatChannel = new TeamChatChannel(this);

    @Override
    public TeamChatChannel getChannel() {
        return teamChatChannel;
    }

    @Override
    public EnumTeamStatus getRank(MessageReceiver receiver)  {
        if (!(receiver instanceof Player)) return EnumTeamStatus.NONE;
        PlayerData data = PlayerData.get((Player)receiver);
        return data == null? EnumTeamStatus.NONE : data.getForgePlayer().team.getHighestStatus(data.getForgePlayer());
    }

    @Override
    public boolean isMember(MessageReceiver receiver) {
        if (!(receiver instanceof Player)) return false;
        PlayerData data = PlayerData.get((Player)receiver);
        return data != null && data.getForgePlayer().team == (ForgeTeam)(Object)this;
    }
}
