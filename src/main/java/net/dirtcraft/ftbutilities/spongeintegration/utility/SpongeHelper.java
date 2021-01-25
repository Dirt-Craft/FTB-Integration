package net.dirtcraft.ftbutilities.spongeintegration.utility;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.world.LocationBridge;

import javax.annotation.Nullable;

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
}
