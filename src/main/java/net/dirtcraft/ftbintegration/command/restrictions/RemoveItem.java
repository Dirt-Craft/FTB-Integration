package net.dirtcraft.ftbintegration.command.restrictions;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import net.minecraft.item.Item;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.item.ItemType;

import javax.annotation.Nonnull;
import java.util.Locale;

public class RemoveItem implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        ItemType specified = RestrictionsBase.getItemType(src, args);
        boolean success = FtbIntegration.INSTANCE.getConfig().removeItemBlacklist((Item) specified);
        String template;
        if (success) template = "&aSuccessfully removed the blacklist for &7\"&c%s&7\"";
        else template = "&cFailed remove the blacklist for &7\"&c%s&7\" &8(Is it blacklisted?)";
        src.sendMessages(SpongeHelper.formatText(template, specified.getTranslation().get(Locale.ENGLISH)));
        return CommandResult.success();
    }
}
