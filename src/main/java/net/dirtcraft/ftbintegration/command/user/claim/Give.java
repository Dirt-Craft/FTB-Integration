package net.dirtcraft.ftbintegration.command.user.claim;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.command.chunks.Balance;
import net.dirtcraft.ftbintegration.core.api.ChunkPlayerInfo;
import net.dirtcraft.ftbintegration.storage.Database;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;

public class Give implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof User) && !args.hasAny("target"))  {
            throw new CommandException(Text.of("You must be a player or specify a target."));
        } else if (args.hasAny("target") && !src.hasPermission(Permission.CHUNKS_CLAIM_OTHERS)) {
            throw new CommandException(Text.of("You do not have permission to modify others claims."));
        }
        int value = args.requireOne("amount");
        User target = args.<User>getOne("target").orElseGet(()->(User)src);

        Database database = FtbIntegration.INSTANCE.getDatabase();
        boolean success = database.addClaims(target.getUniqueId(), value);
        ForgePlayer player = Universe.get().getPlayer(target.getUniqueId());
        if (player instanceof ChunkPlayerInfo){
            ChunkPlayerInfo info = (ChunkPlayerInfo) player;
            info.modifyExtraChunks(value, 0);
        }

        String result = success? "&aSuccessfully added" : "&cFailed to add";
        if (src != target){
            src.sendMessage(SpongeHelper.formatText("%s %d claim-chunks to %s's balance.", result, value, target.getName()));
            Balance.showBalanceAsync(target, target.getCommandSource().orElse(null));
        }
        src.sendMessage(SpongeHelper.formatText("%s %d claim-chunks to your balance.", result, value, target.getName()));
        if (!success && src != target) {
            src.sendMessage(SpongeHelper.formatText("&cAn error occurred, Please contact staff."));
            SpongeHelper.logFailure(src, target, "CLAIM_ADD", value);
        } else if (success) Balance.showBalanceAsync(target, src);
        return CommandResult.success();
    }
}