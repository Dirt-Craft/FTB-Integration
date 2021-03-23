package net.dirtcraft.ftbintegration.command.chunks;

import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.command.user.claim.ClaimBase;
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

public class ChunksBase implements CommandExecutor {
    public static final String[] ALIASES = new String[]{"chunks", "chunk", "ch"};
    private static Map<CommandSpec, String[]> commandMap;

    public static CommandSpec getCommand(){

        CommandSpec pos1 = CommandSpec.builder()
                .executor(new Pos1())
                .build();

        CommandSpec pos2 = CommandSpec.builder()
                .executor(new Pos2())
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
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("team-id"))))
                .executor(new UnclaimChunks())
                .build();

        CommandSpec setGroupClaims = CommandSpec.builder()
                .permission(Permission.CHUNK_CLAIM_MODIFY_GROUP)
                .arguments(GenericArguments.string(Text.of("group-id")),
                        GenericArguments.integer(Text.of("value")))
                .executor(new SetGroupClaims())
                .build();

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new ChunksBase());

        commandMap = Stream.of(
                new Pair<>(pos1, new String[]{"pos1"}),
                new Pair<>(pos2, new String[]{"pos2"}),
                new Pair<>(setGroupClaims, new String[]{"setgroupclaims", "sgc"}),
                new Pair<>(toggleSpawns, new String[]{"togglespawns", "ts"}),
                new Pair<>(unclaimChunks, new String[]{"unclaim", "uc"}),
                new Pair<>(claimChunks, new String[]{"claim", "c"})
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
