package net.dirtcraft.ftbintegration.command.debug;

import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.Pair;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DebugBase implements CommandExecutor {
    public static final String[] ALIASES = new String[]{"debug", "dev"};
    private static Map<CommandSpec, String[]> commandMap;
    public static CommandSpec getCommand(){
        CommandSpec setLogging = CommandSpec.builder()
                .executor(new SetLogging())
                .permission(Permission.LOG_MODIFY)
                .build();

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new net.dirtcraft.ftbintegration.command.badge.BadgeBase());

        commandMap = Stream.of(
                new Pair<>(setLogging, new String[]{"logging", "l"})
        ).collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a,b)->a, LinkedHashMap::new));
        commandMap.forEach(base::child);

        return base.build();
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String alias = String.join(" ", IntegrationBase.ALIAS, ALIASES[0]);
        return SpongeHelper.showCommandUsage(src, alias, commandMap);
    }
}
