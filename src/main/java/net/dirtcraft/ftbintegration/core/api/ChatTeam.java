package net.dirtcraft.ftbintegration.core.api;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.NucleusHelper;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public interface ChatTeam {
    TeamChatChannel getChannel();
    boolean isMember(MessageReceiver receiver);
    EnumTeamStatus getRank(MessageReceiver receiver);
    default EnumTeamStatus getRank(Object object){
        return object instanceof MessageReceiver? getRank((MessageReceiver) object) : EnumTeamStatus.NONE;
    }

    class TeamChatChannel implements MessageChannel {
        public static NucleusHelper nucleusHelper = NucleusHelper.get();
        private final ChatTeam team;

        public TeamChatChannel(ChatTeam team) {
            this.team = team;
        }

        @Override
        public Collection<MessageReceiver> getMembers() {
            Collection<MessageReceiver> receivers = Sponge.getServer().getOnlinePlayers().stream()
                    .filter(p->team.isMember(p) || canSpy(p))
                    .collect(Collectors.toList());
            receivers.add(Sponge.getServer().getConsole());
            return receivers;
        }

        @Override
        public void send(@Nullable Object sender, Text original, ChatType type) {
            checkNotNull(original, "original text");
            checkNotNull(type, "type");
            for (MessageReceiver member : this.getMembers()) {
                String rank = getName(team.getRank(sender));
                Text name = sender instanceof User ? NucleusHelper.get().getName((User) sender) : SpongeHelper.getText("Unknown");
                if (team.isMember(member)) {
                    member.sendMessage(SpongeHelper.formatText("&7[&2Team&7][%s&7] &f", rank)
                            .concat(name)
                            .concat(SpongeHelper.getText("&r: "))
                            .concat(original));
                } else {
                    member.sendMessage(SpongeHelper.formatText("&6[SocialSpy][%s -> TEAM]: %s", name.toPlain(), original.toPlain()));
                }
            }
        }

        private boolean canSpy(User user){
            return user.hasPermission(Permission.CHAT_SPY) && nucleusHelper.hasSocialSpy(user);
        }

        public String getName(EnumTeamStatus status){
            switch (status) {
                case OWNER: return "&6Owner";
                case MOD: return "&3Mod";
                case MEMBER: return "&fMember";
                case ALLY: return "&aAlly";
                case ENEMY: return "&0Enemy";
                case INVITED: return "&5Invited";
                default: return "&8SYSTEM";
            }
        }
    }
}
