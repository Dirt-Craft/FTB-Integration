package net.dirtcraft.ftbutilitiesplus.handlers;

import net.dirtcraft.ftbutilitiesplus.data.PlayerData;
import net.dirtcraft.ftbutilitiesplus.utility.CauseContextHelper;
import net.dirtcraft.ftbutilitiesplus.utility.ClaimedChunkHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.block.BlockBridge;

import java.util.ArrayList;
import java.util.List;

public class GPHandler {
    private int lastBlockPreTick = -1;
    private boolean lastBlockPreCancelled = false;

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPre(ChangeBlockEvent.Pre event) {
        lastBlockPreTick = Sponge.getServer().getRunningTimeTicks();
        final EventContext context = event.getContext();
        final boolean isForgePlayerBreak = context.containsKey(EventContextKeys.PLAYER_BREAK);

        if (event.getSource() instanceof Player && isForgePlayerBreak){
            return;
        }
        if (context.containsKey(EventContextKeys.PISTON_RETRACT)) {
            return;
        }
        if (context.containsKey(EventContextKeys.BLOCK_EVENT_PROCESS)) {
            return;
        }

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

        lastBlockPreCancelled = false;

        if (isForgePlayerBreak && !hasFakePlayer && source instanceof Player) {
            final Player player = (Player) source;
            for (Location<World> location : event.getLocations()) {
                //if (GriefPreventionPlugin.isTargetIdBlacklisted(ClaimFlag.BLOCK_BREAK.toString(), location.getBlock(), world.getProperties())) {
                //    GPTimings.BLOCK_PRE_EVENT.stopTimingIfSync();
                //    return;
                //}

                if (location.getBlockType() == BlockTypes.AIR) {
                    continue;
                }

                // check overrides
                boolean blockBlockBreak = ClaimedChunkHelper.blockBlockEditing(player, location);
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
                // Mods such as enderstorage will send chest updates to itself
                // We must ignore cases like these to avoid issues with mod
                if (tileEntity != null) {
                    if (location.getPosition().equals(tileEntity.getLocation().getPosition())) {
                        continue;
                    }
                }

                if (user != null && !ClaimedChunkHelper.blockBlockEditing(user, location)) continue;
                if (ClaimedChunkHelper.isSameTeam(sourceLocation, location) && user == null && sourceEntity == null) {
                    continue;
                }

                // If a player successfully interacted with a block recently such as a pressure plate, ignore check
                // This fixes issues such as pistons not being able to extend
                PlayerData playerData = PlayerData.getData(user);
                if (user != null && !isForgePlayerBreak && playerData != null && playerData.checkLastInteraction(ClaimedChunkHelper.getTeam(location), user)) {
                    continue;
                }

                if (user != null && pistonExtend) {
                    if (!ClaimedChunkHelper.blockBlockInteractions(user, location)) continue;
                }

                if (user != null && ClaimedChunkHelper.blockBlockEditing(user, location)) {
                    event.setCancelled(true);
                    lastBlockPreCancelled = true;
                    return;
                }
            }
        } else if (user != null) {
            for (Location<World> location : event.getLocations()) {
                // Mods such as enderstorage will send chest updates to itself
                // We must ignore cases like these to avoid issues with mod
                if (tileEntity != null) {
                    if (location.getPosition().equals(tileEntity.getLocation().getPosition())) {
                        continue;
                    }
                }

                // If a player successfully interacted with a block recently such as a pressure plate, ignore check
                // This fixes issues such as pistons not being able to extend
                PlayerData playerData = PlayerData.getData(user);
                if (user != null && !isForgePlayerBreak && playerData != null && playerData.checkLastInteraction(ClaimedChunkHelper.getTeam(location), user)) {
                    continue;
                }

                if (!ClaimedChunkHelper.blockBlockInteractions(user, location)) continue;
                if (ClaimedChunkHelper.blockBlockInteractions(user, location)) {
                    event.setCancelled(true);
                    lastBlockPreCancelled = true;
                    return;
                }
            }
        }
    }
}
