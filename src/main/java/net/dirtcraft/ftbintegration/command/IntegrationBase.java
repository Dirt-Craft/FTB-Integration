package net.dirtcraft.ftbintegration.command;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.command.badge.BadgeBase;
import net.dirtcraft.ftbintegration.command.chunks.ChunksBase;
import net.dirtcraft.ftbintegration.command.restrictions.RestrictBase;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.Pair;
import net.dirtcraft.ftbintegration.utility.Switcher;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.*;

public class IntegrationBase implements CommandExecutor {
    public static final String ALIAS = "ftbi";
    private static Map<CommandSpec, String[]> commandMap;

    public static void registerCommands(FtbIntegration plugin){
        CommandSpec debug = CommandSpec.builder()
                .permission(Permission.DEBUG)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .executor(new DebugClaim())
                .build();

        CommandSpec bypass = CommandSpec.builder()
                .permission(Permission.BYPASS)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .executor(new IgnoreClaim())
                .build();

        CommandSpec settings = CommandSpec.builder()
                .executor(new Settings())
                .build();

        CommandSpec reload = CommandSpec.builder()
                .permission(Permission.RELOAD_CONFIG)
                .executor(new Reload())
                .build();

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new IntegrationBase());

        commandMap = Stream.of(
                new Pair<>(ChunksBase.getCommand(), ChunksBase.ALIASES),
                new Pair<>(BadgeBase.getCommand(), BadgeBase.ALIASES),
                new Pair<>(RestrictBase.getCommand(), RestrictBase.ALIASES),
                new Pair<>(settings, new String[]{"settings", "s"}),
                new Pair<>(reload, new String[]{"reload"})
        ).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        commandMap.forEach(base::child);

        Sponge.getCommandManager().register(plugin, debug, "dc", "debugclaims");
        Sponge.getCommandManager().register(plugin, bypass, "ic", "ignoreclaims");
        Sponge.getCommandManager().register(plugin, base.build(), ALIAS, "ftbintegration");
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        Switcher<String> s = new Switcher<>("&3", "&b");
        List<Text> message = new ArrayList<>();
        commandMap.forEach((cmd, aliases)->{
            if (!cmd.testPermission(src) || aliases.length == 0) return;
            message.add(formatCommand(ALIAS, aliases[0], s.get()));
        });
        if (message.isEmpty()) message.add(formatText("&s You have no available commands.", s.get()));
        message.add(0, formatText("&6%s\n&eVersion %s\n&6Available subcommands:", FtbIntegration.NAME, FtbIntegration.VERSION));
        src.sendMessages(message);
        return CommandResult.success();
    }
}
