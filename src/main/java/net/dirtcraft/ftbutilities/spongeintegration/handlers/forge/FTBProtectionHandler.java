package net.dirtcraft.ftbutilities.spongeintegration.handlers.forge;

import com.feed_the_beast.ftblib.lib.util.InvUtils;
import com.feed_the_beast.ftblib.lib.util.ServerUtils;
import com.feed_the_beast.ftbutilities.FTBUtilitiesConfig;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesUniverseData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FTBProtectionHandler {

    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent event) {
        if (FTBUtilitiesConfig.world.disable_player_suffocation_damage && event.getEntity() instanceof EntityPlayer && (event.getSource() == DamageSource.IN_WALL || event.getSource() == DamageSource.FLY_INTO_WALL)) {
            event.setAmount(0F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityAttacked(AttackEntityEvent event) {
        if (!ClaimedChunks.canAttackEntity(event.getEntityPlayer(), event.getTarget())) {
            InvUtils.forceUpdate(event.getEntityPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (FTBUtilitiesConfig.world.isItemRightClickDisabled(event.getItemStack())) {
            event.setCanceled(true);

            if (!event.getWorld().isRemote) {
                event.getEntityPlayer().sendStatusMessage(new TextComponentString("Item disabled!"), true);
            }

            return;
        }

        if (ClaimedChunks.blockBlockInteractions(event.getEntityPlayer(), event.getPos(), null)) {
            InvUtils.forceUpdate(event.getEntityPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (FTBUtilitiesConfig.world.isItemRightClickDisabled(event.getItemStack())) {
            event.setCanceled(true);

            if (!event.getWorld().isRemote) {
                event.getEntityPlayer().sendStatusMessage(new TextComponentString("Item disabled!"), true);
            }

            return;
        }

        if (ClaimedChunks.blockItemUse(event.getEntityPlayer(), event.getHand(), event.getPos())) {
            InvUtils.forceUpdate(event.getEntityPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ClaimedChunks.blockBlockEditing(event.getPlayer(), event.getPos(), event.getState())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (ClaimedChunks.blockBlockEditing(event.getPlayer(), event.getPos(), event.getPlacedBlock())) {
            InvUtils.forceUpdate(event.getPlayer());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (ClaimedChunks.blockBlockEditing(event.getEntityPlayer(), event.getPos(), null)) {
            event.setCanceled(true);
        }
    }

    /*
	@SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemPickup(EntityItemPickupEvent event)
    {
    }
    */

    private static String getStateName(IBlockState state) {
        if (state == state.getBlock().getDefaultState()) {
            return state.getBlock().getRegistryName().toString();
        }

        return state.toString();
    }

    private static String getDim(EntityPlayer player) {
        return ServerUtils.getDimensionName(player.dimension).getUnformattedText();
    }

    private static String getPos(BlockPos pos) {
        return String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
    }

    @SubscribeEvent
    public static void onBlockBreakLog(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();

        if (FTBUtilitiesConfig.world.logging.block_broken && player instanceof EntityPlayerMP && FTBUtilitiesConfig.world.logging.log((EntityPlayerMP) player)) {
            FTBUtilitiesUniverseData.worldLog(String.format("%s broke %s at %s in %s", player.getName(), getStateName(event.getState()), getPos(event.getPos()), getDim(player)));
        }
    }

    @SubscribeEvent
    public static void onBlockPlaceLog(BlockEvent.PlaceEvent event) {
        EntityPlayer player = event.getPlayer();

        if (FTBUtilitiesConfig.world.logging.block_placed && player instanceof EntityPlayerMP && FTBUtilitiesConfig.world.logging.log((EntityPlayerMP) player)) {
            FTBUtilitiesUniverseData.worldLog(String.format("%s placed %s at %s in %s", player.getName(), getStateName(event.getState()), getPos(event.getPos()), getDim(player)));
        }
    }

    @SubscribeEvent
    public static void onRightClickItemLog(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();

        if (FTBUtilitiesConfig.world.logging.item_clicked_in_air && player instanceof EntityPlayerMP && FTBUtilitiesConfig.world.logging.log((EntityPlayerMP) player)) {
            FTBUtilitiesUniverseData.worldLog(String.format("%s clicked %s in air at %s in %s", player.getName(), event.getItemStack().getItem().getRegistryName(), getPos(event.getPos()), getDim(player)));
        }
    }
}
