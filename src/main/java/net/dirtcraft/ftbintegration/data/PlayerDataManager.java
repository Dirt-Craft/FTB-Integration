package net.dirtcraft.ftbintegration.data;

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerDataManager {
    static final PlayerDataManager INSTANCE = new PlayerDataManager();
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private UserStorageService userStorageService;

    public static PlayerDataManager getInstance(){
        return INSTANCE;
    }

    public PlayerData getOrCreate(User user){
        if (user == null) return null;
        PlayerData ret = playerDataMap.get(user.getUniqueId());
        if (ret == null) ret = new PlayerData(user);
        return ret;
    }

    public PlayerData get(User user){
        if (user == null) return null;
        return playerDataMap.get(user.getUniqueId());
    }

    public PlayerData get(GameProfile user){
        return playerDataMap.get(user.getId());
    }

    public PlayerData loadUser(User user){
        PlayerData data = new PlayerData(user);
        playerDataMap.put(user.getUniqueId(), data);
        return data;
    }

    public void unloadUser(User user){
        playerDataMap.remove(user.getUniqueId());
    }

    private UserStorageService getUserStorageService(){
        if (this.userStorageService == null) userStorageService = Sponge.getServiceManager()
                .provideUnchecked(UserStorageService.class);
        return userStorageService;
    }

    private Optional<User> getUser(GameProfile profile){
        return getUserStorageService().get(profile.getId());
    }

}
