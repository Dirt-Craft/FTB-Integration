package net.dirtcraft.ftbintegration.command.restrictions;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import net.minecraft.block.Block;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import javax.annotation.Nonnull;
import java.util.Locale;

public class WhitelistInteract implements CommandExecutor {
    @Nonnull
    @Override
    public CommandResult execute(@Nonnull CommandSource src, @Nonnull CommandContext args) throws CommandException {
        BlockType specified = RestrictionsBase.getBlockType(src, args);
        boolean success = FtbIntegration.INSTANCE.getConfig().addInteractWhitelist((Block) specified);
        String template;
        if (success) template = "&aSuccessfully whitelisted &7\"&c%s&7\"";
        else template = "&cFailed to whitelist &7\"&c%s&7\" &8(Is it already whitelisted?)";
        src.sendMessages(SpongeHelper.formatText(template, specified.getTranslation().get(Locale.ENGLISH)));
        return CommandResult.success();
    }
}