package net.dirtcraft.ftbutilities.spongeintegration.command;

import net.dirtcraft.ftbutilities.spongeintegration.FtbUtilitiesSpongeIntegration;
import net.dirtcraft.ftbutilities.spongeintegration.utility.Permission;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class Base implements CommandExecutor {

    public static void registerCommands(FtbUtilitiesSpongeIntegration plugin){
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
                .arguments(GenericArguments.string(Text.of("teamid")),
                        GenericArguments.bool(Text.of("value")))
                .executor(new ToggleSpawns())
                .build();

        CommandSpec settings = CommandSpec.builder()
                .executor(new Settings())
                .build();

        CommandSpec base = CommandSpec.builder()
                .child(toggleSpawns, "togglespawns", "ts")
                .child(settings, "settings", "s")
                .build();

        Sponge.getCommandManager().register(plugin, base, "ftbi", "ftbintegration");
        Sponge.getCommandManager().register(plugin, bypass, "ic", "ignoreclaims");
        Sponge.getCommandManager().register(plugin, debug, "dc", "debugclaims");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return CommandResult.success();
    }
}
