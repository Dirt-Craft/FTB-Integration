package net.dirtcraft.ftbutilitiesplus.handlers;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.server.permission.DefaultPermissionHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.context.IContext;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class SpongePermissionHandler implements IPermissionHandler {
    public static final SpongePermissionHandler INSTANCE = new SpongePermissionHandler();
    private final UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);

    @Override
    public boolean hasPermission(@Nonnull GameProfile profile, @Nonnull String node, @Nullable IContext context) {
        if (profile.getId() == null) {
            if (profile.getName() == null) return false;
            else profile = new GameProfile(EntityPlayer.getOfflineUUID(profile.getName()), profile.getName());
        }

        return DefaultPermissionHandler.INSTANCE.hasPermission(profile, node, context) ||
                uss.get(profile.getId())
                .map(u -> u.hasPermission(node))
                .orElse(false);
    }

    @Override
    public void registerNode(@Nonnull String node, @Nonnull DefaultPermissionLevel level, @Nonnull String desc) {
        DefaultPermissionHandler.INSTANCE.registerNode(node, level, desc);
    }

    @Override
    public Collection<String> getRegisteredNodes() {
        return DefaultPermissionHandler.INSTANCE.getRegisteredNodes();
    }

    @Override
    public String getNodeDescription(String node) {
        return DefaultPermissionHandler.INSTANCE.getNodeDescription(node);
    }
}
