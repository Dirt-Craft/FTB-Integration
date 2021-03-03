package net.dirtcraft.ftbintegration.command.restrictions;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.utility.Switcher;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.formatText;

public class ListEdit implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        Switcher<String> switcher = new Switcher<>("&6", "&e");
        PaginationService service = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        List<Text> contents = FtbIntegration.INSTANCE.getConfig()
                .getEditWhitelist()
                .map(type->format(type, switcher.get()))
                .collect(Collectors.toList());
        service.builder()
                .title(formatText("&f Block-Edit Whitelist "))
                .contents(contents)
                .padding(formatText("&c&m-"))
                .sendTo(src);
        return CommandResult.success();
    }

    public Text format(BlockType type, String color){
        String cmd = String.format("/ftbi restrict redit %s", type.getName());
        return formatText("%s - %s", color, type.getTranslation().get(Locale.ENGLISH))
                .toBuilder()
                .onClick(TextActions.suggestCommand(cmd))
                .build();
    }
}
