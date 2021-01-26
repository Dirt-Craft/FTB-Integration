package net.dirtcraft.ftbutilities.spongeintegration;

import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.PlayerEventHandler;
import net.dirtcraft.ftbutilities.spongeintegration.command.debug.Debug;
import net.dirtcraft.ftbutilities.spongeintegration.data.PlayerData;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.NucleusHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.BlockEventHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(id = "ftbu-sponge-integration-plugin",
        name = FtbUtilitiesSpongeIntegrationMod.NAME,
        version = FtbUtilitiesSpongeIntegrationMod.VERSION,
        description = "Beefs up FTB Util claim handling",
        authors = {"ShinyAfro"})
public class FtbUtilitiesSpongeIntegrationPlugin {
    final List<Object> listeners = Stream.of(
            new NucleusHandler(),
            new BlockEventHandler(),
            new PlayerEventHandler()
    ).collect(Collectors.toList());

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        CommandSpec activateListeners = CommandSpec.builder()
                .permission("ftbutilities.debug")
                .executor((a,b)->{
                    deregisterListeners();
                    return CommandResult.success();
                })
                .build();

        CommandSpec deactivateListeners = CommandSpec.builder()
                .permission("ftbutilities.debug")
                .executor((a,b)->{
                    deregisterListeners();
                    return CommandResult.success();
                })
                .build();

        CommandSpec debug = CommandSpec.builder()
                .permission("ftbutilities.debug")
                .executor(new Debug())
                .child(activateListeners, "enable")
                .child(deactivateListeners, "disable")
                .build();

        CommandSpec main = CommandSpec.builder()
                .permission("ftbutilities.main")
                .child(debug, "debug")
                .build();
        this.registerListeners();
        Sponge.getCommandManager().register(this, main, "ftbupp");
    }

    public void registerListeners(){
        FtbUtilitiesSpongeIntegrationMod.INSTANCE.disableDefaultFtbHandler();
        listeners.forEach(handler->Sponge.getEventManager().registerListeners(this, handler));
    }

    public void deregisterListeners(){
        FtbUtilitiesSpongeIntegrationMod.INSTANCE.enableDefaultFtbHandler();
        Sponge.getEventManager().unregisterPluginListeners(this);
    }
}
