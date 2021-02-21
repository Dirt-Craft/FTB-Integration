package net.dirtcraft.ftbutilities.spongeintegration.data;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.authlib.GameProfile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataManager {
    static final PlayerDataManager INSTANCE = new PlayerDataManager();
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final ArrayListMultimap<ForgeTeam, PlayerData> loadedByTeam = ArrayListMultimap.create();
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
        return playerDataMap.get(user.getUniqueId());
    }

    public PlayerData get(GameProfile user){
        return playerDataMap.get(user.getId());
    }

    public void loadUser(User user){
        PlayerData player = new PlayerData(user);
        ForgeTeam team = player.getForgeTeam();
        if (team == null){
            playerDataMap.put(user.getUniqueId(), player);
            return;
        } else if (loadedByTeam.containsKey(team)) return;
        List<PlayerData> teamMates = team.getMembers().stream()
                .map(fp->fp.profile)
                .map(this::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::getOrCreate)
                .collect(Collectors.toList());
        loadedByTeam.putAll(team, teamMates);
        teamMates.forEach(pd->playerDataMap.put(pd.getUser().getUniqueId(), pd));
    }

    //todo test this shit works?
    public void unloadUser(User user){
        PlayerData player = playerDataMap.get(user.getUniqueId());
        ForgeTeam team = player.getForgeTeam();
        if (team == null || !loadedByTeam.containsKey(team)) playerDataMap.remove(user.getUniqueId());
        else if (loadedByTeam.get(team).stream().noneMatch(PlayerData::isOnline)) {
            loadedByTeam.get(team).forEach(pd->{
                playerDataMap.remove(pd.getUser().getUniqueId());
            });
            loadedByTeam.removeAll(team);
        }
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
