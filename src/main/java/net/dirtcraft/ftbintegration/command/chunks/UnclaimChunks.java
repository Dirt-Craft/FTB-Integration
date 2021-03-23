package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.TeamType;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.core.api.CompatClaimedChunks;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public class UnclaimChunks implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command!"));
        Optional<ForgeTeam> team = !args.hasAny("team-id")? Optional.empty(): args.<String>getOne("team-id")
                .filter(discard->src.hasPermission(Permission.CLAIM_OTHER))
                .map(Universe.get()::getTeam)
                .filter(t->t.type != TeamType.NONE);
        PlayerData data = PlayerData.get((Player)src);
        if (data == null) throw new CommandException(Text.of("Unable to locate player data!"));
        if (data.getSelectedRegion().size() > 50){
            Text warning = SpongeHelper.formatText("&cWarning! You are attempting to unclaim %d chunks.\n&6Do you wish to proceed? ", data.getSelectedRegion().size());
            Text confirm = SpongeHelper.formatText("&bConfirm")
                    .toBuilder()
                    .onClick(TextActions.executeCallback(x->execute(src, data, team.orElse(null))))
                    .onHover(TextActions.showText(SpongeHelper.formatText("Execute claim command")))
                    .build();
            src.sendMessage(warning.concat(confirm));
        } else execute(src, data, team.orElse(null));
        return CommandResult.success();
    }

    public void execute(CommandSource src, PlayerData data, ForgeTeam team){
        ForgePlayer forgePlayer = data.getForgePlayer();
        ClaimedChunks claimedChunks = ClaimedChunks.instance;
        CompatClaimedChunks compat = (CompatClaimedChunks) claimedChunks;

        long success = data.getSelectedRegion().stream()
                .map(claimedChunks::getChunk)
                .filter(Objects::nonNull)
                .filter(claimedChunk -> canUnclaim(claimedChunk, forgePlayer, src))
                .map(ClaimedChunk::getPos)
                .filter(pos-> team == null || hasTeam(pos, team))
                .map(compat::compatUnclaimChunk)
                .filter(Boolean::booleanValue)
                .count();
        long fail = data.getSelectedRegion().size() - success;
        String message = String.format("Successfully unclaimed %d chunks, with %d failures.", success, fail);
        Text response = TextSerializers.FORMATTING_CODE.deserialize(message);
        src.sendMessage(response);
    }

    public boolean canUnclaim(ClaimedChunk chunk, ForgePlayer player, CommandSource src){
        return chunk.getTeam().isModerator(player) || src.hasPermission(Permission.CLAIM_OTHER);
    }

    private boolean hasTeam(ChunkDimPos pos, ForgeTeam team){
        ClaimedChunks chunks = ClaimedChunks.instance;
        ClaimedChunk chunk = chunks.getChunk(pos);
        if (chunk == null) return false;
        else return chunk.getTeam() == team;
    }
}
