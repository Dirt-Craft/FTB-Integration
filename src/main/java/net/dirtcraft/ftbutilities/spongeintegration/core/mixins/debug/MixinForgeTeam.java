package net.dirtcraft.ftbutilities.spongeintegration.core.mixins.debug;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.util.FinalIDObject;
import net.dirtcraft.ftbutilities.spongeintegration.core.api.DebugTeamInfo;
import net.dirtcraft.ftbutilities.spongeintegration.core.api.FlagTeamInfo;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ForgeTeam.class, remap = false)
public abstract class MixinForgeTeam extends FinalIDObject implements DebugTeamInfo {

    public MixinForgeTeam(String _id, int flags) {
        super(_id, flags);
    }

    @Unique private ITextComponent debugTitle = null;

    @Shadow public ForgePlayer owner;

    @Shadow public abstract ITextComponent getTitle();
    @Shadow public abstract List<ForgePlayer> getMembers();

    @Inject(method = "setTitle", at = @At(value = "TAIL"))
    private void onSetTitle(String s, CallbackInfo ci){
        regenerateDebugTitle();
    }

    @Inject(method = "addMember", at = @At(value = "TAIL"))
    private void onAddMember(ForgePlayer player, boolean simulate, CallbackInfoReturnable<Boolean> cir){
        regenerateDebugTitle();
    }

    @Inject(method = "removeMember", at = @At(value = "TAIL"))
    private void onRemoveMember(ForgePlayer player, CallbackInfoReturnable<Boolean> cir){
        regenerateDebugTitle();
    }

    @Inject(method = "setStatus", at = @At(value = "TAIL"))
    private void onSetStatus(ForgePlayer player, EnumTeamStatus status, CallbackInfoReturnable<Boolean> cir){
        regenerateDebugTitle();
    }

    @Override
    public ITextComponent getDebugTitle() {
        if (debugTitle == null) debugTitle = generateDebugTitle();
        return debugTitle;
    }

    @Override
    public void regenerateDebugTitle(){
        this.debugTitle = generateDebugTitle();
    }

    @Unique
    private ITextComponent generateDebugTitle() {
        if (owner == null) return getTitle().createCopy().appendText("\n[Server Team]");
        ITextComponent textComponent = getTitle().createCopy()
                .appendText("\nTeam ID: ")
                .appendText(getId());
        if (!getMembers().isEmpty()){
            textComponent.appendText("\nMembers: ");
            for (ForgePlayer fp : getMembers()){
                textComponent.appendText("\n - ");
                textComponent.appendText(fp.getName());
                if (fp == owner) textComponent.appendText(" [Owner]");
            }
        }
        if (this instanceof FlagTeamInfo){
            FlagTeamInfo flagTeamInfo = (FlagTeamInfo) this;
            textComponent.appendText("\nFlags:");
            textComponent.appendText("\n - Mob Spawn: ");
            textComponent.appendText(String.valueOf(!flagTeamInfo.blockMobSpawns()));
        }
        return textComponent;
    }


}
