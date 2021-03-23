package net.dirtcraft.ftbintegration.core.mixins.patches.bypass.pneumaticcraft;

import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.data.PlayerDataManager;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntitySecurityStation.class, remap = false)
public class MixinTileEntitySecurityStation {
    @Inject(method = "isPlayerOnWhiteList", at = @At("HEAD"), cancellable = true)
    public void isWhiteList(EntityPlayer player, CallbackInfoReturnable<Boolean> cir){
        try {
            PlayerData data = PlayerDataManager.getInstance().get(player.getGameProfile());
            if (data != null && data.canBypassClaims()) cir.setReturnValue(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
