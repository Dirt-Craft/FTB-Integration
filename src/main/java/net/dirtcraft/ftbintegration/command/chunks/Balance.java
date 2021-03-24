package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftblib.lib.data.Universe;
import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.core.api.ChunkPlayerInfo;
import net.dirtcraft.ftbintegration.handlers.sponge.LuckPermHandler;
import net.dirtcraft.ftbintegration.storage.Database;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.*;

public class Balance implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof User) && !args.hasAny("target"))  {
            throw new CommandException(Text.of("You must be a player or specify a target."));
        } else if (args.hasAny("target") && !src.hasPermission(Permission.CHUNKS_BALANCE_OTHERS)) {
            throw new CommandException(Text.of("You do not have permission to modify others claims."));
        }
        User target = args.<User>getOne("target").orElseGet(()->(User)src);
        Task.builder()
                .async()
                .execute(()->showBalanceSync(target, src))
                .submit(FtbIntegration.INSTANCE);
        return CommandResult.success();
    }

    public static void ModifyBalanceAndNotify(boolean success,
                                              User target,
                                              CommandSource src,
                                              String prefix,
                                              String type,
                                              String self,
                                              String other,
                                              int value){
        String name = target.getName();
        if (success && src != target && target.getPlayer().isPresent()){
            Player p = target.getPlayer().get();
            src.sendMessage(formatText(other, prefix, value, name));
            p.sendMessage(formatText(self, prefix, value, name));
            Balance.showBalanceAsync(target, p, src);
        } else if (success) {
            src.sendMessage(formatText(other, prefix, value, name));
            Balance.showBalanceAsync(target, src);
        } else if (src != target && target.getPlayer().isPresent()) {
            Player p = target.getPlayer().get();
            src.sendMessage(formatText(other, prefix, value, name));
            p.sendMessage(formatText(self, prefix, value, name));
            p.sendMessage(formatText("&cAn error occurred, Please contact staff."));
            Balance.showBalanceAsync(target, p, src);
            logFailure(src, target, type, value);
        } else {
            src.sendMessage(formatText(other, prefix, value, name));
            Balance.showBalanceAsync(target, src);
            logFailure(src, target, "CLAIM_REMOVE", value);
        }

    }

    public static void showBalanceAsync(User target, MessageReceiver... recipients){
        Task.builder()
                .async()
                .execute(()->showBalanceSync(target, recipients))
                .submit(FtbIntegration.INSTANCE);
    }

    @SuppressWarnings("SuspiciousRegexArgument")
    private static void showBalanceSync(User target, MessageReceiver... recipients){
        Database db = FtbIntegration.INSTANCE.getDatabase();
        Database.ChunkData playerData = db.getChunkData(target.getUniqueId()).orElse(null);
        ChunkPlayerInfo info = (ChunkPlayerInfo) Universe.get().getPlayer(target.getUniqueId());
        if (playerData == null || info == null) {
            for (MessageReceiver recipient : recipients) recipient.sendMessages(SpongeHelper.formatText(
                    "&cCould not retrieve ftb chunk balance.\n" +
                    "&cEither the user does not exist or are not in the database."));
            return;
        }
        LuckPermHandler.setPlayerBaseChunkData(info, target);
        info.setExtraChunks(playerData.claims, playerData.loaders);
        List<Text> message = new ArrayList<>();
        int addClaims = playerData.claims;
        int addLoaders = playerData.loaders;
        int baseLoaders = info.getBaseLoaders();
        int baseClaims = info.getBaseClaims();
        message.add(formatText("&c&m------&6%s's Chunk-Balance&c&m------", target.getName()));
        message.add(formatText("&b - Claim Chunks: %d [%d + %d]", addClaims + baseClaims, baseClaims, addClaims));
        message.add(formatText("&3 - Loader Chunks: %d [%d + %d]", addLoaders + baseLoaders, baseLoaders, addLoaders));
        message.add(formatText("&c&m-------------------------%s", target.getName().replaceAll(".", "-")));
        for (MessageReceiver recipient : recipients) recipient.sendMessages(message);
    }
}
