package net.dirtcraft.ftbintegration.core;

import net.dirtcraft.ftbintegration.FtbIntegration;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class MixinLoader implements IFMLLoadingPlugin {
    public static final Logger LOG = LogManager.getLogger(FtbIntegration.MODID);
    List<ModPatch> modPatches = new ArrayList<>();

    public MixinLoader() {
        ModPatch.builder()
                .setName("Pneumatic-Craft")
                .setConfig("mixins.patches.pneumaticcraft.json")
                .addRequiredMod("\\Qpneumaticcraft-repressurized-1.12.2-\\E.*\\.jar")
                .build(modPatches);
        ModPatch.builder()
                .setName("FTB-Utilities")
                .setConfig("mixins.ftbutilities.json")
                .addRequiredMod("\\QFTBLib-\\E\\d.*\\.jar")
                .addRequiredMod("\\QFTBUtilities-\\E\\d.*\\.jar")
                .build(modPatches);


        MixinBootstrap.init();
        for (File file : new File("mods").listFiles()){
            for (ModPatch patch : modPatches) patch.checkRequirements(file);
        }

        for (ModPatch patch : modPatches) patch.tryApply();
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[0];
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {

    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
