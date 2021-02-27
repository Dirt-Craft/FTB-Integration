package net.dirtcraft.ftbintegration.command.chunks;

import net.dirtcraft.ftbintegration.handlers.forge.SpongePermissionHandler;
import net.dirtcraft.ftbintegration.storage.Permission;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import javax.annotation.Nonnull;

public class SetGroupClaims implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String group = args.requireOne("group-id");
        String value = args.requireOne("value");
        SpongePermissionHandler.INSTANCE.setGroupMeta(group, Permission.CHUNK_CLAIM_META, value);
        return CommandResult.success();
    }
}
