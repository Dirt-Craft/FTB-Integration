package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.TeamType;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimResult;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
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

@SuppressWarnings("DuplicatedCode")
public class ClaimChunks implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of("Only players may use this command!"));
        PlayerData data = PlayerData.get((Player)src);
        if (data == null) throw new CommandException(Text.of("Unable to locate player data!"));
        ForgePlayer forgePlayer;

        if (args.hasAny("team-id")) forgePlayer = args.<String>getOne("team-id")
                .filter(discard->src.hasPermission(Permission.CLAIM_OTHER))
                .map(Universe.get()::getTeam)
                .filter(t->t.type != TeamType.NONE)
                .map(ClaimedChunkHelper::getTeamOwner)
                .orElseThrow(()->new CommandException(Text.of("Unable to find team, or you do not have permission.")));
        else forgePlayer = data.getForgePlayer();

        if (forgePlayer == null) throw new CommandException(Text.of("Invalid Team!"));
        if (data.getSelectedRegion().size() > 50){
            Text warning = SpongeHelper.formatText("&cWarning! You are attempting to claim %d chunks.\n&6Do you wish to proceed? ", data.getSelectedRegion().size());
            Text confirm = SpongeHelper.formatText("&bConfirm")
                    .toBuilder()
                    .onClick(TextActions.executeCallback(x->execute(src, data, forgePlayer)))
                    .onHover(TextActions.showText(SpongeHelper.formatText("Execute claim command")))
                    .build();
            src.sendMessage(warning.concat(confirm));
        } else execute(src, data, forgePlayer);
        
        return CommandResult.success();
    }

    public void execute(CommandSource src, PlayerData data, ForgePlayer forgePlayer){
        ClaimedChunks claimedChunks = ClaimedChunks.instance;

        for (ChunkDimPos pos : data.getSelectedRegion()) claimedChunks.claimChunk(forgePlayer, pos);
        long success = data.getSelectedRegion().stream()
                .map(ch->claimedChunks.claimChunk(forgePlayer, ch))
                .filter(claimResult -> claimResult == ClaimResult.SUCCESS)
                .count();

        long fail = data.getSelectedRegion().size() - success;

        String message = String.format("Successfully claimed %d chunks, with %d failures.", success, fail);
        Text response = TextSerializers.FORMATTING_CODE.deserialize(message);
        src.sendMessage(response);
    }
}
