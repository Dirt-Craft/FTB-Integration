package net.dirtcraft.ftbintegration.core.mixins.debug;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import net.dirtcraft.ftbintegration.core.api.DebugPlayerInfo;
import net.dirtcraft.ftbintegration.utility.NbtHelper;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgePlayer.class, remap = false)
public abstract class MixinForgePlayer implements DebugPlayerInfo {

    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;

    private static final String SEEN_KEY = "ftb-integration:seen";

    @Shadow public abstract boolean isOnline();

    @Unique private long lastSeenMs = 0;

    @Override
    public void updateLastSeenMs() {
        this.lastSeenMs = System.currentTimeMillis();
    }

    @Override
    public long getLastSeenMs() {
        return this.isOnline()? 0 : System.currentTimeMillis() - lastSeenMs;
    }

    @Inject(method = "serializeNBT", at = @At("TAIL"))
    private void onSerialize(CallbackInfoReturnable<NBTTagCompound> cir){
        NBTTagCompound nbt = cir.getReturnValue();
        nbt.setLong(SEEN_KEY, lastSeenMs);
    }

    @Inject(method = "deserializeNBT", at = @At("TAIL"))
    private void onDeserialize(NBTTagCompound nbt, CallbackInfo ci){
        lastSeenMs = NbtHelper.getOrDefault(nbt, SEEN_KEY, 0L);
    }

    public String getElapsedString() {
        long time = getLastSeenMs();
        if (lastSeenMs == 0) return "(?)";
        else if (time < DAY) return "(0D)";
        else return String.format("(%dD)", time/DAY);
    }
}
