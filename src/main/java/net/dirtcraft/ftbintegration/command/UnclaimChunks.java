package net.dirtcraft.ftbintegration.command;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimResult;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
import net.dirtcraft.ftbintegration.utility.Permission;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class UnclaimChunks implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command!"));
        PlayerData data = PlayerData.get((Player)src);
        if (data == null) throw new CommandException(Text.of("Unable to locate player data!"));
        ClaimedChunks claimedChunks = ClaimedChunks.instance;
        ForgePlayer forgePlayer;
        ForgeTeam forgeTeam = args.<String>getOne("team")
                .filter(discard->src.hasPermission(Permission.CLAIM_OTHER))
                .map(Universe.get()::getTeam)
                .orElse(null);
        if (forgeTeam == null) forgePlayer = data.getForgePlayer();
        else if (forgeTeam.type.isNone) throw new CommandException(Text.of("Invalid Team!"));
        else forgePlayer = ClaimedChunkHelper.getTeamOwner(forgeTeam);
        if (forgePlayer == null) throw new CommandException(Text.of("Invalid Team!"));
        for (ChunkDimPos pos : data.getSelectedRegion()) claimedChunks.claimChunk(forgePlayer, pos);
        long success = data.getSelectedRegion().stream()
                .map(claimedChunks::getChunk)
                .filter(Objects::nonNull)
                .filter(claimedChunk -> canUnclaim(claimedChunk, forgePlayer, src))
                .map(ClaimedChunk::getPos)
                .map(claimedChunks::unclaimChunk)
                .count();
        long fail = data.getSelectedRegion().size() - success;
        String message = String.format("Successfully unclaimed %d chunks, with %d failures.", success, fail);
        Text response = TextSerializers.FORMATTING_CODE.deserialize(message);
        src.sendMessage(response);
        return CommandResult.success();
    }

    public boolean canUnclaim(ClaimedChunk chunk, ForgePlayer player, CommandSource src){
        return chunk.getTeam().isModerator(player) || src.hasPermission(Permission.CLAIM_OTHER);
    }
}
