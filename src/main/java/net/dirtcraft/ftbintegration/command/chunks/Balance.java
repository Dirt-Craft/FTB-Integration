package net.dirtcraft.ftbintegration.command.chunks;

import com.feed_the_beast.ftblib.lib.data.Universe;
import net.dirtcraft.ftbintegration.FtbIntegration;
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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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

    public static void showBalanceAsync(User target, CommandSource recipient){
        Task.builder()
                .async()
                .execute(()->showBalanceSync(target, recipient))
                .submit(FtbIntegration.INSTANCE);
    }

    @SuppressWarnings("SuspiciousRegexArgument")
    private static void showBalanceSync(User target, CommandSource recipient){
        Database db = FtbIntegration.INSTANCE.getDatabase();
        Database.ChunkData playerData = db.getChunkData(target.getUniqueId()).orElse(null);
        ChunkPlayerInfo info = (ChunkPlayerInfo) Universe.get().getPlayer(target.getUniqueId());
        if (playerData == null || info == null) {
            return;
        }
        List<Text> message = new ArrayList<>();
        int addClaims = playerData.claims;
        int addLoaders = playerData.loaders;
        int baseLoaders = info.getBaseLoaders();
        int baseClaims = info.getBaseClaims();
        message.add(SpongeHelper.formatText("&c&m----- &6%s's Chunk-Balance &c&m-----", target.getName()));
        message.add(SpongeHelper.formatText("&b - Claim Chunks: %d [%d + %d]", addClaims + baseClaims, baseClaims, addClaims));
        message.add(SpongeHelper.formatText("&3 - Loader Chunks: %d [%d + %d]", addLoaders + baseLoaders, baseLoaders, addLoaders));
        message.add(SpongeHelper.formatText("&c&m------------------------%s", target.getName().replaceAll(".", "-")));
        recipient.sendMessages(message);
    }
}
