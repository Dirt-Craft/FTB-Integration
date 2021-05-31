package net.dirtcraft.ftbintegration.core.mixins.flags;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.TeamData;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesTeamData;
import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.core.api.FlagTeamInfo;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FTBUtilitiesTeamData.class, remap = false)
public abstract class MixinFTBUtilitiesTeamData extends TeamData {
    public MixinFTBUtilitiesTeamData(ForgeTeam t) {
        super(t);
    }

    @Inject(method = "addConfig", at = @At(value = "TAIL"))
    private void onAddConfig(ConfigGroup main, CallbackInfo ci) {
        String id = FtbIntegration.MODID.replace("-", "_");
        ConfigGroup group = main.getGroup(id);
        group.setDisplayName(new TextComponentString(FtbIntegration.NAME));
        FlagTeamInfo team = (FlagTeamInfo)this.team;

        group.addBool("block_mob_spawns", team::blockMobSpawns, team::setBlockMobSpawns, false);
        group.addEnum("allow_entry", team::allowEntry, team::setAllowEntryRank, EnumTeamStatus.NAME_MAP);
    }
}
