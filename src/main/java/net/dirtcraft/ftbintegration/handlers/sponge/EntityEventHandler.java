package net.dirtcraft.ftbintegration.handlers.sponge;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import net.dirtcraft.ftbintegration.core.api.FlagTeamInfo;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.CauseContextHelper;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
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
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntityEventHandler {

    private final Collection<EntityMatcher> entityWhitelist = Arrays.asList(
            new EntityTypeMatcher(EntityTypes.ARMOR_STAND),
            new EntityTypeMatcher(EntityTypes.PLAYER),
            new EntityTypeMatcher(EntityTypes.ITEM),
            new EntityTypeMatcher(EntityTypes.FALLING_BLOCK),
            new EntityTypeMatcher(EntityTypes.ITEM_FRAME),
            new EntityTypeIdMatcher("appliedenergistics2:appeng.entity.entitychargedquartz")
    );

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

    @Listener
    public void onEntitySpawn(SpawnEntityEvent event) {
        HashMap<ClaimedChunk, Boolean> spawnMap = new HashMap<>();
        event.filterEntities(entity -> {
            final boolean whitelistedEntity = entityWhitelist.stream()
                    .anyMatch(matcher -> matcher.matches(entity));
            if (whitelistedEntity) {
                return true;
            }
            ClaimedChunk chunk = ClaimedChunkHelper.getChunk(entity.getLocation());
            if (chunk == null) return true;
            else if (spawnMap.containsKey(chunk)) return spawnMap.get(chunk);
            ForgeTeam team = chunk.getTeam();
            boolean blockSpawn = team instanceof FlagTeamInfo && ((FlagTeamInfo) team).blockMobSpawns();
            spawnMap.put(chunk, !blockSpawn);
            return !blockSpawn;
        });
    }

    private interface EntityMatcher {

        boolean matches(final Entity entity);
    }

    private static class EntityTypeMatcher implements EntityMatcher {

        private final EntityType entityType;

        public EntityTypeMatcher(final EntityType entityType) {
            this.entityType = entityType;
        }

        @Override
        public boolean matches(final Entity entity) {
            return Objects.equals(entityType, entity.getType());
        }
    }

    private static class EntityTypeIdMatcher implements EntityMatcher {

        private final String id;

        public EntityTypeIdMatcher(final String id) {
            this.id = id;
        }

        @Override
        public boolean matches(final Entity entity) {
            return Objects.equals(id, entity.getType().getId());
        }
    }
}
