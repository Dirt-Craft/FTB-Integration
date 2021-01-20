/*
 * This file is part of GriefPrevention, licensed under the MIT License (MIT).
 *
 * Copyright (c) Ryan Hamshire
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

package net.dirtcraft.ftbutilitiesplus.handlers.gp;

import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import net.dirtcraft.ftbutilitiesplus.data.PlayerData;
import net.dirtcraft.ftbutilitiesplus.utility.CauseContextHelper;
import net.dirtcraft.ftbutilitiesplus.utility.ClaimedChunkHelper;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.LocationBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockEventHandler {
    private int lastBlockPreTick = -1;
    private boolean lastBlockPreCancelled = false;

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPre(ChangeBlockEvent.Pre event) {
        final EventContext context = event.getContext();
        final boolean isForgePlayerBreak = context.containsKey(EventContextKeys.PLAYER_BREAK);

        if (event.getSource() instanceof Player && isForgePlayerBreak) return;

        lastBlockPreTick = Sponge.getServer().getRunningTimeTicks();
        lastBlockPreCancelled = false;

        if (context.containsKey(EventContextKeys.PISTON_RETRACT)) return;
        if (context.containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) return;

        final Cause cause = event.getCause();
        final TileEntity tileEntity = cause.first(TileEntity.class).orElse(null);
        final LocatableBlock eventBlock = context.get(EventContextKeys.BLOCK_EVENT_QUEUE).orElse(null);
        final boolean pistonExtend = context.containsKey(EventContextKeys.PISTON_EXTEND);
        final boolean isBlockEvent = eventBlock != null;
        Object source = tileEntity != null ? tileEntity : cause.root();
        Location<World> sourceLocation = null;
        boolean isVanillaBlock;
        if (!pistonExtend && isBlockEvent) {
            isVanillaBlock = ((BlockBridge) eventBlock.getBlockState().getType()).bridge$isVanilla();
            if (isVanillaBlock) return;
            if (context.containsKey(EventContextKeys.NEIGHBOR_NOTIFY_SOURCE)) {
                source = eventBlock;
                sourceLocation = eventBlock.getLocation();
            }
        }

        final User user = CauseContextHelper.getEventUser(event);
        final PlayerData playerData = PlayerData.from(user);
        final LocatableBlock locatableBlock = cause.first(LocatableBlock.class).orElse(null);
        final boolean hasFakePlayer = context.containsKey(EventContextKeys.FAKE_PLAYER);
        Entity sourceEntity = null;
        // Always use TE as source if available
        if (sourceLocation == null) {
            sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;
            if (sourceLocation == null && source instanceof Entity) {
                // check entity
                sourceEntity = ((Entity) source);
                sourceLocation = sourceEntity.getLocation();
            }
        }

        if (context.containsKey(EventContextKeys.LIQUID_FLOW)) return;
        if (context.containsKey(EventContextKeys.FIRE_SPREAD)) return;
        if (context.containsKey(EventContextKeys.LEAVES_DECAY)) return;


        if (isForgePlayerBreak && !hasFakePlayer && source instanceof Player) {
            for (Location<World> location : event.getLocations()) {
                //if (GriefPreventionPlugin.isTargetIdBlacklisted(ClaimFlag.BLOCK_BREAK.toString(), location.getBlock(), world.getProperties())) {
                //    GPTimings.BLOCK_PRE_EVENT.stopTimingIfSync();
                //    return;
                //}

                if (location.getBlockType() == BlockTypes.AIR) {
                    continue;
                }

                // check overrides
                boolean blockBlockBreak = ClaimedChunkHelper.blockBlockEditing(playerData, location);
                if (blockBlockBreak) {
                    event.setCancelled(true);
                    lastBlockPreCancelled = true;
                    return;
                }
            }
            return;
        }

        if (sourceLocation != null) {
            List<Location<World>> sourceLocations = event.getLocations();
            if (pistonExtend) {
                // check next block in extend direction
                sourceLocations = new ArrayList<>(event.getLocations());
                Location<World> location = sourceLocations.get(sourceLocations.size() - 1);
                final Direction direction = locatableBlock.getLocation().getBlock().get(Keys.DIRECTION).get();
                final Location<World> dirLoc = location.getBlockRelative(direction);
                sourceLocations.add(dirLoc);
            }
            for (Location<World> location : sourceLocations) {
                ClaimedChunk chunk = ClaimedChunkHelper.getChunk(location);
                // Mods such as enderstorage will send chest updates to itself
                // We must ignore cases like these to avoid issues with mod
                if (tileEntity != null) {
                    if (location.getPosition().equals(tileEntity.getLocation().getPosition())) {
                        continue;
                    }
                }

                if (user != null && !ClaimedChunkHelper.blockBlockEditing(playerData, location)) continue;
                if (ClaimedChunkHelper.isSameTeam(sourceLocation, location) && user == null && sourceEntity == null) {
                    continue;
                }

                // If a player successfully interacted with a block recently such as a pressure plate, ignore check
                // This fixes issues such as pistons not being able to extend
                if (user != null && !isForgePlayerBreak && playerData != null && playerData.checkLastInteraction(chunk, user)) {
                    continue;
                }

                if (user != null && pistonExtend) {
                    if (!ClaimedChunkHelper.blockBlockInteractions(playerData, location)) continue;
                }

                if (user != null && ClaimedChunkHelper.blockBlockEditing(playerData, location)) {
                    event.setCancelled(true);
                    lastBlockPreCancelled = true;
                    return;
                }
            }
        } else if (user != null) {
            for (Location<World> location : event.getLocations()) {
                ClaimedChunk chunk = ClaimedChunkHelper.getChunk(location);
                // Mods such as enderstorage will send chest updates to itself
                // We must ignore cases like these to avoid issues with mod
                if (tileEntity != null) {
                    if (location.getPosition().equals(tileEntity.getLocation().getPosition())) {
                        continue;
                    }
                }

                // If a player successfully interacted with a block recently such as a pressure plate, ignore check
                // This fixes issues such as pistons not being able to extend
                if (!isForgePlayerBreak && playerData != null && playerData.checkLastInteraction(chunk, user)) {
                    continue;
                }

                if (!ClaimedChunkHelper.blockBlockInteractions(playerData, location)) continue;
                if (ClaimedChunkHelper.blockBlockInteractions(playerData, location)) {
                    event.setCancelled(true);
                    lastBlockPreCancelled = true;
                    return;
                }
            }
        }
    }

    // Handle fluids flowing into claims
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockNotify(NotifyNeighborBlockEvent event) {
        if (event.getContext().containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) {
            return;
        }
        final TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
        // Pistons are handled in Pre handler
        if (tileEntity instanceof TileEntityPiston) {
            return;
        }

        LocatableBlock locatableBlock = event.getCause().first(LocatableBlock.class).orElse(null);
        Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;
        ClaimedChunk sourceClaim = null;
        PlayerData playerData = null;

        final User user = CauseContextHelper.getEventUser(event);
        if (user == null) {
            return;
        }
        if (sourceLocation == null) {
            Player player = event.getCause().first(Player.class).orElse(null);
            if (player == null) {
                return;
            }

            sourceLocation = player.getLocation();
            playerData = PlayerData.from(player);
            sourceClaim = ClaimedChunkHelper.getChunk(sourceLocation);
        } else {
            playerData = PlayerData.from(user);
            sourceClaim = ClaimedChunkHelper.getChunk(sourceLocation);
        }

        ClaimedChunk targetClaim = null;
        List<Direction> removed = new ArrayList<>();
        for (Map.Entry<Direction, BlockState> neighborEntry : event.getNeighbors().entrySet()) {
            final Direction direction = neighborEntry.getKey();
            final BlockState blockState = neighborEntry.getValue();
            final Location<World> location = sourceLocation.getBlockRelative(direction);
            targetClaim = ClaimedChunkHelper.getChunk(location);
            if (sourceClaim == null && targetClaim == null) {
                if (playerData != null) {
                    playerData.setLastInteractData(targetClaim);
                }
                continue;
            } else if (sourceClaim != null && targetClaim != null && sourceClaim.getTeam().equals(targetClaim.getTeam())) {
                if (playerData != null) {
                    playerData.setLastInteractData(targetClaim);
                }
                continue;
            } else if (sourceClaim != null && targetClaim == null) {
                final MatterProperty matterProperty = blockState.getProperty(MatterProperty.class).orElse(null);
                if (matterProperty != null && matterProperty.getValue() != MatterProperty.Matter.LIQUID) {
                    if (playerData != null) {
                        playerData.setLastInteractData(targetClaim);
                    }
                    continue;
                }
            } else if (playerData != null && playerData.checkLastInteraction(targetClaim, user)) {
                continue;
            } else  {
                // Needed to handle levers notifying doors to open etc.
                if (ClaimedChunkHelper.blockBlockInteractions(playerData, location)) {
                    if (playerData != null) {
                        playerData.setLastInteractData(targetClaim);
                    }
                    continue;
                }
            }

            // no claim crossing unless trusted
            removed.add(direction);
        }

        for (Direction direction : removed) {
            event.getNeighbors().remove(direction);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockCollide(CollideBlockEvent event, @Root Entity source) {
        if (event instanceof CollideBlockEvent.Impact) {
            return;
        }
        // ignore falling blocks
        if (source instanceof EntityFallingBlock) return;

        final User user = CauseContextHelper.getEventUser(event);
        if (user == null) {
            return;
        }

        final BlockType blockType = event.getTargetBlock().getType();
        if (blockType.equals(BlockTypes.AIR)) return;

        if (source instanceof EntityItem && (blockType != BlockTypes.PORTAL && !(blockType instanceof BlockBasePressurePlate)))
            return;

        /*
        BlockPos collidePos = ((LocationBridge)(Object) event.getTargetLocation()).bridge$getBlockPos();
        short shortPos = BlockUtils.blockPosToShort(collidePos);
        int entityId = ((net.minecraft.entity.Entity) source).getEntityId();
        BlockPosCache entityBlockCache = BlockUtils.ENTITY_BLOCK_CACHE.get(entityId);
        if (entityBlockCache == null) {
            entityBlockCache = new BlockPosCache(shortPos);
            BlockUtils.ENTITY_BLOCK_CACHE.put(entityId, entityBlockCache);
        } else {
            Tristate result = entityBlockCache.getCacheResult(shortPos);
            if (result != Tristate.UNDEFINED) {
                if (result == Tristate.FALSE) {
                    event.setCancelled(true);
                }

                GPTimings.BLOCK_COLLIDE_EVENT.stopTimingIfSync();
                return;
            }
        }
         */

        PlayerData playerData;
        ClaimedChunk targetClaim = ClaimedChunkHelper.getChunk(event.getTargetLocation());
        if (user instanceof Player) {
            playerData = PlayerData.from(user);
            playerData.setLastInteractData(targetClaim);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onProjectileImpactBlock(CollideBlockEvent.Impact event) {
        if (!(event.getSource() instanceof Entity)) return;

        final User user = CauseContextHelper.getEventUser(event);
        if (user == null) {
            return;
        }

        Location<World> impactPoint = event.getImpactPoint();
        PlayerData playerData = null;
        if (user instanceof Player) playerData = PlayerData.from(user);
        if (ClaimedChunkHelper.blockBlockInteractions(playerData, impactPoint)) {
            event.setCancelled(true);
        }
    }

    /*
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Object source = event.getSource();
        if (source instanceof Explosion) {
            final Explosion explosion = (Explosion) source;
            if (explosion.getSourceExplosive().isPresent()) {
                source = explosion.getSourceExplosive().get();
            } else {
                Entity exploder = event.getCause().first(Entity.class).orElse(null);
                if (exploder != null) {
                    source = exploder;
                }
            }
        }

        final User user = CauseContextHelper.getEventUser(event);
        final PlayerData playerData = PlayerData.from(user);
        ClaimedChunk targetClaim = null;
        final List<Location<World>> filteredLocations = new ArrayList<>();
        for (Location<World> location : event.getAffectedLocations()) {
            targetClaim =  ClaimedChunkHelper.getChunk(location);


            if (ClaimedChunkHelper.blockBlockEditing(playerData, targetClaim)) {
                // Avoid lagging server from large explosions.
                if (event.getAffectedLocations().size() > 100) {
                    event.setCancelled(true);
                    break;
                }
                filteredLocations.add(location);
            }
        }
        // Workaround for SpongeForge bug
        if (event.isCancelled()) {
            event.getAffectedLocations().clear();
        } else if (!filteredLocations.isEmpty()) {
            event.getAffectedLocations().removeAll(filteredLocations);
        }
    }
     */

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        if (event instanceof ExplosionEvent) return;
        if (event.getCause().root() instanceof TileEntityPiston) return;
        if (event.getContext().containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) return;

        if (lastBlockPreTick == Sponge.getServer().getRunningTimeTicks()) {
            event.setCancelled(lastBlockPreCancelled);
            return;
        }

        Object source = event.getSource();
        // Handled in Explosion listeners
        if (source instanceof Explosion) return;
        final Player player = source instanceof Player ? (Player) source : null;
        final User user = player != null ? player : CauseContextHelper.getEventUser(event);

        // TODO FIX liquid_flow context leaking in sponge
        /*final boolean isLiquidSource = event.getContext().containsKey(EventContextKeys.LIQUID_FLOW);
        if (isLiquidSource) {
            return;
        }*/


        // ignore falling blocks when there is no user
        // avoids dupes with falling blocks such as Dragon Egg
        if (user == null && source instanceof EntityFallingBlock) return;

        ClaimedChunk sourceClaim = null;
        LocatableBlock locatable = null;
        if (source instanceof LocatableBlock) {
            locatable = (LocatableBlock) source;
            sourceClaim = ClaimedChunkHelper.getChunk(locatable.getLocation());
        } else {
            sourceClaim = ClaimedChunkHelper.getChunk(event.getCause());
        }
        if (sourceClaim == null) {
            return;
        }

        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        ClaimedChunk targetClaim = null;
        for (Transaction<BlockSnapshot> transaction : transactions) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            targetClaim = ClaimedChunkHelper.getChunk(location);
            if (locatable != null && targetClaim == null) continue;
            if (location == null || transaction.getOriginal().getState().getType() == BlockTypes.AIR) {
                continue;
            }

            // check overrides
            PlayerData playerData = PlayerData.from(user);
            if (ClaimedChunkHelper.blockBlockEditing(playerData, location)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        final Object source = event.getSource();
        // Pistons are handled in onBlockPre
        if (source instanceof TileEntityPiston) return;
        if (event.getContext().containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) return;
        if (lastBlockPreTick == Sponge.getServer().getRunningTimeTicks() && !(event.getCause().root() instanceof Player)) {
            event.setCancelled(lastBlockPreCancelled);
            return;
        }

        final World world = event.getTransactions().get(0).getFinal().getLocation().get().getExtent();

        ClaimedChunk sourceClaim;
        LocatableBlock locatable = null;
        final User user = CauseContextHelper.getEventUser(event);
        if (source instanceof LocatableBlock) {
            locatable = (LocatableBlock) source;
            sourceClaim = ClaimedChunkHelper.getChunk(locatable.getLocation());
        } else {
            sourceClaim = ClaimedChunkHelper.getChunk(event.getCause());
        }
        if (sourceClaim == null) return;

        Player player = user instanceof Player ? (Player) user : null;
        PlayerData playerData = PlayerData.from(user);

        //sourceClaim != null &&
        if (!(source instanceof User) && playerData != null && playerData.checkLastInteraction(sourceClaim, user)) return;

        ClaimedChunk targetClaim = null;
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            final BlockSnapshot block = transaction.getFinal();
            Location<World> location = block.getLocation().orElse(null);
            if (location == null) continue;

            targetClaim = ClaimedChunkHelper.getChunk(location);
            if (locatable != null && targetClaim == null) continue;

            //if (GPFlags.BLOCK_PLACE) {
            // Allow blocks to grow within claims
            if (user == null && ClaimedChunkHelper.isSameTeam(sourceClaim, targetClaim)) return;


            if (ClaimedChunkHelper.blockBlockEditing(playerData, location)) {
                // TODO - make sure this doesn't spam
                    /*if (source instanceof Player) {
                        final Text message = GriefPreventionPlugin.instance.messageData.permissionBuild
                                .apply(ImmutableMap.of(
                                "player", Text.of(targetClaim.getOwnerName())
                        )).build();
                        GriefPreventionPlugin.sendClaimDenyMessage(targetClaim, (Player) source, message);
                    }*/
                event.setCancelled(true);
                return;
            }
            //}

            /*
            // warn players when they place TNT above sea level, since it doesn't destroy blocks there
            if (GPFlags.EXPLOSION_SURFACE && player != null && block.getState().getType() == BlockTypes.TNT && GPPermissionHandler.getClaimPermission(event, location, targetClaim, GPPermissions.EXPLOSION_SURFACE, event.getCause().root(), block.getState(), user) == Tristate.FALSE &&
                    !block.getLocation().get().getExtent().getDimension().getType().equals(DimensionTypes.NETHER) &&
                    block.getPosition().getY() > GriefPreventionPlugin.instance.getSeaLevel(block.getLocation().get().getExtent()) - 5 &&
                    targetClaim.isWilderness()) {
                GriefPreventionPlugin.sendMessage(player, GriefPreventionPlugin.instance.messageData.warningTntAboveSeaLevel.toText());
            }

            // warn players about disabled pistons outside of land claims
            if (player != null && !playerData.canIgnoreClaim(targetClaim) && activeConfig.getConfig().general.limitPistonsToClaims &&
                    (block.getState().getType() == BlockTypes.PISTON || block.getState().getType() == BlockTypes.STICKY_PISTON) &&
                    targetClaim.isWilderness()) {
                GriefPreventionPlugin.sendMessage(player, GriefPreventionPlugin.instance.messageData.warningPistonsOutsideClaims.toText());
            }
             */
        }
    }
}
