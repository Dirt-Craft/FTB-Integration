package net.dirtcraft.ftbintegration.core.mixins.debug;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.util.FinalIDObject;
import net.dirtcraft.ftbintegration.core.api.DebugPlayerInfo;
import net.dirtcraft.ftbintegration.core.api.DebugTeamInfo;
import net.dirtcraft.ftbintegration.core.api.FlagTeamInfo;
import net.dirtcraft.ftbintegration.core.mixins.generic.AccessorFinalIDObject;
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
public abstract class MixinForgeTeam extends FinalIDObject implements DebugTeamInfo, AccessorFinalIDObject {

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
        ITextComponent textComponent = getTitle().createCopy()
                .appendText("\n")
                .appendText("Team ID: ")
                .appendText(getTeamIdString());
        if (owner == null) textComponent.appendText(" [Server]");
        if (!getMembers().isEmpty()){
            textComponent.appendText("\n")
                .appendText("Members: ");
            for (ForgePlayer fp : getMembers()){
                textComponent.appendText("\n")
                        .appendText(" - ")
                        .appendText(fp.getName());
                if (fp == owner) textComponent.appendText("✯");
                if (fp instanceof DebugPlayerInfo) textComponent.appendText(" " + ((DebugPlayerInfo) fp).getElapsedString());
            }
        }
        if (this instanceof FlagTeamInfo){
            FlagTeamInfo flagTeamInfo = (FlagTeamInfo) this;
            String mobSpawn = String.valueOf(!flagTeamInfo.blockMobSpawns());
            textComponent.appendText("\n")
                    .appendText("Flags:")
                    .appendText("\n")
                    .appendText(" - Mob Spawn: ")
                    .appendText(mobSpawn);
        }
        return textComponent;
    }


}
