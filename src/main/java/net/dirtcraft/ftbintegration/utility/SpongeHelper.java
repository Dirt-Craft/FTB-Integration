package net.dirtcraft.ftbintegration.utility;

import com.mojang.authlib.GameProfile;
import net.dirtcraft.ftbintegration.FtbIntegration;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.world.LocationBridge;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SpongeHelper {
    public static GameProfile getGameProfile(User user){
        return new GameProfile(user.getUniqueId(), user.getName());
    }

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

    public static boolean hasOwner(BlockSnapshot clickedBlock){
        return clickedBlock.getCreator().isPresent() || clickedBlock.getNotifier().isPresent();
    }

    public static void sendOwnerData(Player player, BlockSnapshot clickedBlock){
        Task.builder()
                .async()
                .execute(()->{
                    GameProfileManager manager = Sponge.getServer().getGameProfileManager();
                    String owner = clickedBlock.getCreator()
                            .map(manager::get)
                            .map(CompletableFuture::join)
                            .map(profile->profile.getName().orElse(profile.getUniqueId().toString()))
                            .orElse("None.");
                    String notifer = clickedBlock.getNotifier()
                            .map(manager::get)
                            .map(CompletableFuture::join)
                            .map(profile->profile.getName().orElse(profile.getUniqueId().toString()))
                            .orElse("None.");
                    player.sendMessage(formatText("&7Owner: &d%s\n&8Notifier: &5%s", owner, notifer));
                }).submit(FtbIntegration.INSTANCE);
    }
}
