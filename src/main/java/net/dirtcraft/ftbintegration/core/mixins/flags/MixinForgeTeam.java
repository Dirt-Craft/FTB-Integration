package net.dirtcraft.ftbintegration.core.mixins.flags;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import net.dirtcraft.ftbintegration.core.api.DebugTeamInfo;
import net.dirtcraft.ftbintegration.core.api.FlagTeamInfo;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeTeam.class, remap = false)
public abstract class MixinForgeTeam implements FlagTeamInfo {


    @Unique private final String flags$BLOCK_MOB_SPAWN_KEY = "spongeintegration:blockmobspawns";
    @Unique private boolean flags$blockMobSpawns = false;

    @Shadow public abstract void markDirty();

    @Override
    public boolean blockMobSpawns(){
        return flags$blockMobSpawns;
    }

    public void setBlockMobSpawns(boolean value){
        flags$blockMobSpawns = value;
        if (this instanceof DebugTeamInfo) ((DebugTeamInfo) this).regenerateDebugTitle();
        this.markDirty();
    }

    @Inject(method = "serializeNBT", at = @At(value = "TAIL"))
    private void onSerialize(CallbackInfoReturnable<NBTTagCompound> cir){
        NBTTagCompound nbt = cir.getReturnValue();
        nbt.setBoolean(flags$BLOCK_MOB_SPAWN_KEY, flags$blockMobSpawns);
    }

    @Inject(method = "deserializeNBT", at = @At(value = "TAIL"))
    private void onDeserialize(NBTTagCompound nbt, CallbackInfo ci){
        flags$blockMobSpawns = nbt.hasKey(flags$BLOCK_MOB_SPAWN_KEY) && nbt.getBoolean(flags$BLOCK_MOB_SPAWN_KEY);
    }




}
