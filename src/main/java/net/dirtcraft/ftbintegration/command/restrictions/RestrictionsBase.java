package net.dirtcraft.ftbintegration.command.restrictions;

import net.dirtcraft.ftbintegration.storage.Permission;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

public class RestrictionsBase {
    public static CommandSpec getCommand(){
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


        return CommandSpec.builder()
                .child(itemBlacklist, "blacklistitem", "bitem")
                .child(itemRemove, "removeitem", "ritem")
                .child(itemList, "listitem", "litem")
                .child(interactWhitelist, "whitelistinteract", "winteract")
                .child(interactRemove, "removeinteract", "rinteract")
                .child(interactList, "listinteract", "linteract")
                .child(editWhitelist, "whitelistedit", "wedit")
                .child(editRemove, "removeedit", "redit")
                .child(editList, "listedit", "ledit")
                .build();
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
