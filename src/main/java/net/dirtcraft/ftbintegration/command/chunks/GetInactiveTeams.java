package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.core.api.CompatClaimedChunks;
import net.dirtcraft.ftbintegration.core.api.DebugPlayerInfo;
import net.dirtcraft.ftbintegration.core.mixins.generic.AccessorFinalIDObject;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import net.dirtcraft.ftbintegration.utility.Switcher;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.formatText;

@SuppressWarnings("DuplicatedCode")
public class GetInactiveTeams implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        execute(src);
        return CommandResult.success();
    }

    public void execute(CommandSource src){
        long after = 1000L * 60L * 60L * 24L * 30L;
        List<ForgeTeam> inactive = new ArrayList<>();
        teams: for (ForgeTeam team : Universe.get().getTeams()) {
            if (team.owner == null) continue;
            for (ForgePlayer player : team.getMembers()) {
                DebugPlayerInfo dPlayer = (DebugPlayerInfo) player;
                if (dPlayer.getLastSeenMs() < after) continue teams;
            }
            inactive.add(team);
        }

        if (inactive.isEmpty()) {
            src.sendMessage(SpongeHelper.getText("&cThere was no inactive teams found!"));
        } else {
            Switcher<String> switcher = new Switcher<>("&6", "&e");
            PaginationService service = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
            List<Text> output = inactive.stream()
                    .map(t->" &7- " + switcher.get() + ((AccessorFinalIDObject)t).getTeamIdString())
                    .map(SpongeHelper::getText)
                    .collect(Collectors.toList());
            service.builder()
                    .title(SpongeHelper.getText("&6Inactive Teams Located:"))
                    .contents(output)
                    .padding(formatText("&c&m-"))
                    .sendTo(src);

        }
    }
}
