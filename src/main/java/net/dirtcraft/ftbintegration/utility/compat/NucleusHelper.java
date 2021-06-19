package net.dirtcraft.ftbintegration.utility.compat;

import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserPreferenceService;
import net.dirtcraft.ftbintegration.utility.SpongeHelper;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;


public interface NucleusHelper {
    static NucleusHelper get(){
        return INTERNAL.INSTANCE;
    }

    default boolean hasSocialSpy(User user){
        return false;
    }

    default Text getName(User user){
        return SpongeHelper.getText(user.getName());
    }

    class Nucleus2 implements NucleusHelper {
        private final NucleusUserPreferenceService.PreferenceKey<Boolean> socialSpy;
        private final NucleusNicknameService nickName;
        private final NucleusUserPreferenceService preferences;
        private Nucleus2(){
            this.preferences = NucleusAPI.getUserPreferenceService();
            this.nickName = NucleusAPI.getNicknameService().orElse(null);
            this.socialSpy = preferences.keys().socialSpyEnabled().orElse(null);
        }

        @Override
        public boolean hasSocialSpy(User user){
            return preferences.getPreferenceFor(user, socialSpy).orElse(false);
        }

        @Override
        public Text getName(User user){
            if (nickName == null) return SpongeHelper.getText(user.getName());
            else return nickName.getNickname(user)
                    .orElse(SpongeHelper.getText(user.getName()));
        }
    }

    class INTERNAL{
        private static final NucleusHelper INSTANCE = getINSTANCE();
        private static NucleusHelper getINSTANCE(){
            try {
                return new Nucleus2();
            } catch (Exception e){
                return new NucleusHelper() {};
            }
        }
    }
}
