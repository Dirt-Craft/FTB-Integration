package net.dirtcraft.ftbintegration.core.mixins.badges;

import com.feed_the_beast.ftbutilities.data.FTBUtilitiesUniverseData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = FTBUtilitiesUniverseData.class)
public interface FTBUtilitiesUniverseDataAccessor {
    @Accessor("BADGE_CACHE")
    static Map<UUID, String> getBADGE_CACHE(){
        throw new AssertionError();
    }
}
