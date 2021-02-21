package net.dirtcraft.ftbintegration.core.mixins.debug;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftbutilities.net.MessageClaimedChunksUpdate;
import net.dirtcraft.ftbintegration.core.api.DebugTeamInfo;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.data.PlayerDataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MessageClaimedChunksUpdate.class, remap = false)
public abstract class MixinMessageClaimedChunksUpdate {

    @Redirect(method = "<init>(IILnet/minecraft/entity/player/EntityPlayer;)V", at = @At(value = "INVOKE", target = "Lcom/feed_the_beast/ftblib/lib/data/ForgeTeam;getTitle()Lnet/minecraft/util/text/ITextComponent;"))
    public ITextComponent getHoverText(ForgeTeam team, int sx, int sz, EntityPlayer player) {
        PlayerData data = PlayerDataManager.getInstance().get(player.getGameProfile());
        if (data == null || !data.canDebugClaims() || !(team instanceof DebugTeamInfo)) return team.getTitle();
        else return ((DebugTeamInfo) team).getDebugTitle();
    }
}
