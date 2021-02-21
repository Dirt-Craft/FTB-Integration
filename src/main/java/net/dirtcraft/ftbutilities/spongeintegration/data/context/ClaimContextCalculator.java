package net.dirtcraft.ftbutilities.spongeintegration.data.context;

import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

import java.util.Set;

public class ClaimContextCalculator implements ContextCalculator<Subject> {
    public static final String KEY = "claim";

    public static void register(){
        Sponge.getServiceManager().provideUnchecked(PermissionService.class).registerContextCalculator(new ClaimContextCalculator());
    }

    public String getContext(Player player){
        PlayerData data = PlayerData.get(player);
        if (data == null) return "unknown";
        else return data.getClaimStandingIn();
    }

    @Override
    public void accumulateContexts(Subject target, Set<Context> accumulator) {
        Object player = target.getCommandSource().orElse(null);
        if (!(player instanceof Player)) return;
        Context context = new Context(KEY,getContext((Player)player));
        accumulator.add(context);
    }


    @Override
    public boolean matches(Context context, Subject target) {
        Object player = target.getCommandSource().orElse(null);
        if (!context.getKey().equals(KEY) || !(player instanceof Player)) return false;
        return context.getValue().equals(getContext((Player)player));
    }
}
