package net.dirtcraft.ftbintegration.command.user.claim;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.command.chunks.Balance;
import net.dirtcraft.ftbintegration.storage.Database;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;

import javax.annotation.Nonnull;

public class Remove implements CommandExecutor {
    private static final String TYPE = "CLAIM_REMOVE";
    private static final String SELF = "%s %d claim-chunks from your balance.";
    private static final String OTHER = "%s %d claim-chunks from %s's balance.";
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        User target = SpongeHelper.targetOrSelf(src, args, Permission.CHUNKS_CLAIM_OTHERS);
        int value = args.requireOne("amount");

        Database database = FtbIntegration.INSTANCE.getDatabase();
        boolean success = database.removeClaims(target.getUniqueId(), value);

        String result = success ? "&aSuccessfully removed" : "&cFailed to remove";
        Balance.ModifyBalanceAndNotify(success, target, src, result, TYPE, SELF, OTHER, value);
        return CommandResult.success();
    }
}