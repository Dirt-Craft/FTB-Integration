package net.dirtcraft.ftbintegration.utility.compat;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface CrateHelper {
    CrateHelper INSTANCE = Internal.getInstance();

    default boolean isHuskyCrate(Location<World> location){
        return false;
    }

    class Husky2Impl implements CrateHelper {
        @Override
        public boolean isHuskyCrate(Location<World> location){
            return location != null && HuskyCrates.registry.isPhysicalCrate(location);
        }
    }

    class Internal {
        private static CrateHelper getInstance(){
            try {
                Class<?> clazz = Class.forName("com.codehusky.huskycrates.HuskyCrates");
                clazz.getDeclaredField("registry");
                clazz = Class.forName("com.codehusky.huskycrates.Registry");
                clazz.getDeclaredMethod("isPhysicalCrate", Location.class);
                return new Husky2Impl();
            } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
                return new CrateHelper() {};
            }
        }
    }
}
