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

package net.dirtcraft.ftbintegration.handlers.sponge;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import net.dirtcraft.ftbintegration.FtbIntegration;
import net.dirtcraft.ftbintegration.core.mixins.generic.AccessorTileEntity;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.utility.CauseContextHelper;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.mixin.core.tileentity.TileEntityAccessor;

import java.util.*;

public class BlockEventHandler {

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPre(ChangeBlockEvent.Pre event) {

        final Cause cause = event.getCause();
        final EventContext context = event.getContext();

        if (context.containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) return;
        if (context.containsKey(EventContextKeys.PISTON_RETRACT)) return;
        if (context.containsKey(EventContextKeys.LEAVES_DECAY)) return;
        if (context.containsKey(EventContextKeys.LIQUID_FLOW)) return;
        if (context.containsKey(EventContextKeys.FIRE_SPREAD)) return;

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
        final PlayerData playerData = PlayerData.getOrCreate(user);
        final LocatableBlock locatableBlock = cause.first(LocatableBlock.class).orElse(null);
        final boolean hasFakePlayer = context.containsKey(EventContextKeys.FAKE_PLAYER);

        if (sourceLocation == null) {
            sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;
            if (sourceLocation == null && source instanceof Entity) {
                sourceLocation = ((Entity) source).getLocation();
            }
        }

        final boolean isForgePlayerBreak = context.containsKey(EventContextKeys.PLAYER_BREAK);
        if (isForgePlayerBreak && !hasFakePlayer && source instanceof Player) {
            if (handlePlayerBreak(event.getLocations(), playerData)){
                event.setCancelled(true);
            }
        } else if (sourceLocation != null || user != null) {
            List<Location<World>> sourceLocations = event.getLocations();
            if (sourceLocation != null && pistonExtend) {
                // add next block in extend direction
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
                if (tileEntity != null && location.getPosition().equals(tileEntity.getLocation().getPosition())) continue;

                if (!ClaimedChunkHelper.blockBlockEditing(playerData, chunk, location)) continue;

                // If a player successfully interacted with a block recently such as a pressure plate, ignore check
                // This fixes issues such as pistons not being able to extend
                if (!isForgePlayerBreak && playerData.checkLastInteraction(chunk, user)) continue;

                //Don't cancel any related events if it's a leaf
                if (location.getBlockType() == BlockTypes.LEAVES || location.getBlockType() ==  BlockTypes.LEAVES2) return;
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean handlePlayerBreak(List<Location<World>> locations, PlayerData playerData) {
        for (Location<World> location : locations) {
            if (location.getBlockType() == BlockTypes.AIR) continue;
            if (ClaimedChunkHelper.blockBlockEditing(playerData, location)) {
                return true;
            }
        }
        return false;
    }

    // Handle fluids flowing into claims
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockNotify(NotifyNeighborBlockEvent event) {
        if (event.getContext().containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) return;
        final TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
        if (tileEntity instanceof TileEntityPiston) return;

        LocatableBlock locatableBlock = event.getCause().first(LocatableBlock.class).orElse(null);
        Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;
        ClaimedChunk sourceClaim;
        PlayerData playerData;

        final User user = CauseContextHelper.getEventUser(event);
        if (user == null) return;

        if (sourceLocation == null) {
            Player player = event.getCause().first(Player.class).orElse(null);
            if (player == null) {
                return;
            }

            sourceLocation = player.getLocation();
            playerData = PlayerData.getOrCreate(player);
        } else {
            playerData = PlayerData.getOrCreate(user);
        }

        sourceClaim = ClaimedChunkHelper.getChunk(sourceLocation);
        List<Direction> removed = new ArrayList<>();
        for (Map.Entry<Direction, BlockState> neighborEntry : event.getNeighbors().entrySet()) {
            final Direction direction = neighborEntry.getKey();
            final BlockState blockState = neighborEntry.getValue();
            final Location<World> location = sourceLocation.getBlockRelative(direction);
            ClaimedChunk targetClaim = ClaimedChunkHelper.getChunk(location);
            if (sourceClaim == null && targetClaim == null) {
                if (playerData != null) {
                    playerData.setLastInteractData(null);
                }
                continue;
            } else if (ClaimedChunkHelper.isSameTeam(sourceClaim, targetClaim)) {
                if (playerData != null) {
                    playerData.setLastInteractData(targetClaim);
                }
                continue;
            } else if (sourceClaim != null && targetClaim == null) {
                final MatterProperty matterProperty = blockState.getProperty(MatterProperty.class).orElse(null);
                if (matterProperty != null && matterProperty.getValue() != MatterProperty.Matter.LIQUID) {
                    if (playerData != null) {
                        playerData.setLastInteractData(null);
                    }
                    continue;
                }
            } else if (playerData != null && playerData.checkLastInteraction(targetClaim, user)) {
                continue;
            } else  {
                // Needed to handle levers notifying doors to open etc.
                if (!ClaimedChunkHelper.blockBlockInteractions(playerData, targetClaim, location)) {
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
        if (event instanceof CollideBlockEvent.Impact) return;
        if (source instanceof EntityFallingBlock) return;

        final BlockType blockType = event.getTargetBlock().getType();
        if (blockType.equals(BlockTypes.AIR)) return;

        final User user = CauseContextHelper.getEventUser(event);
        if (user == null) {
            return;
        }

        if (source instanceof EntityItem && (blockType != BlockTypes.PORTAL && !(blockType instanceof BlockBasePressurePlate))) return;

        if (user instanceof Player) {
            ClaimedChunk targetClaim = ClaimedChunkHelper.getChunk(event.getTargetLocation());
            PlayerData playerData = PlayerData.getOrCreate(user);
            playerData.setLastInteractData(targetClaim);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onProjectileImpactBlock(CollideBlockEvent.Impact event) {
        if (!(event.getSource() instanceof Entity)) return;

        final User user = CauseContextHelper.getEventUser(event);

        if (user instanceof Player && ClaimedChunkHelper.blockBlockInteractions(PlayerData.getOrCreate(user), event.getImpactPoint())) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        final User user = CauseContextHelper.getEventUser(event);
        final PlayerData playerData = PlayerData.get(user);
        // Avoid lagging server from large explosions.
        if (event.getAffectedLocations().size() > 255) {
            event.getAffectedLocations().clear();
            event.setCancelled(true);
        } else if (event.getAffectedLocations().size() > 20) {
            final HashMap<ForgeTeam, Tristate> chunkCache = new HashMap<>();
            final List<Location<World>> filteredLocations = new ArrayList<>();
            for (Location<World> location : event.getAffectedLocations()){
                ClaimedChunk chunk = ClaimedChunkHelper.getChunk(location);
                if (chunk == null) continue;
                switch (chunkCache.getOrDefault(chunk.getTeam(), Tristate.UNDEFINED)){
                    case FALSE: filteredLocations.add(location);
                    case TRUE: continue;
                }
                if (!chunk.hasExplosions() || ClaimedChunkHelper.blockBlockEditing(playerData, chunk, location)) {
                    filteredLocations.add(location);
                    chunkCache.put(chunk.getTeam(), Tristate.FALSE);
                } else {
                    chunkCache.put(chunk.getTeam(), Tristate.TRUE);
                }
            }
            event.getAffectedLocations().removeAll(filteredLocations);

        } else {
            final List<Location<World>> filteredLocations = new ArrayList<>();
            for (Location<World> location : event.getAffectedLocations()) {
                ClaimedChunk chunk = ClaimedChunkHelper.getChunk(location);
                if (!chunk.hasExplosions() || ClaimedChunkHelper.blockBlockEditing(playerData, chunk, location)) {
                    filteredLocations.add(location);
                }
            }
            event.getAffectedLocations().removeAll(filteredLocations);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        final Object source = event.getSource();

        if (event instanceof ExplosionEvent || source instanceof Explosion) return;
        if (event.getCause().root() instanceof TileEntityPiston) return;
        if (event.getContext().containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) return;

        final User user = source instanceof Player? (User) source : CauseContextHelper.getEventUser(event);

        // ignore falling blocks when there is no user
        // avoids dupes with falling blocks such as Dragon Egg
        if (user == null && source instanceof EntityFallingBlock) return;

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            ClaimedChunk targetClaim = ClaimedChunkHelper.getChunk(location);
            if (source instanceof LocatableBlock && targetClaim == null) continue;
            if (location == null || transaction.getOriginal().getState().getType() == BlockTypes.AIR) {
                continue;
            }

            PlayerData playerData = PlayerData.getOrCreate(user);
            if (ClaimedChunkHelper.blockBlockEditing(playerData, location)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        final Object source = event.getSource();

        if (source instanceof TileEntityPiston) return;
        if (event.getContext().containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) return;

        ClaimedChunk sourceClaim;
        final User user = CauseContextHelper.getEventUser(event);
        if (source instanceof LocatableBlock) {
            sourceClaim = ClaimedChunkHelper.getChunk(((LocatableBlock) source).getLocation());
        } else {
            sourceClaim = ClaimedChunkHelper.getChunk(event.getCause());
        }

        PlayerData playerData = PlayerData.getOrCreate(user);

        if ( sourceClaim != null && !(source instanceof User) && playerData != null && playerData.checkLastInteraction(sourceClaim, user)) return;

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            final BlockSnapshot block = transaction.getFinal();
            Location<World> location = block.getLocation().orElse(null);
            if (location == null) continue;

            ClaimedChunk targetClaim = ClaimedChunkHelper.getChunk(location);
            if (targetClaim == null) continue;

            // Allow blocks to grow within claims
            if (user == null && ClaimedChunkHelper.isSameTeam(sourceClaim, targetClaim)) return;

            if (ClaimedChunkHelper.blockBlockEditing(playerData, targetClaim, location)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.POST)
    public void afterBlockPlace(ChangeBlockEvent.Place event){
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()){
            if (transaction.getFinal().getCreator().isPresent()) continue;
            User user = CauseContextHelper.getEventUser(event);
            Optional<TileEntity> optTe = transaction.getFinal().getLocation().flatMap(Location::getTileEntity);
            if (user == null || user instanceof FakePlayer || !optTe.isPresent()) continue;

            TileEntity te = optTe.get();
            OwnershipTrackedBridge teTracker = (OwnershipTrackedBridge) te;
            teTracker.tracked$setOwnerReference(user);
            World cd = te.getLocation().getExtent();
            int cx = te.getLocation().getBlockX() >> 4;
            int cz = te.getLocation().getBlockZ() >> 4;
            BlockPos bp = ((net.minecraft.tileentity.TileEntity)te).getPos();
            ChunkBridge bridge = (ChunkBridge) cd.getChunk(cx, 0, cz).get();
            bridge.bridge$setBlockCreator(bp, user.getUniqueId());
            bridge.bridge$markChunkDirty();
        }
    }
}
