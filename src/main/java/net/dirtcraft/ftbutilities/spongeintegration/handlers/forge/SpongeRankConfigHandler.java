package net.dirtcraft.ftbutilities.spongeintegration.handlers.forge;

import com.feed_the_beast.ftblib.lib.config.*;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.ranks.Ranks;
import com.mojang.authlib.GameProfile;
import net.dirtcraft.ftbutilities.spongeintegration.utility.Pair;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.model.PermissionHolder;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpongeRankConfigHandler implements IRankConfigHandler {
    private final LuckPerms api  = LuckPermsProvider.get();
    private final String CLAIMS = FTBUtilitiesPermissions.CLAIMS_MAX_CHUNKS;
    private final Map<String, Function<UUID, ConfigValue>> overrides = Stream.of(
            new Pair<String, Function<UUID, ConfigValue>>(CLAIMS, uuid->getIntFromMeta(uuid, CLAIMS))
    ).collect(Collectors.toMap(Pair::getKey, Pair::getValue));


    @Override
    public void registerRankConfig(RankConfigValueInfo info)
    {
        DefaultRankConfigHandler.INSTANCE.registerRankConfig(info);
    }

    @Override
    public Collection<RankConfigValueInfo> getRegisteredConfigs()
    {
        return DefaultRankConfigHandler.INSTANCE.getRegisteredConfigs();
    }

    @Override
    public ConfigValue getConfigValue(MinecraftServer server, GameProfile profile, String node) {
        ConfigValue value = ConfigNull.INSTANCE;

        if (overrides.containsKey(node)) value = overrides.get(node).apply(profile.getId());
        else if (Ranks.isActive()) value = Ranks.INSTANCE.getPermission(profile, node, true);

        return value.isNull() ? DefaultRankConfigHandler.INSTANCE.getConfigValue(server, profile, node) : value;
    }

    @Nullable
    @Override
    public RankConfigValueInfo getInfo(String node)
    {
        return DefaultRankConfigHandler.INSTANCE.getInfo(node);
    }

    private ConfigValue getIntFromMeta(UUID uuid, String node){
        return Optional.ofNullable(api.getUserManager().getUser(uuid))
                .map(PermissionHolder::getCachedData)
                .map(CachedDataManager::getMetaData)
                .map(meta->meta.getMetaValue(node))
                .filter(s->s.matches("\\d+"))
                .map(Integer::parseInt)
                .<ConfigValue>map(ConfigInt::new)
                .orElse(ConfigNull.INSTANCE);
    }
}
