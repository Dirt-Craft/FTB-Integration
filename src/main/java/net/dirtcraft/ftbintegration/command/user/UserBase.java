package net.dirtcraft.ftbintegration.command.user;

import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.command.chunks.*;
import net.dirtcraft.ftbintegration.command.user.claim.ClaimBase;
import net.dirtcraft.ftbintegration.command.user.loader.LoaderBase;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.Pair;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserBase implements CommandExecutor {
    public static final String[] ALIASES = new String[]{"user", "u", "admin"};
    private static Map<CommandSpec, String[]> commandMap;

    public static CommandSpec getCommand(){
        CommandSpec balance = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.user(Text.of("target"))))
                .permission(Permission.CHUNKS_BALANCE_BASE)
                .executor(new Balance())
                .build();

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new UserBase());

        commandMap = Stream.of(
                new Pair<>(LoaderBase.getCommand(), LoaderBase.ALIASES),
                new Pair<>(ClaimBase.getCommand(), ClaimBase.ALIASES),
                new Pair<>(balance, new String[]{"balance", "bal", "b"})
        ).collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a, b)->a, LinkedHashMap::new));
        commandMap.forEach(base::child);

        return base.build();
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String alias = String.join(" ", IntegrationBase.ALIAS, ALIASES[0]);
        return SpongeHelper.showCommandUsage(src, alias, commandMap, false);
    }
}
