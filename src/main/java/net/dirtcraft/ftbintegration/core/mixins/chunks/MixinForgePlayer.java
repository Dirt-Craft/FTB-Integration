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

    private static final String CLAIM_KEY = "ftb-integration:claims";
    private static final String LOADER_KEY = "ftb-integration:loaders";
    @Unique private int claims = 100;
    @Unique private int loaders = 0;

    @Unique
    @Override
    public int getClaims() {
        return claims;
    }

    @Unique
    @Override
    public int getLoaders() {
        return loaders;
    }

    @Unique
    @Override
    public void setClaims(int amount) {
        this.claims = amount;
        this.markDirty();
        if (team != null) team.clearCache();
    }

    @Unique
    @Override
    public void setLoaders(int amount) {
        this.loaders = amount;
        this.markDirty();
        if (team != null) team.clearCache();
    }

    @Unique
    @Override
    public void loadChunkData(){
        SpongePermissionHandler handler = SpongePermissionHandler.INSTANCE;
        int chunks = handler.getMetaOrDefault(getId(), Permission.CHUNK_CLAIM_META, Integer::valueOf, 100);
        int loaders = handler.getMetaOrDefault(getId(), Permission.CHUNK_LOADER_META, Integer::valueOf, 0);
        setClaims(chunks);
        setLoaders(loaders);
    }

    @Inject(method = "serializeNBT", at = @At("TAIL"))
    private void onSerialize(CallbackInfoReturnable<NBTTagCompound> cir){
        NBTTagCompound nbt = cir.getReturnValue();
        nbt.setInteger(CLAIM_KEY, claims);
        nbt.setInteger(LOADER_KEY, loaders);
    }

    @Inject(method = "deserializeNBT", at = @At("TAIL"))
    private void onDeserialize(NBTTagCompound nbt, CallbackInfo ci){
        claims = NbtHelper.getOrDefault(nbt, CLAIM_KEY, 100);
        loaders = NbtHelper.getOrDefault(nbt, LOADER_KEY, 0);
    }
}
