package net.dirtcraft.ftbintegration.command.badge;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.Pair;
import net.dirtcraft.ftbintegration.utility.Switcher;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.*;

public class BadgeBase implements CommandExecutor {
    public static final String[] ALIASES = new String[]{"badge", "b"};
    private static Map<CommandSpec, String[]> commandMap;
    public static CommandSpec getCommand(){
        CommandSpec setBadge = CommandSpec.builder()
                .executor(new SetBadge())
                .permission(Permission.BADGE_CLEAR)
                .arguments(
                        GenericArguments.string(Text.of("badge")),
                        GenericArguments.optional(GenericArguments.player(Text.of("target")))
                ).build();

        CommandSpec clearBadge = CommandSpec.builder()
                .executor(new ClearBadge())
                .permission(Permission.BADGE_SET)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .build();

        CommandSpec getBadge = CommandSpec.builder()
                .executor(new GetCurrent())
                .permission(Permission.BADGE_GET)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .build();

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new BadgeBase());

        commandMap = Stream.of(
                new Pair<>(setBadge, new String[]{"set", "s"}),
                new Pair<>(clearBadge, new String[]{"clear", "c"}),
                new Pair<>(getBadge, new String[]{"get", "g"})
        ).collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a,b)->a, LinkedHashMap::new));
        commandMap.forEach(base::child);

        return base.build();
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String alias = String.join(" ", IntegrationBase.ALIAS, ALIASES[0]);
        Switcher<String> s = new Switcher<>("&3", "&b");
        List<Text> message = new ArrayList<>();
        commandMap.forEach((cmd, aliases)->{
            if (!cmd.testPermission(src) || aliases.length == 0) return;
            message.add(formatCommandSuggest(src, cmd, alias, aliases[0], s.get()));
        });
        if (message.isEmpty()) message.add(formatText("&s You have no available commands.", s.get()));
        message.add(0, formatText("&6Available subcommands:", FtbIntegration.NAME, FtbIntegration.VERSION));
        src.sendMessages(message);
        return CommandResult.success();
    }
}
