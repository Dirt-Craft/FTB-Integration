package net.dirtcraft.ftbintegration.command;

import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.Switcher;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import static net.dirtcraft.ftbintegration.utility.SpongeHelper.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class Settings implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(getText("&cOnly players may execute this command!"));
        PlayerData data = PlayerData.get((User) src);
        if (data == null) throw new CommandException(getText("&cUnable to retrieve data!"));

        Switcher<String> switcher = new Switcher<>("&6", "&e");
        List<Text> message = Arrays.asList(
                formatText("&c&m--------&f FTB-Integration Admin Settings &c&m--------"),
                formatText("%sBypass: ", switcher.get()).concat(getToggle(data.canBypassClaims(), data::toggleBypassClaims)),
                formatText("%sDebug: ", switcher.get()).concat(getToggle(data.canDebugClaims(), data::toggleDebugClaims)),
                formatText("%sBadge: ", switcher.get()).concat(getSuggestible(data.getBadge(), "/ftbi badge set <url>")),
                formatText("&c&m--------------------------------------------")
        );
        src.sendMessages(message);
        return CommandResult.success();
    }

    private Text getSuggestible(String value, String suggestion){
        if (value == null || value.equalsIgnoreCase("")) value = "Not Specified";
        String base = "&b\"" + value + "\"";
        return getText(base).toBuilder()
                .onClick(TextActions.suggestCommand(suggestion))
                .build();
    }

    private Text getToggle(boolean value, Runnable toggle){
        String base;
        if (value) base = "&7[&aEnabled&7] &cDisabled";
        else base = "&cEnabled &7[&aDisabled&7]";
        return getText(base).toBuilder()
                .onClick(TextActions.executeCallback(src->changeSetting(toggle, src)))
                .build();
    }

    private void changeSetting(Runnable action, CommandSource src){
        action.run();
        Sponge.getCommandManager().process(src, "ftbi settings");
    }
}
