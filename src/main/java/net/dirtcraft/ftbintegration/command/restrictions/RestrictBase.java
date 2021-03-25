package net.dirtcraft.ftbintegration.command.restrictions;

import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.Pair;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RestrictBase implements CommandExecutor {
    public static final String[] ALIASES = new String[]{"restrict", "r"};
    private static Map<CommandSpec, String[]> commandMap;
    public static CommandSpec getCommand(){
        CommandSpec dimList = CommandSpec.builder()
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new ListDimensions())
                .build();

        CommandSpec dimAdd = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new AddDimension())
                .build();

        CommandSpec dimRem = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new RemoveDimension())
                .build();

        CommandSpec itemBlacklist = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("item-id"), ItemType.class)))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new BlacklistItem())
                .build();

        CommandSpec itemRemove = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("item-id"), ItemType.class)))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new RemoveItem())
                .build();

        CommandSpec itemList = CommandSpec.builder()
                .permission(Permission.RESTRICT_VIEW)
                .executor(new ListItem())
                .build();

        CommandSpec interactWhitelist = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("item-id"), ItemType.class)))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new WhitelistInteract())
                .build();

        CommandSpec interactRemove = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("item-id"), ItemType.class)))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new RemoveInteract())
                .build();

        CommandSpec interactList = CommandSpec.builder()
                .permission(Permission.RESTRICT_VIEW)
                .executor(new ListInteract())
                .build();

        CommandSpec editWhitelist = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("item-id"), ItemType.class)))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new WhitelistEdit())
                .build();

        CommandSpec editRemove = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("item-id"), ItemType.class)))
                .permission(Permission.RESTRICT_MODIFY)
                .executor(new RemoveEdit())
                .build();

        CommandSpec editList = CommandSpec.builder()
                .permission(Permission.RESTRICT_VIEW)
                .executor(new ListEdit())
                .build();

        CommandSpec.Builder base = CommandSpec.builder()
                .executor(new RestrictBase());

        commandMap = Stream.of(
                new Pair<>(dimList, new String[]{"listdims", "ldims", "listworlds", "worlds"}),
                new Pair<>(dimAdd, new String[]{"adddim", "addworld", "adim", "aworld"}),
                new Pair<>(dimRem, new String[]{"removedim", "removeworld", "rdim", "rworld"}),
                new Pair<>(itemBlacklist, new String[]{"blacklistitem", "bitem"}),
                new Pair<>(itemRemove, new String[]{"removeitem", "ritem"}),
                new Pair<>(itemList, new String[]{"listitem", "litem"}),
                new Pair<>(interactWhitelist, new String[]{"whitelistinteract", "winteract"}),
                new Pair<>(interactRemove, new String[]{"removeinteract", "rinteract"}),
                new Pair<>(interactList, new String[]{"listinteract", "linteract"}),
                new Pair<>(editWhitelist, new String[]{"whitelistedit", "wedit"}),
                new Pair<>(editRemove, new String[]{"removeedit", "redit"}),
                new Pair<>(editList, new String[]{"listedit", "ledit"})
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

    public static ItemType getItemType(CommandSource src, CommandContext args) throws CommandException{
        ItemType specified = args.<ItemType>getOne("item-id").orElse(null);
        if (specified == null){
            if (!(src instanceof Player)) throw new CommandException(Text.of("You must specify an item to blacklist."));
            specified = ((Player)src).getItemInHand(HandTypes.MAIN_HAND)
                    .map(ItemStack::getType)
                    .filter(t->t != ItemTypes.AIR)
                    .orElseThrow(()->new CommandException(Text.of("You must specify an item-type or be holding one.")));
        }
        return specified;
    }

    public static BlockType getBlockType(CommandSource src, CommandContext args) throws CommandException{
        BlockType specified = args.<BlockType>getOne("item-id").orElse(null);
        if (specified != null) return specified;
        else return getItemType(src, args)
                .getBlock()
                .orElseThrow(()->new CommandException(Text.of("The item-type specified does not have a block-type!")));
    }
}
