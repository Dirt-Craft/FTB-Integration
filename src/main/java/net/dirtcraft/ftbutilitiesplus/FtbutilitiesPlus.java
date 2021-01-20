package net.dirtcraft.ftbutilitiesplus;

import com.feed_the_beast.ftbutilities.handlers.FTBUtilitiesPlayerEventHandler;
import com.google.inject.Inject;
import net.dirtcraft.ftbutilitiesplus.command.Debug;
import net.dirtcraft.ftbutilitiesplus.data.PlayerData;
import net.dirtcraft.ftbutilitiesplus.handlers.GPHandler;
import net.dirtcraft.ftbutilitiesplus.handlers.PlayerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "ftbutilities-plus",
        name = "FTB Utilities Plus",
        description = "Beefs up FTB Util claim handling",
        authors = {
                "ShinyAfro"
        }
)
public class FtbutilitiesPlus {

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        //MinecraftForge.EVENT_BUS.unregister(FTBUtilitiesPlayerEventHandler.class);
        CommandSpec debug = CommandSpec.builder()
                .permission("ftbutilities.debug")
                .executor(new Debug())
                .build();

        CommandSpec main = CommandSpec.builder()
                .permission("ftbutilities.main")
                .child(debug, "debug")
                .build();

        Sponge.getEventManager().registerListeners(this, new GPHandler());
        Sponge.getEventManager().registerListeners(this, new PlayerEventHandler());
        Sponge.getEventManager().registerListeners(this, new PlayerData());
        Sponge.getCommandManager().register(this, main, "ftbupp");
    }
}
