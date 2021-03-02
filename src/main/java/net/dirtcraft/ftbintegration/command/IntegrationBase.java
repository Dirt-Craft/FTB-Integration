package net.dirtcraft.ftbintegration.command;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.command.badge.BadgeBase;
import net.dirtcraft.ftbintegration.command.chunks.SetGroupClaims;
import net.dirtcraft.ftbintegration.command.restrictions.RestrictionsBase;
import net.dirtcraft.ftbintegration.storage.Permission;
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

public class IntegrationBase implements CommandExecutor {

    public static void registerCommands(FtbIntegration plugin){
        CommandSpec settings = CommandSpec.builder()
                .executor(new Settings())
                .build();

        CommandSpec reload = CommandSpec.builder()
                .permission(Permission.RELOAD_CONFIG)
                .executor((a,b)->{
                    FtbIntegration.INSTANCE.loadConfig();
                    return CommandResult.success();
                }).build();

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

        CommandSpec setGroupClaims = CommandSpec.builder()
                .permission(Permission.CHUNK_CLAIM_MODIFY_GROUP)
                .arguments(GenericArguments.string(Text.of("group-id")),
                        GenericArguments.string(Text.of("value")))
                .executor(new SetGroupClaims())
                .build();

        CommandSpec chunks = CommandSpec.builder()
                .child(setGroupClaims, "setgroupclaims", "sgc")
                .build();

        CommandSpec base = CommandSpec.builder()
                .child(RestrictionsBase.getCommand(), "restrict")
                .child(BadgeBase.getCommand(), "badge", "b")
                .child(toggleSpawns, "togglespawns", "ts")
                .child(unclaimChunks, "unclaim", "uc")
                .child(claimChunks, "claim", "c")
                .child(settings, "settings", "s")
                .child(chunks, "chunks", "ch")
                .child(reload, "reload", "r")
                .build();

        Sponge.getCommandManager().register(plugin, debug, "dc", "debugclaims");
        Sponge.getCommandManager().register(plugin, bypass, "ic", "ignoreclaims");
        Sponge.getCommandManager().register(plugin, base, "ftbi", "ftbintegration");
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        return CommandResult.success();
    }
}
