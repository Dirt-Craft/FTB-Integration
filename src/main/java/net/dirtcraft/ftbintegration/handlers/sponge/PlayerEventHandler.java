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

import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.flowpowered.math.vector.Vector3d;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.data.PlayerDataManager;
import net.dirtcraft.ftbintegration.data.sponge.PlayerSettings;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraftforge.common.util.FakePlayer;
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
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;

public class PlayerEventHandler {

    private final PlayerDataManager manager = PlayerDataManager.getInstance();
    private int lastInteractItemPrimaryTick = -1;
    private int lastInteractItemSecondaryTick = -1;

    @Listener
    public void onLogin(ClientConnectionEvent.Join event){
        manager.loadUser(event.getTargetEntity());
        PlayerSettings settings = Sponge.getDataManager()
                .getManipulatorBuilder(PlayerSettings.class).get()
                .createFrom(event.getTargetEntity()).get();
        event.getTargetEntity().offer(settings);
    }

    @Listener(order = Order.POST)
    public void onLogoff(ClientConnectionEvent.Disconnect event){
        manager.unloadUser(event.getTargetEntity());
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractEntity(InteractEntityEvent.Primary event, @First Player player) {
        final Entity targetEntity = event.getTargetEntity();
        final Location<World> location = targetEntity.getLocation();
        if (ClaimedChunkHelper.blockBlockInteractions(PlayerData.getOrCreate(player), location)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractEntity(InteractEntityEvent.Secondary event, @First Player player) {
        final Entity targetEntity = event.getTargetEntity();
        final Location<World> location = targetEntity.getLocation();
        if (ClaimedChunkHelper.blockBlockInteractions(PlayerData.getOrCreate(player), location)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerUseItem(UseItemStackEvent.Start event, @First Player player) {
        if (ClaimedChunkHelper.blockItemUse(PlayerData.getOrCreate(player), player.getLocation(), event.getItemStackInUse().getType())) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractItem(InteractItemEvent event, @Root Player player) {
        final World world = player.getWorld();
        final HandInteractEvent handEvent = (HandInteractEvent) event;
        final ItemStack itemInHand = player.getItemInHand(handEvent.getHandType()).orElse(ItemStack.empty());

        handleItemInteract(event, player, world, itemInHand);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractInventoryOpen(InteractInventoryEvent.Open event, @First Player player) {
        event.getCause().getContext().get(EventContextKeys.BLOCK_HIT).ifPresent(blockSnapshot -> {
            final Location<World> location = blockSnapshot.getLocation().get();
            if (!ClaimedChunkHelper.blockBlockEditing(PlayerData.getOrCreate(player), location)) return;
            ((EntityPlayerMP) player).closeScreen();
            event.setCancelled(true);
        });
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteractBlockPrimary(InteractBlockEvent.Primary.MainHand event, @First Player player) {
        final BlockSnapshot clickedBlock = event.getTargetBlock();
        final HandType handType = event.getHandType();
        final ItemStack itemInHand = player.getItemInHand(handType).orElse(ItemStack.empty());
        // Run our item hook since Sponge no longer fires InteractItemEvent when targetting a non-air block
        if (clickedBlock != BlockSnapshot.NONE && handleItemInteract(event, player, player.getWorld(), itemInHand)) return;


        final Location<World> location = clickedBlock.getLocation().orElse(null);
        if (location == null) return;

        final PlayerData playerData = PlayerData.getOrCreate(player);
        final ClaimedChunk claim = ClaimedChunkHelper.getChunk(location);
        if (ClaimedChunkHelper.blockBlockInteractions(playerData, location)) {
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
        if (handleItemInteract(event, player, player.getWorld(), itemInHand)) {
            event.setCancelled(true);
            return;
        }

        final PlayerData playerData = PlayerData.getOrCreate(player);
        final Location<World> location = clickedBlock.getLocation().orElse(null);
        if (location == null) return;

        final ClaimedChunk claim = ClaimedChunkHelper.getChunk(location);
        final TileEntity tileEntity = clickedBlock.getLocation().get().getTileEntity().orElse(null);
        if (playerData != null) {
            boolean result = (tileEntity instanceof IInventory) ? ClaimedChunkHelper.blockBlockEditing(playerData, location): ClaimedChunkHelper.blockBlockInteractions(playerData, location);
            if (result) {
                // if player is holding an item, check if it can be placed
                if (!itemInHand.isEmpty() && itemInHand instanceof ItemBlock) {
                    if (ClaimedChunkHelper.blockBlockEditing(playerData, location)) {
                        playerData.setLastInteractData(claim);
                        return;
                    }
                }
                if (!(player instanceof FakePlayer) && handType == HandTypes.MAIN_HAND) {
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

        if (playerData != null) playerData.setLastInteractData(claim);
    }

    public boolean handleItemInteract(InteractEvent event, Player player, World world, ItemStack itemInHand) {
        if (lastInteractItemSecondaryTick == Sponge.getServer().getRunningTimeTicks() || lastInteractItemPrimaryTick == Sponge.getServer().getRunningTimeTicks()) {
            return event.isCancelled();
        }

        if (event instanceof InteractItemEvent.Primary) {
            lastInteractItemPrimaryTick = Sponge.getServer().getRunningTimeTicks();
        } else {
            lastInteractItemSecondaryTick = Sponge.getServer().getRunningTimeTicks();
        }

        final ItemType itemType = itemInHand.getType();
        if (itemInHand.isEmpty() || itemType instanceof ItemFood) {
            return event.isCancelled();
        }

        final Cause cause = event.getCause();
        final EventContext context = cause.getContext();
        final BlockSnapshot blockSnapshot = context.get(EventContextKeys.BLOCK_HIT).orElse(BlockSnapshot.NONE);
        final Vector3d interactPoint = event.getInteractionPoint().orElse(null);
        final Entity entity = context.get(EventContextKeys.ENTITY_HIT).orElse(null);
        final Location<World> location = entity != null ? entity.getLocation()
                : blockSnapshot != BlockSnapshot.NONE ? blockSnapshot.getLocation().get()
                : interactPoint != null ? new Location<>(world, interactPoint)
                : player.getLocation();

        final PlayerData playerData = PlayerData.getOrCreate(player);

        if (ClaimedChunkHelper.blockItemUse(playerData, location, itemType)) {
            if (event instanceof InteractItemEvent) {
                if (!(player instanceof FakePlayer) && itemType == ItemTypes.WRITABLE_BOOK) {
                    ((EntityPlayerMP) player).closeScreen();
                }
            }
            event.setCancelled(true);
        }
        return event.isCancelled();
    }

}

