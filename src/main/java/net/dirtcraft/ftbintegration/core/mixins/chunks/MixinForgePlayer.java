package net.dirtcraft.ftbintegration.core.mixins.chunks;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import net.dirtcraft.ftbintegration.core.api.ChunkPlayerInfo;
import net.dirtcraft.ftbintegration.handlers.forge.SpongePermissionHandler;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.dirtcraft.ftbintegration.utility.NbtHelper;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = ForgePlayer.class, remap = false)
public abstract class MixinForgePlayer implements ChunkPlayerInfo {
    @Shadow public abstract void markDirty();

    @Shadow public ForgeTeam team;

    @Shadow public abstract UUID getId();

    private static final String CLAIM_BASE_KEY = "ftb-integration:claims-base";
    private static final String LOADER_BASE_KEY = "ftb-integration:loaders-base";
    private static final String CLAIM_ADD_KEY = "ftb-integration:claims-extra";
    private static final String LOADER_ADD_KEY = "ftb-integration:loaders-extra";
    @Unique private int baseClaims = 100;
    @Unique private int baseLoaders = 0;
    @Unique private int addClaims = 0;
    @Unique private int addLoaders = 0;

    @Unique
    @Override
    public int getTotalClaims() {
        return baseClaims + addClaims;
    }

    @Unique
    @Override
    public int getTotalLoaders() {
        return baseLoaders + addLoaders;
    }

    @Unique
    @Override
    public int getAddClaims() {
        return addClaims;
    }

    @Unique
    @Override
    public int getAddLoaders() {
        return addLoaders;
    }

    @Unique
    @Override
    public int getBaseClaims() {
        return baseClaims;
    }

    @Unique
    @Override
    public int getBaseLoaders() {
        return baseLoaders;
    }

    @Unique
    @Override
    public void setBaseChunks(){
        SpongePermissionHandler handler = SpongePermissionHandler.INSTANCE;
        this.baseClaims = handler.getMetaOrDefault(getId(), Permission.CHUNK_CLAIM_META, Integer::valueOf, this.baseClaims);
        this.baseLoaders = handler.getMetaOrDefault(getId(), Permission.CHUNK_LOADER_META, Integer::valueOf, this.baseLoaders);
        this.markDirty();
        if (team != null) team.clearCache();
    }

    @Unique
    @Override
    public void setBaseChunks(int claims, int loaders){
        this.baseClaims = claims;
        this.baseLoaders = loaders;
        this.markDirty();
        if (team != null) team.clearCache();
    }

    @Unique
    @Override
    public void setExtraChunks(int claims, int loaders){
        this.addClaims = claims;
        this.addLoaders = loaders;
        this.markDirty();
        if (team != null) team.clearCache();
    }

    @Unique
    @Override
    public void modifyExtraChunks(int claims, int loaders){
        this.addClaims += claims;
        this.addLoaders += loaders;
        this.markDirty();
        if (team != null) team.clearCache();
    }

    @Inject(method = "serializeNBT", at = @At("TAIL"))
    private void onSerialize(CallbackInfoReturnable<NBTTagCompound> cir){
        NBTTagCompound nbt = cir.getReturnValue();
        nbt.setInteger(CLAIM_BASE_KEY, baseClaims);
        nbt.setInteger(LOADER_BASE_KEY, baseLoaders);
        nbt.setInteger(CLAIM_ADD_KEY, addClaims);
        nbt.setInteger(LOADER_ADD_KEY, addLoaders);
    }

    @Inject(method = "deserializeNBT", at = @At("TAIL"))
    private void onDeserialize(NBTTagCompound nbt, CallbackInfo ci){
        baseClaims = NbtHelper.getOrDefault(nbt, CLAIM_BASE_KEY, 100);
        baseLoaders = NbtHelper.getOrDefault(nbt, LOADER_BASE_KEY, 0);
        addClaims = NbtHelper.getOrDefault(nbt, CLAIM_ADD_KEY, 0);
        addLoaders = NbtHelper.getOrDefault(nbt, LOADER_ADD_KEY, 0);
    }
}
