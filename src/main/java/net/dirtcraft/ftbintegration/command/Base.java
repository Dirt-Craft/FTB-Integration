package net.dirtcraft.ftbintegration.command;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.utility.Permission;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;

public class Base implements CommandExecutor {

    public static void registerCommands(FtbIntegration plugin){
        CommandSpec bypass = CommandSpec.builder()
                .permission(Permission.BYPASS)
                .executor(new IgnoreClaim())
                .build();

        CommandSpec debug = CommandSpec.builder()
                .permission(Permission.DEBUG)
                .executor(new DebugClaim())
                .build();

        CommandSpec toggleSpawns = CommandSpec.builder()
                .permission(Permission.FLAG_MOB_SPAWN)
                .arguments(GenericArguments.string(Text.of("team-id")),
                        GenericArguments.bool(Text.of("value")))
                .executor(new ToggleSpawns())
                .build();

        CommandSpec claimChunks = CommandSpec.builder()
                .permission(Permission.CLAIM_CHUNK)
                .executor(new ClaimChunks())
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("team-id"))))
                .build();

        CommandSpec unclaimChunks = CommandSpec.builder()
                .permission(Permission.CLAIM_CHUNK)
                .executor(new UnclaimChunks())
                .build();

        CommandSpec base = CommandSpec.builder()
                .child(toggleSpawns, "togglespawns", "ts")
                .child(unclaimChunks, "unclaim", "uc")
                .child(claimChunks, "claim", "c")
                .build();

        Sponge.getCommandManager().register(plugin, debug, "dc", "debugclaims");
        Sponge.getCommandManager().register(plugin, bypass, "ic", "ignoreclaims");
        Sponge.getCommandManager().register(plugin, base, "ftbi", "ftbintegration");
    }

    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        return CommandResult.success();
    }
}
