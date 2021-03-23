package net.dirtcraft.ftbintegration.core.mixins.chunks;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.TeamData;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesTeamData;
import net.dirtcraft.ftbintegration.core.api.ChunkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FTBUtilitiesTeamData.class, remap = false)
public abstract class MixinFTBUtilitiesTeamData extends TeamData {

    public MixinFTBUtilitiesTeamData(ForgeTeam t) {
        super(t); throw new AssertionError();
    }

    @Shadow private int cachedMaxClaimChunks;
    @Shadow private int cachedMaxChunkloaderChunks;

    /**
     * @author need annotation to shush
     */
    @Overwrite
    public int getMaxClaimChunks() {

        if (!ClaimedChunks.isActive()) {
            return -1;
        } else if (!this.team.isValid()) {
            return -2;
        } else if (this.cachedMaxClaimChunks >= 0) {
            return this.cachedMaxClaimChunks;
        } else {
            this.cachedMaxClaimChunks = 0;

            for (ForgePlayer player : team.getMembers()) {
                cachedMaxClaimChunks += ((ChunkPlayerInfo)player).getTotalClaims();
            }

            return this.cachedMaxClaimChunks;
        }
    }

    /**
     * @author need annotation to shush
     */
    @Overwrite
    public int getMaxChunkloaderChunks() {
        if (!ClaimedChunks.isActive()) {
            return -1;
        } else if (!this.team.isValid()) {
            return -2;
        } else if (this.cachedMaxChunkloaderChunks >= 0) {
            return this.cachedMaxChunkloaderChunks;
        } else {
            this.cachedMaxChunkloaderChunks = 0;

            for (ForgePlayer player : team.getMembers()) {
                cachedMaxChunkloaderChunks += ((ChunkPlayerInfo)player).getTotalLoaders();
            }

            return this.cachedMaxChunkloaderChunks;
        }
    }

}
