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

public class Set implements CommandExecutor {
    private static final String TYPE = "CLAIM_SET";
    private static final String SELF = "%3$s your claim-chunks balance to %2$d.";
    private static final String OTHER = "%s %3$s's claim-chunks balance to %2$d.";
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        User target = SpongeHelper.targetOrSelf(src, args, Permission.CHUNKS_CLAIM_OTHERS);
        int value = args.requireOne("amount");

        Database database = FtbIntegration.INSTANCE.getDatabase();
        boolean success = database.setClaims(target.getUniqueId(), value);

        String result = success ? "&aSuccessfully set" : "&cFailed to set";
        Balance.ModifyBalanceAndNotify(success, target, src, result, TYPE, SELF, OTHER, value);
        return CommandResult.success();
    }
}