package net.dirtcraft.ftbintegration.core.mixins.generic;

import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.core.api.CompatClaimedChunks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(value = ClaimedChunks.class, remap = false)
public abstract class AccessorClaimedChunks implements CompatClaimedChunks {
    @Shadow @Final private Map<ChunkDimPos, ClaimedChunk> map;

    @Shadow public abstract void removeChunk(ChunkDimPos pos);

    @Unique @Override
    public boolean compatUnclaimChunk(ChunkDimPos pos) {
        ClaimedChunk chunk = map.get(pos);
        if (chunk != null && !chunk.isInvalid()) {
            chunk.setLoaded(false);
            removeChunk(pos);
            return true;
        } else return false;
    }
}
