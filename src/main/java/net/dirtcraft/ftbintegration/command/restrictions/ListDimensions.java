package net.dirtcraft.ftbintegration.command.restrictions;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.storage.Configuration;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import net.dirtcraft.ftbintegration.utility.Switcher;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.formatText;

public class ListDimensions implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) {
        Switcher<String> switcher = new Switcher<>("&6", "&e");
        PaginationService service = Sponge.getServiceManager().provideUnchecked(PaginationService.class);

        List<Text> contents = FtbIntegration.INSTANCE.getConfig()
                .getDimensions()
                .map(dim->format(dim, switcher.get()))
                .collect(Collectors.toList());
        service.builder()
                .title(getTitle(args))
                .contents(contents)
                .padding(formatText("&c&m-"))
                .sendTo(src);
        return CommandResult.success();
    }

    public Text format(World dim, String color){
        String cmd = String.format("/ftbi restrict rdim %s", dim.getName());
        return formatText("%s - %s", color, dim.getName())
                .toBuilder()
                .onClick(TextActions.suggestCommand(cmd))
                .build();
    }

    public Text getTitle(CommandContext args){
        Configuration configuration = FtbIntegration.INSTANCE.getConfig();
        String type = configuration.getDimListType() == Configuration.ListType.WHITELIST? "WhiteList":"Blacklist";
        Configuration.ListType other = configuration.getDimListType() == Configuration.ListType.WHITELIST?
                Configuration.ListType.BLACKLIST: Configuration.ListType.WHITELIST;
        return SpongeHelper.formatText("&8 Dimension %s ", type).toBuilder()
                .onHover(TextActions.showText(SpongeHelper.formatText("Click to toggle!")))
                .onClick(TextActions.executeCallback(a->{
                    configuration.setDimListType(other);
                    execute(a, args);
                })).build();
    }
}