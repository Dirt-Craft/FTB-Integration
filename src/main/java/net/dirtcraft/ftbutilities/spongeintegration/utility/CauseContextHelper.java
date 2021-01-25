/*
 * This file is part of GriefPrevention, licensed under the MIT License (MIT).
 *
 * Copyright (c) bloodmc
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.dirtcraft.ftbutilities.spongeintegration.utility;

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
        if (context.containsKey(EventContextKeys.LEAVES_DECAY)) {
            return null;
        }

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