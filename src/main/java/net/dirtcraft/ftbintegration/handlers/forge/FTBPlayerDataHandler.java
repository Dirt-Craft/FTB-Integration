package net.dirtcraft.ftbintegration.handlers.forge;

import com.feed_the_beast.ftblib.events.player.ForgePlayerConfigEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerDataEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedOutEvent;
import com.feed_the_beast.ftbutilities.FTBUtilitiesConfig;
import com.feed_the_beast.ftbutilities.handlers.FTBUtilitiesPlayerEventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FTBPlayerDataHandler {

    @SubscribeEvent
    public void registerPlayerData(ForgePlayerDataEvent event) {
        FTBUtilitiesPlayerEventHandler.registerPlayerData(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLoggedIn(ForgePlayerLoggedInEvent event) {
        FTBUtilitiesPlayerEventHandler.onPlayerLoggedIn(event);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(ForgePlayerLoggedOutEvent event) {
        FTBUtilitiesPlayerEventHandler.onPlayerLoggedOut(event);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        FTBUtilitiesPlayerEventHandler.onPlayerClone(event);
    }

    @SubscribeEvent
    public void getPlayerSettings(ForgePlayerConfigEvent event) {
        FTBUtilitiesPlayerEventHandler.getPlayerSettings(event);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        FTBUtilitiesPlayerEventHandler.onDeath(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChunkChanged(EntityEvent.EnteringChunk event) {
        FTBUtilitiesPlayerEventHandler.onChunkChanged(event);
    }

    @SubscribeEvent
    public void onEntityDamage(LivingDamageEvent event) {
        if (FTBUtilitiesConfig.world.disable_player_suffocation_damage && event.getEntity() instanceof EntityPlayer && (event.getSource() == DamageSource.IN_WALL || event.getSource() == DamageSource.FLY_INTO_WALL)) {
            event.setAmount(0F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onNameFormat(PlayerEvent.NameFormat event) {
        FTBUtilitiesPlayerEventHandler.onNameFormat(event);
    }
}
