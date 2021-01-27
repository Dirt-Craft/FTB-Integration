package net.dirtcraft.ftbutilities.spongeintegration.handlers.forge;

import com.feed_the_beast.ftblib.lib.config.*;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.ranks.Ranks;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Collection;

public class SpongeRankConfigHandler implements IRankConfigHandler {
    private final String CLAIMS = FTBUtilitiesPermissions.CLAIMS_MAX_CHUNKS;


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
    public ConfigValue getConfigValue(MinecraftServer server, GameProfile profile, String node)
    {
        ConfigValue value = ConfigNull.INSTANCE;

        if (Ranks.isActive())
        {
            value = Ranks.INSTANCE.getPermission(profile, node, true);
        }

        return value.isNull() ? DefaultRankConfigHandler.INSTANCE.getConfigValue(server, profile, node) : value;
    }

    @Nullable
    @Override
    public RankConfigValueInfo getInfo(String node)
    {
        return DefaultRankConfigHandler.INSTANCE.getInfo(node);
    }
}
