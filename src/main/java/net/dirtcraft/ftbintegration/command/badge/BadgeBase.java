package net.dirtcraft.ftbintegration.command.badge;

import net.dirtcraft.ftbintegration.storage.Permission;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class BadgeBase {
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

        return CommandSpec.builder()
                .child(setBadge, "set", "s")
                .child(clearBadge, "clear", "c")
                .child(getBadge, "get", "g")
                .build();
    }
}
