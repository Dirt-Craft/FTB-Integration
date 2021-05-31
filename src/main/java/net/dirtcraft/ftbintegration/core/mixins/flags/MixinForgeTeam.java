package net.dirtcraft.ftbintegration.core.mixins.flags;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
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

import javax.annotation.Nullable;

@Mixin(value = ForgeTeam.class, remap = false)
public abstract class MixinForgeTeam implements FlagTeamInfo {


    @Unique private final String FLAGS_KEY$BLOCK_MOB_SPAWN = "ftb-integration:block-mob-spawns";
    @Unique private final String FLAGS_KEY$EJECT_TO_SPAWN = "ftb-integration:eject-to-spawn";
    @Unique private final String FLAGS_KEY$ALLOW_ENTRY = "ftb-integration:allow-entry";
    @Unique private boolean flags$blockMobSpawns = false;
    @Unique private boolean flags$ejectToSpawn = false;
    @Unique private EnumTeamStatus flags$allowEntry = EnumTeamStatus.NONE;

    @Shadow public abstract void markDirty();

    @Shadow public abstract EnumTeamStatus getHighestStatus(@Nullable ForgePlayer player);

    @Override
    public boolean blockMobSpawns(){
        return flags$blockMobSpawns;
    }

    public void setBlockMobSpawns(boolean value){
        flags$blockMobSpawns = value;
        if (this instanceof DebugTeamInfo) ((DebugTeamInfo) this).regenerateDebugTitle();
        this.markDirty();
    }

    public boolean allowEntry(ForgePlayer player){
        EnumTeamStatus pStat = this.getHighestStatus(player);
        return pStat.isEqualOrGreaterThan(flags$allowEntry);
    }

    public EnumTeamStatus allowEntry() {
        return flags$allowEntry;
    }

    public void setAllowEntryRank(EnumTeamStatus rank) {
        flags$allowEntry = rank;
        if (this instanceof DebugTeamInfo) ((DebugTeamInfo) this).regenerateDebugTitle();
        this.markDirty();
    }

    public boolean ejectEntrantSpawn() {
        return flags$ejectToSpawn;
    }

    public void setEjectEntrantSpawn(boolean val) {
        flags$ejectToSpawn = val;
        if (this instanceof DebugTeamInfo) ((DebugTeamInfo) this).regenerateDebugTitle();
        this.markDirty();
    }

    @Inject(method = "serializeNBT", at = @At("TAIL"))
    private void onSerialize(CallbackInfoReturnable<NBTTagCompound> cir){
        NBTTagCompound nbt = cir.getReturnValue();
        nbt.setBoolean(FLAGS_KEY$BLOCK_MOB_SPAWN, flags$blockMobSpawns);
        nbt.setBoolean(FLAGS_KEY$EJECT_TO_SPAWN, flags$ejectToSpawn);
        nbt.setInteger(FLAGS_KEY$ALLOW_ENTRY, flags$allowEntry.ordinal());
    }

    @Inject(method = "deserializeNBT", at = @At("TAIL"))
    private void onDeserialize(NBTTagCompound nbt, CallbackInfo ci){
        flags$blockMobSpawns = nbt.hasKey(FLAGS_KEY$BLOCK_MOB_SPAWN) && nbt.getBoolean(FLAGS_KEY$BLOCK_MOB_SPAWN);
        flags$ejectToSpawn = nbt.hasKey(FLAGS_KEY$EJECT_TO_SPAWN) && nbt.getBoolean(FLAGS_KEY$EJECT_TO_SPAWN);
        if (nbt.hasKey(FLAGS_KEY$ALLOW_ENTRY)){
            int idx = nbt.getInteger(FLAGS_KEY$ALLOW_ENTRY);
            EnumTeamStatus[] statuses = EnumTeamStatus.values();
            if (idx < 0 || idx >= statuses.length) flags$allowEntry = EnumTeamStatus.NONE;
            else flags$allowEntry = statuses[idx];
        }
    }
}
