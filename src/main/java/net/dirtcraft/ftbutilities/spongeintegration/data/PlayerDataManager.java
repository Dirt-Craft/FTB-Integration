package net.dirtcraft.ftbutilities.spongeintegration.data;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.authlib.GameProfile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerDataManager {
    static final PlayerDataManager INSTANCE = new PlayerDataManager();
    private final Map<User, PlayerData> userDataMap = new HashMap<>();
    private final Map<GameProfile, PlayerData> profileDataMap = new HashMap<>();
    private final ArrayListMultimap<ForgeTeam, PlayerData> loadedByTeam = ArrayListMultimap.create();
    private final UserStorageService userStorageService = Sponge.getServiceManager()
            .provideUnchecked(UserStorageService.class);

    public static PlayerDataManager getInstance(){
        return INSTANCE;
    }

    public PlayerData getOrCreate(User user){
        PlayerData ret = userDataMap.get(user);
        if (ret == null && user != null) ret = new PlayerData(user);
        return ret;
    }

    public PlayerData get(User user){
        return userDataMap.get(user);
    }

    public PlayerData get(GameProfile user){
        return profileDataMap.get(user);
    }

    public void loadUser(User user){
        PlayerData player = new PlayerData(user);
        ForgeTeam team = player.getForgeTeam();
        if (team == null){
            userDataMap.put(user, player);
            profileDataMap.put(player.getGameProfile(), player);
            return;
        } else if (loadedByTeam.containsKey(team)) return;
        List<PlayerData> teamMates = team.players.keySet().stream()
                .map(fp->fp.profile)
                .map(this::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::getOrCreate)
                .collect(Collectors.toList());
        loadedByTeam.putAll(team, teamMates);
        teamMates.forEach(pd->userDataMap.put(pd.getUser(), pd));
        teamMates.forEach(pd->profileDataMap.put(pd.getGameProfile(), pd));
    }

    public void unloadUser(User user){
        PlayerData player = userDataMap.get(user);
        ForgeTeam team = player.getForgeTeam();
        if (team == null || loadedByTeam.containsKey(team)) userDataMap.remove(user);
        else if (loadedByTeam.get(team).stream().noneMatch(PlayerData::isOnline)) {
            loadedByTeam.get(team).forEach(pd->{
                userDataMap.remove(pd.getUser());
                profileDataMap.remove(pd.getGameProfile());
            });
            loadedByTeam.removeAll(team);
        }
    }

    private Optional<User> getUser(GameProfile profile){
        return userStorageService.get(profile.getId());
    }

}
