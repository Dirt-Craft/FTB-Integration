package net.dirtcraft.ftbintegration.command.chunks;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.command.badge.BadgeBase;
import net.dirtcraft.ftbintegration.command.restrictions.RestrictBase;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.*;

public class ChunksBase implements CommandExecutor {
    public static final String[] ALIASES = new String[]{"chunks", "ch"};
    private static Map<CommandSpec, String[]> commandMap;

    public static CommandSpec getCommand(){
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

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new ChunksBase());

        commandMap = Stream.of(
                new Pair<>(setGroupClaims, new String[]{"setgroupclaims", "sgc"}),
                new Pair<>(toggleSpawns, new String[]{"togglespawns", "ts"}),
                new Pair<>(unclaimChunks, new String[]{"unclaim", "uc"}),
                new Pair<>(claimChunks, new String[]{"claim", "c"})
        ).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        commandMap.forEach(base::child);

        return base.build();
    }

    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        String alias = String.join(" ", IntegrationBase.ALIAS + ALIASES[0]);
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
