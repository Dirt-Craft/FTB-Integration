package net.dirtcraft.ftbintegration.command.user.claim;

import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.command.chunks.ChunksBase;
import net.dirtcraft.ftbintegration.command.user.UserBase;
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

public class ClaimBase implements CommandExecutor {
    public static final String[] ALIASES = new String[]{"extraclaims", "bonusclaims", "claims", "c"};
    private static Map<CommandSpec, String[]> commandMap;

    public static CommandSpec getCommand(){
        CommandSpec give = CommandSpec.builder()
                .permission(Permission.CHUNKS_CLAIM_ADD)
                .arguments(GenericArguments.integer(Text.of("amount")),
                        GenericArguments.optional(GenericArguments.user(Text.of("target"))))
                .executor(new Give())
                .build();

        CommandSpec set = CommandSpec.builder()
                .permission(Permission.CHUNKS_CLAIM_SET)
                .arguments(GenericArguments.integer(Text.of("amount")),
                        GenericArguments.optional(GenericArguments.user(Text.of("target"))))
                .executor(new Set())
                .build();

        CommandSpec remove = CommandSpec.builder()
                .permission(Permission.CHUNKS_CLAIM_REMOVE)
                .arguments(GenericArguments.integer(Text.of("amount")),
                        GenericArguments.optional(GenericArguments.user(Text.of("target"))))
                .executor(new Remove())
                .build();

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new ClaimBase());

        commandMap = Stream.of(
                new Pair<>(give, new String[]{"give", "add", "g"}),
                new Pair<>(remove, new String[]{"remove", "take", "r"}),
                new Pair<>(set, new String[]{"set", "s"})
        ).collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a,b)->a, LinkedHashMap::new));
        commandMap.forEach(base::child);

        return base.build();
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String alias = String.join(" ", IntegrationBase.ALIAS, UserBase.ALIASES[0], ALIASES[0]);
        return SpongeHelper.showCommandUsage(src, alias, commandMap);
    }
}
