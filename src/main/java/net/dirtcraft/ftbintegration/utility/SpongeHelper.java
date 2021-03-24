package net.dirtcraft.ftbintegration.utility;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.LocationBridge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpongeHelper {
    @SuppressWarnings("ConstantConditions")
    public static BlockPos getBlockPos(Location<World> location){
        return ((LocationBridge) (Object) location).bridge$getBlockPos();
    }

    public static @Nullable net.minecraft.world.World getWorld(Location<World> location){
        return (net.minecraft.world.World) location.getExtent();
    }

    public static IBlockState getBlockState(Location<World> location){
        return (IBlockState) location.getBlock();
    }

    public static Text formatText(String format, Object... args){
        String content = String.format(format, args);
        return TextSerializers.FORMATTING_CODE.deserialize(content);
    }

    public static Text getText(String string){
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }


    public static Text formatCommand(String base, String alias, String color){
        String command = "/" + String.join(" ", base, alias);
        return formatText("%s - %s", color, alias).toBuilder()
                .onClick(TextActions.runCommand(command))
                .onHover(TextActions.showText(formatText("Click to run %s", command)))
                .build();
    }

    public static Text formatCommandSuggest(CommandSource src, CommandSpec spec, String base, String alias, String color){
        String command = "/" + String.join(" ", base, alias);
        return formatText("%s - %s", color, alias).toBuilder()
                .onClick(TextActions.suggestCommand(command + " " + spec.getUsage(src).toPlain()))
                .onHover(TextActions.showText(formatText("Click to run %s", command)))
                .build();
    }

    public static boolean hasOwner(BlockSnapshot clickedBlock){
        Optional<OwnershipTrackedBridge> oTe = clickedBlock
                .getLocation()
                .flatMap(Location::getTileEntity)
                .map(OwnershipTrackedBridge.class::cast);
        return oTe.isPresent() &&
                oTe.flatMap(OwnershipTrackedBridge::tracked$getNotifierUUID).isPresent() ||
                oTe.flatMap(OwnershipTrackedBridge::tracked$getOwnerUUID).isPresent();
    }

    public static void sendOwnerData(Player player, BlockSnapshot clickedBlock){
        Task.builder()
                .async()
                .execute(()->{
                    Optional<OwnershipTrackedBridge> oTe = clickedBlock
                            .getLocation()
                            .flatMap(Location::getTileEntity)
                            .map(OwnershipTrackedBridge.class::cast);
                    GameProfileManager manager = Sponge.getServer().getGameProfileManager();
                    String owner = oTe.flatMap(OwnershipTrackedBridge::tracked$getOwnerUUID)
                            .map(manager::get)
                            .map(CompletableFuture::join)
                            .map(profile->profile.getName().orElse(profile.getUniqueId().toString()))
                            .orElse("None.");
                    String notifier = oTe.flatMap(OwnershipTrackedBridge::tracked$getNotifierUUID)
                            .map(manager::get)
                            .map(CompletableFuture::join)
                            .map(profile->profile.getName().orElse(profile.getUniqueId().toString()))
                            .orElse("None.");
                    player.sendMessage(formatText("&7Owner: &d%s\n&8Notifier: &5%s", owner, notifier));
                }).submit(FtbIntegration.INSTANCE);
    }

    @Nonnull
    public static CommandResult showCommandUsage(CommandSource src, String alias, Map<CommandSpec, String[]> commandMap) throws CommandException {
        return showCommandUsage(src, alias, commandMap, true);
    }

    @Nonnull
    public static CommandResult showCommandUsage(CommandSource src, String alias, Map<CommandSpec, String[]> commandMap, boolean suggest) throws CommandException {
        Switcher<String> s = new Switcher<>("&3", "&b");
        List<Text> message = new ArrayList<>();
        commandMap.forEach((cmd, aliases)->{
            if (!cmd.testPermission(src) || aliases.length == 0) return;
            if (!suggest) message.add(formatCommand(alias, aliases[0], s.get()));
            else message.add(formatCommandSuggest(src, cmd, alias, aliases[0], s.get()));
        });
        if (message.isEmpty()) message.add(formatText("%sYou have no available commands.", s.get()));
        message.add(0, formatText("&6Available subcommands:", FtbIntegration.NAME, FtbIntegration.VERSION));
        src.sendMessages(message);
        return CommandResult.success();
    }

    public static User targetOrSelf(CommandSource src, @Nonnull CommandContext args,@Nonnull String permission) throws CommandException {
        if (!(src instanceof User) && !args.hasAny("target")) {
            throw new CommandException(Text.of("You must be a player or specify a target."));
        } else if (args.hasAny("target") && !src.hasPermission(permission)) {
            throw new CommandException(Text.of("You do not have permission to modify others claims."));
        }
        //noinspection ConstantConditions
        return args.<User>getOne("target").orElseGet(() -> (User) src);
    }

    public static void logFailure(CommandSource source, User target, String type, int amount) {
        //todo probably log to a file or something i guess?
        String output = String.format("Failed operation %s (%d, %s) by %s", type, amount, target.getName(), source.getName());
        System.out.println(output);
    }
}
