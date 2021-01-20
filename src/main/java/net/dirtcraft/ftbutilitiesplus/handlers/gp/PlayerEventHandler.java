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
import com.flowpowered.math.vector.Vector3d;
import net.dirtcraft.ftbutilitiesplus.data.PlayerData;
import net.dirtcraft.ftbutilitiesplus.utility.ClaimedChunkHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.server.SPacketChunkData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;

public class PlayerEventHandler {

    private int lastInteractItemPrimaryTick = -1;
    private int lastInteractItemSecondaryTick = -1;

    /*
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractInventoryOpen(InteractInventoryEvent.Open event, @First Player player) {
        if (!GPFlags.INTERACT_INVENTORY || !GriefPreventionPlugin.instance.claimsEnabledForWorld(player.getWorld().getProperties())) {
            return;
        }

        final Cause cause = event.getCause();
        final EventContext context = cause.getContext();
        final BlockSnapshot blockSnapshot = context.get(EventContextKeys.BLOCK_HIT).orElse(BlockSnapshot.NONE);
        if (blockSnapshot == BlockSnapshot.NONE) {
            return;
        }
        if (GriefPreventionPlugin.isTargetIdBlacklisted(ClaimFlag.INTERACT_INVENTORY.toString(), blockSnapshot, player.getWorld().getProperties())) {
            return;
        }

        GPTimings.PLAYER_INTERACT_INVENTORY_OPEN_EVENT.startTimingIfSync();
        final Location<World> location = blockSnapshot.getLocation().get();
        final GPClaim claim = this.dataStore.getClaimAt(location);
        final Tristate result = GPPermissionHandler.getClaimPermission(event, location, claim, GPPermissions.INVENTORY_OPEN, player, blockSnapshot, player, TrustType.CONTAINER, true);
        if (result == Tristate.FALSE) {
            Text message = GriefPreventionPlugin.instance.messageData.permissionInventoryOpen
                    .apply(ImmutableMap.of(
                            "owner", claim.getOwnerName(),
                            "block", blockSnapshot.getState().getType().getId())).build();
            GriefPreventionPlugin.sendClaimDenyMessage(claim, player, message);
            ((EntityPlayerMP) player).closeScreen();
            event.setCancelled(true);
        }

        GPTimings.PLAYER_INTERACT_INVENTORY_OPEN_EVENT.stopTimingIfSync();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractInventoryClose(InteractInventoryEvent.Close event, @Root Player player) {
        final ItemStackSnapshot cursor = event.getCursorTransaction().getOriginal();
        if (cursor == ItemStackSnapshot.NONE || !GPFlags.ITEM_DROP || !GriefPreventionPlugin.instance.claimsEnabledForWorld(player.getWorld().getProperties())) {
            return;
        }
        if (GriefPreventionPlugin.isTargetIdBlacklisted(ClaimFlag.ITEM_DROP.toString(), cursor, player.getWorld().getProperties())) {
            return;
        }

        GPTimings.PLAYER_INTERACT_INVENTORY_CLOSE_EVENT.startTimingIfSync();
        final Location<World> location = player.getLocation();
        final GPClaim claim = this.dataStore.getClaimAt(location);
        if (GPPermissionHandler.getClaimPermission(event, location, claim, GPPermissions.ITEM_DROP, player, cursor, player, TrustType.ACCESSOR, true) == Tristate.FALSE) {
            Text message = GriefPreventionPlugin.instance.messageData.permissionItemDrop
                    .apply(ImmutableMap.of(
                            "owner", claim.getOwnerName(),
                            "item", cursor.getType().getId())).build();
            GriefPreventionPlugin.sendClaimDenyMessage(claim, player, message);
            event.setCancelled(true);
        }

        GPTimings.PLAYER_INTERACT_INVENTORY_CLOSE_EVENT.stopTimingIfSync();
    }
     */

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlockPrimary(InteractBlockEvent.Primary.MainHand event, @First Player player) {
        final BlockSnapshot clickedBlock = event.getTargetBlock();
        final HandType handType = event.getHandType();
        final ItemStack itemInHand = player.getItemInHand(handType).orElse(ItemStack.empty());
        // Run our item hook since Sponge no longer fires InteractItemEvent when targetting a non-air block
        if (clickedBlock != BlockSnapshot.NONE && handleItemInteract(event, player, player.getWorld(), itemInHand).isCancelled()) return;


        final Location<World> location = clickedBlock.getLocation().orElse(null);
        final Object source = !itemInHand.isEmpty() ? itemInHand : player;
        if (location == null) return;

        final PlayerData playerData = PlayerData.from(player);
        final ClaimedChunk claim = ClaimedChunkHelper.getChunk(location);
        if (ClaimedChunkHelper.blockBlockInteractions(playerData, location)) {
            /*
            if (GPPermissionHandler.getClaimPermission(event, location, claim, GPPermissions.BLOCK_BREAK, player, clickedBlock.getState(), player, TrustType.BUILDER, true) == Tristate.TRUE) {
                GPTimings.PLAYER_INTERACT_BLOCK_PRIMARY_EVENT.stopTimingIfSync();
                playerData.setLastInteractData(claim);
                return;
            }
             */
            event.setCancelled(true);
            return;
        }
        playerData.setLastInteractData(claim);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlockSecondary(InteractBlockEvent.Secondary event, @First Player player) {
        final BlockSnapshot clickedBlock = event.getTargetBlock();
        // Run our item hook since Sponge no longer fires InteractItemEvent when targetting a non-air block
        final HandType handType = event.getHandType();
        final ItemStack itemInHand = player.getItemInHand(handType).orElse(ItemStack.empty());
        if (handleItemInteract(event, player, player.getWorld(), itemInHand).isCancelled()) {
            event.setCancelled(true);
            return;
        }

        final Object source = !itemInHand.isEmpty() ? itemInHand : player;

        // Check if item is banned
        final PlayerData playerData = PlayerData.from(player);
        final Location<World> location = clickedBlock.getLocation().orElse(null);
        if (location == null) return;

        final ClaimedChunk claim = ClaimedChunkHelper.getChunk(location);
        final TileEntity tileEntity = clickedBlock.getLocation().get().getTileEntity().orElse(null);
        if (playerData != null) {
            boolean result = (tileEntity != null && tileEntity instanceof IInventory) ? ClaimedChunkHelper.blockBlockEditing(playerData, location): ClaimedChunkHelper.blockBlockInteractions(playerData, location);
            if (result) {
                // if player is holding an item, check if it can be placed
                if (!itemInHand.isEmpty() && itemInHand instanceof ItemBlock) {
                    if (ClaimedChunkHelper.blockBlockEditing(playerData, location)) {
                        playerData.setLastInteractData(claim);
                        return;
                    }
                }
                if (!SpongeImplHooks.isFakePlayer(((EntityPlayerMP) player)) && handType == HandTypes.MAIN_HAND) {
                    ((EntityPlayerMP) player).closeScreen();
                }

                // Special case for vanilla flower pots to fix client visual glitch
                // TODO - Fix in Forge so we can remove this hack
                if (clickedBlock.getState().getType() == BlockTypes.FLOWER_POT) {
                    final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;
                    mcPlayer.sendContainerToPlayer(mcPlayer.inventoryContainer);
                    if (tileEntity != null) {
                        mcPlayer.connection.sendPacket(((net.minecraft.tileentity.TileEntity) tileEntity).getUpdatePacket());
                        mcPlayer.connection.sendPacket(new SPacketChunkData(((net.minecraft.world.chunk.Chunk) ((ActiveChunkReferantBridge) player).bridge$getActiveChunk()),
                                1));
                    }
                }
                // Always cancel if using a mod item in hand due to dupes etc.
                if (!itemInHand.isEmpty() && !itemInHand.getType().getId().startsWith("minecraft")) {
                    event.setCancelled(true);
                } else {
                    event.setUseBlockResult(Tristate.FALSE);
                }
                return;
            }
        }

        playerData.setLastInteractData(claim);
    }

    public InteractEvent handleItemInteract(InteractEvent event, Player player, World world, ItemStack itemInHand) {
        if (lastInteractItemSecondaryTick == Sponge.getServer().getRunningTimeTicks() || lastInteractItemPrimaryTick == Sponge.getServer().getRunningTimeTicks()) {
            // ignore
            return event;
        }

        if (event instanceof InteractItemEvent.Primary) {
            lastInteractItemPrimaryTick = Sponge.getServer().getRunningTimeTicks();
        } else {
            lastInteractItemSecondaryTick = Sponge.getServer().getRunningTimeTicks();
        }

        final ItemType itemType = itemInHand.getType();
        if (itemInHand.isEmpty() || itemType instanceof ItemFood) {
            return event;
        }

        final Cause cause = event.getCause();
        final EventContext context = cause.getContext();
        final BlockSnapshot blockSnapshot = context.get(EventContextKeys.BLOCK_HIT).orElse(BlockSnapshot.NONE);
        final Vector3d interactPoint = event.getInteractionPoint().orElse(null);
        final Entity entity = context.get(EventContextKeys.ENTITY_HIT).orElse(null);
        final Location<World> location = entity != null ? entity.getLocation()
                : blockSnapshot != BlockSnapshot.NONE ? blockSnapshot.getLocation().get()
                : interactPoint != null ? new Location<World>(world, interactPoint)
                : player.getLocation();

        final PlayerData playerData = PlayerData.from(player);

        if (ClaimedChunkHelper.blockItemUse(playerData, location)) {
            if (event instanceof InteractItemEvent) {
                if (!SpongeImplHooks.isFakePlayer(((EntityPlayerMP) player)) && itemType == ItemTypes.WRITABLE_BOOK) {
                    ((EntityPlayerMP) player).closeScreen();
                }
            }
            event.setCancelled(true);
        }
        return event;
    }

}

