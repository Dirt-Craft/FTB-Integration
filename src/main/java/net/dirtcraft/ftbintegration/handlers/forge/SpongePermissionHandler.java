package net.dirtcraft.ftbintegration.handlers.forge;

import com.mojang.authlib.GameProfile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.server.permission.DefaultPermissionHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.context.IContext;
import org.spongepowered.api.Sponge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Function;

public enum SpongePermissionHandler implements IPermissionHandler {
    INSTANCE;
    private final LuckPerms lp = LuckPermsProvider.get();
    private final ImmutableContextSet contexts = lp.getContextManager().getStaticContext();
    private final HashSet<String> defaultNodes = new HashSet<>();

    @Override
    public boolean hasPermission(@Nonnull GameProfile profile, @Nonnull String node, @Nullable IContext context) {
        return hasPermission(profile, node);
    }

    public boolean hasPermission(@Nonnull GameProfile profile, @Nonnull String... node) {
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
        if (level == DefaultPermissionLevel.ALL && !node.startsWith("ftbutilities.claims.item")) {
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

    public String getMeta(User user, String key){
        if (user == null) return null;
        else return user.getCachedData()
                .getMetaData()
                .getMetaValue(key);
    }

    public <T> T getMetaOrDefault(UUID uuid, String key, Function<String, T> mapper, T def){
        try {
            User user = lp.getUserManager().getUser(uuid);
            if (user == null) return def;
            String val = getMeta(user, key);
            if (val == null) return def;
            else return mapper.apply(val);
        } catch (Exception e){
            return def;
        }
    }

    public <T> T getMetaOrDefault(User user, String key, Function<String, T> mapper, T def){
        try {
            String val = getMeta(user, key);
            if (val == null) return def;
            else return mapper.apply(val);
        } catch (Exception e){
            return def;
        }
    }

    public String getServerContext(){
        return contexts.getAnyValue("server").orElse("global");
    }

    public void setGroupMeta(String group, String node, String value){
        String command = String.format("lp group %s meta set %s %s %s", group, node, value, getServerContext());
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
    }
}
