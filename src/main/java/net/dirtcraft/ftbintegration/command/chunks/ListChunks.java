package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.core.mixins.generic.AccessorFinalIDObject;
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

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.formatText;

@SuppressWarnings("DuplicatedCode")
public class ListChunks implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command!"));
        String team = args.requireOne("team-id");
        execute(src, team, ((EntityPlayer)src).dimension);
        return CommandResult.success();
    }

    public void execute(CommandSource src, String teamId, int dim){
        Switcher<String> switcher = new Switcher<>("&3", "&b");
        PaginationService service = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        ClaimedChunks claimedChunks = ClaimedChunks.instance;

        Collection<Text> chunks = claimedChunks.getAllChunks().stream()
                .filter(ch->((AccessorFinalIDObject)ch.getTeam()).getTeamIdString().equals(teamId))
                .map(ClaimedChunk::getPos)
                .filter(pos -> pos.dim == dim)
                .map(pos-> SpongeHelper.formatText(" &8- %s%d, %d", switcher.get(), pos.posX, pos.posZ).toBuilder()
                        .onHover(TextActions.showText(SpongeHelper.getText("Click to teleport!")))
                        .onClick(TextActions.runCommand(String.format("/tppos -f -c %d 4 %d", pos.posX, pos.posZ)))
                        .toText())
                .collect(Collectors.toList());
        service.builder()
                .title(SpongeHelper.getText("&6Chunks located:"))
                .contents(chunks)
                .padding(formatText("&c&m-"))
                .sendTo(src);
    }
}
