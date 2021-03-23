package net.dirtcraft.ftbintegration.core;

import net.dirtcraft.ftbintegration.utility.Pair;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModPatch {
    public static Builder builder(){
        return new Builder();
    }

    private final String name;
    private final String config;
    private final Map<String, Boolean> mods;
    private final List<File> jars;

    private ModPatch(String name, String config, List<String> mods){
        this.jars = new ArrayList<>();
        this.name = name;
        this.config = config;
        this.mods = mods.stream()
                .map(m->new Pair<>(m, false))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public void checkRequirements(File jar) {
        String name = jar.getName();
        mods.keySet().stream()
                .filter(name::matches)
                .forEach(mod->{
                    mods.put(mod, true);
                    jars.add(jar);
                });
    }

    public void tryApply() {
        try {
            if (mods.values().stream().allMatch(Boolean::booleanValue)) {
                for (File file : jars) loadModJar(file);
                Mixins.addConfiguration(config);
                String msg = String.format("§aENABLED MIXIN PATCH -> %s §7@[%s]", name, getJarsString());
                MixinLoader.LOG.info(msg);
            } else {
                String msg = String.format("§cDISABLED MIXIN PATCH -> %s §7@[%s]", name, getJarsString());
                MixinLoader.LOG.info(msg);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private String getJarsString(){
        if (!jars.isEmpty()) {
            List<String> fileNames = this.jars.stream()
                    .map(File::getName)
                    .collect(Collectors.toList());
            return String.join(", ", fileNames);
        } else return "N/A";
    }

    private void loadModJar(File jar) throws Exception {
        ((LaunchClassLoader) this.getClass().getClassLoader()).addURL(jar.toURI().toURL());
        CoreModManager.getReparseableCoremods().add(jar.getName());
    }


    public static class Builder {
        private final List<String> mods = new ArrayList<>();
        private String config;
        private String name;

        private Builder(){

        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setConfig(String config) {
            this.config = config;
            return this;
        }

        public Builder addRequiredMod(String regex) {
            mods.add(regex);
            return this;
        }

        public ModPatch build(){
            return new ModPatch(name == null? config: name, config, mods);
        }

        public ModPatch build(List<ModPatch> list) {
            ModPatch patch = this.build();
            list.add(patch);
            return patch;
        }
    }
}
