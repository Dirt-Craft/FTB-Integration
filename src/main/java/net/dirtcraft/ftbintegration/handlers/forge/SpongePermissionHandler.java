package net.dirtcraft.ftbintegration.handlers.forge;

import com.mojang.authlib.GameProfile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.server.permission.DefaultPermissionHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.context.IContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

public enum SpongePermissionHandler implements IPermissionHandler {
    INSTANCE;
    private final HashSet<String> defaultNodes = new HashSet<>();
    private final LuckPerms lp = LuckPermsProvider.get();

    @Override
    public boolean hasPermission(@Nonnull GameProfile profile, @Nonnull String node, @Nullable IContext context) {
        return hasPermission(profile, context, node);
    }

    public boolean hasPermission(@Nonnull GameProfile profile, @Nullable IContext context, @Nonnull String... node) {
        if (profile.getId() == null) {
            if (profile.getName() == null) return false;
            else profile = new GameProfile(EntityPlayer.getOfflineUUID(profile.getName()), profile.getName());
        }
        User user = lp.getUserManager().getUser(profile.getId());

        if (user == null || node.length == 0) return false;
        Tristate permission = user.getCachedData()
                .getPermissionData()
                .checkPermission(String.join(".", node));

        switch (permission) {
            case TRUE: return true;
            case FALSE: return false;
            default: return defaultNodes.contains(node[0]);
        }
    }

    @Override
    public void registerNode(@Nonnull String node, @Nonnull DefaultPermissionLevel level, @Nonnull String desc) {
        if (level == DefaultPermissionLevel.ALL) {

            defaultNodes.add(node);
        }
        DefaultPermissionHandler.INSTANCE.registerNode(node, level, desc);
    }

    @Override
    public Collection<String> getRegisteredNodes() {
        return DefaultPermissionHandler.INSTANCE.getRegisteredNodes();
    }

    @Override
    public String getNodeDescription(@Nonnull String node) {
        return DefaultPermissionHandler.INSTANCE.getNodeDescription(node);
    }
}
