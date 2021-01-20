package net.dirtcraft.ftbutilitiesplus.data;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.common.SpongeImpl;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    private static final Map<User, PlayerData> playerData = new HashMap<>();
    private ForgeTeam lastInteractClaim;
    private boolean lastInteractResult;
    private int lastInteractTick;

    public static PlayerData getData(User user){
        return playerData.get(user);
    }

    public void setLastInteractData(ForgeTeam claim) {
        this.lastInteractResult = true;
        this.lastInteractClaim = claim;
        this.lastInteractTick = SpongeImpl.getServer().getTickCounter();
    }

    public boolean checkLastInteraction(ForgeTeam claim, User user) {
        if (this.lastInteractResult && user != null && ((SpongeImpl.getServer().getTickCounter() - this.lastInteractTick) <= 2)) {
            return claim == null || claim.equals(this.lastInteractClaim);
        }
        return false;
    }

    public static class Listener{
        @org.spongepowered.api.event.Listener
        public void onLogin(ClientConnectionEvent.Join event){
            playerData.put(event.getTargetEntity(), new PlayerData());
        }
        @org.spongepowered.api.event.Listener
        public void onLogin(ClientConnectionEvent.Disconnect event){
            playerData.remove(event.getTargetEntity());
        }
    }


}
