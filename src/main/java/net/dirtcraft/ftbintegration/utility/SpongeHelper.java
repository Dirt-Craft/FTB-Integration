package net.dirtcraft.ftbintegration.utility;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.LocationBridge;

import javax.annotation.Nullable;
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
}
