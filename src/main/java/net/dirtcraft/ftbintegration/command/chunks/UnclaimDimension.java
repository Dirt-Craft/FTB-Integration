package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.core.api.CompatClaimedChunks;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import net.minecraft.entity.player.EntityPlayer;
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
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
public class UnclaimDimension implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command!"));
        PlayerData data = PlayerData.get((Player) src);
        int id = ((EntityPlayer)src).dimension;
        org.spongepowered.api.world.World world = ((Player) src).getWorld();
        if (data == null) throw new CommandException(Text.of("Unable to locate player data!"));
        Text warning = SpongeHelper.formatText("&cWarning! You are attempting to unclaim all chunks in the\ncurrent (%s[%d]) dimension! Are you sure?! ", world.getName(), id);
        Text confirm = SpongeHelper.formatText("&bConfirm")
                .toBuilder()
                .onClick(TextActions.executeCallback(x -> execute(src, id)))
                .onHover(TextActions.showText(SpongeHelper.formatText("Execute claim command")))
                .build();
        src.sendMessage(warning.concat(confirm));
        return CommandResult.success();
    }

    public void execute(CommandSource src, int dim){
        ClaimedChunks claimedChunks = ClaimedChunks.instance;
        CompatClaimedChunks compat = (CompatClaimedChunks) claimedChunks;

        Collection<ChunkDimPos> chunks = claimedChunks.getAllChunks().stream()
                .map(ClaimedChunk::getPos)
                .filter(pos -> pos.dim == dim)
                .collect(Collectors.toList());

        long success = chunks.stream()
                .map(compat::compatUnclaimChunk)
                .filter(Boolean::booleanValue)
                .count();

        long fail = chunks.size() - success;
        String message = String.format("Successfully unclaimed %d chunks, with %d failures.", success, fail);
        Text response = TextSerializers.FORMATTING_CODE.deserialize(message);
        src.sendMessage(response);
    }
}
