package net.dirtcraft.ftbintegration.utility;

import net.minecraft.entity.IEntityOwnable;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;

import java.util.UUID;

public class CauseContextHelper {

    public static User getEventUser(Event event) {

        if (event.getSource() instanceof Player && !(event.getSource() instanceof FakePlayer)) return (User) event.getSource();
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        if (context.containsKey(EventContextKeys.LEAVES_DECAY)) return null;

        User user = cause.first(User.class).orElse(null);
        User fakePlayer = user instanceof FakePlayer? user : null;

        if (context.containsKey(EventContextKeys.FIRE_SPREAD)) {
            return context.get(EventContextKeys.NOTIFIER).orElse(null);
        }

        if (user == null || fakePlayer != null) {
            if (cause.containsType(TileEntity.class)) {
                user = context.get(EventContextKeys.OWNER)
                        .orElse(context.get(EventContextKeys.NOTIFIER)
                                .orElse(context.get(EventContextKeys.CREATOR)
                                        .orElse(null)));
            } else {
                user = context.get(EventContextKeys.NOTIFIER)
                        .orElse(context.get(EventContextKeys.OWNER)
                                .orElse(context.get(EventContextKeys.CREATOR)
                                        .orElse(null)));
            }
        }

        if (user == null) {
            user = cause.first(Entity.class)
                    .filter(IEntityOwnable.class::isInstance)
                    .map(IEntityOwnable.class::cast)
                    .map(CauseContextHelper::fromEntity)
                    .orElse(null);
        }

        if (user == null) {
            user = fakePlayer;
            if (event instanceof ExplosionEvent) {
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living instanceof User) {
                    user = (User) living;
                }
            }
        }

        return user;
    }

    private static User fromEntity(IEntityOwnable entity){
            UUID id = entity.getOwnerId();
            if (id == null) return null;

            User u = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(id).orElse(null);
            if (u != null && entity instanceof OwnershipTrackedBridge)((OwnershipTrackedBridge)entity).tracked$setOwnerReference(u);

            return u;
    }
}