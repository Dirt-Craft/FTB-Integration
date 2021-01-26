package net.dirtcraft.ftbutilities.spongeintegration.data;

import org.spongepowered.api.entity.living.player.User;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {
    static final PlayerDataManager INSTANCE = new PlayerDataManager();
    private final Map<User, PlayerData> playerData = new HashMap<>();

    public static PlayerDataManager getInstance(){
        return INSTANCE;
    }

    public PlayerData getData(User user){
        PlayerData ret = playerData.get(user);
        if (ret == null && user != null) ret = new PlayerData(user);
        return ret;
    }

    public void loadUser(User user){
        playerData.put(user, new PlayerData(user));
    }

    public void unloadUser(User user){
            playerData.remove(user);
    }

}
