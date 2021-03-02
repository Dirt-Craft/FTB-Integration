package net.dirtcraft.ftbintegration.command;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.core.api.CompatClaimedChunks;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.storage.Permission;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("DuplicatedCode")
public class UnclaimChunks implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command!"));
        PlayerData data = PlayerData.get((Player)src);
        if (data == null) throw new CommandException(Text.of("Unable to locate player data!"));
        ClaimedChunks claimedChunks = ClaimedChunks.instance;
        CompatClaimedChunks compat = (CompatClaimedChunks) claimedChunks;
        ForgePlayer forgePlayer = data.getForgePlayer();
        long success = data.getSelectedRegion().stream()
                .map(claimedChunks::getChunk)
                .filter(Objects::nonNull)
                .filter(claimedChunk -> canUnclaim(claimedChunk, forgePlayer, src))
                .map(ClaimedChunk::getPos)
                .map(compat::compatUnclaimChunk)
                .filter(Boolean::booleanValue)
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
