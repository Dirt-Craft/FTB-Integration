package net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge;

import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerData;
import net.dirtcraft.ftbutilities.spongeintegration.utility.CauseContextHelper;
import net.dirtcraft.ftbutilities.spongeintegration.utility.ClaimedChunkHelper;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

public class EntityEventHandler {
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityAttack(AttackEntityEvent event, @First DamageSource damageSource) {
        if (stopEntityDamage(event, event.getTargetEntity(), event.getCause(), damageSource)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamage(DamageEntityEvent event, @First DamageSource damageSource) {
        if (stopEntityDamage(event, event.getTargetEntity(), event.getCause(), damageSource)) {
            event.setCancelled(true);
        }
    }

    public boolean stopEntityDamage(Event event, Entity targetEntity, Cause cause, DamageSource damageSource) {
        ClaimedChunk claim = ClaimedChunkHelper.getChunk(targetEntity.getLocation());
        if (claim == null && !(targetEntity instanceof Player)) return false;

        User user = CauseContextHelper.getEventUser(event);
        EntityDamageSource entityDamageSource;
        final TileEntity tileEntity = cause.first(TileEntity.class).orElse(null);
        if (tileEntity == null && damageSource instanceof EntityDamageSource) {
            entityDamageSource = (EntityDamageSource) damageSource;
            Entity source = entityDamageSource.getSource();
            if (entityDamageSource instanceof IndirectEntityDamageSource) {
                source = ((IndirectEntityDamageSource) entityDamageSource).getIndirectSource();
            }
            if (source instanceof Player && user == null) {
                user = (User) source;
            }
        }

        return !ClaimedChunkHelper.canAttackEntity(PlayerData.getOrCreate(user), damageSource, targetEntity);
    }

}
