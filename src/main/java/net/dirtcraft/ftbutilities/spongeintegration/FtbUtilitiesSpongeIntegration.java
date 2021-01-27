package net.dirtcraft.ftbutilities.spongeintegration;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.events.RegisterRankConfigHandlerEvent;
import com.feed_the_beast.ftblib.lib.config.DefaultRankConfigHandler;
import com.feed_the_beast.ftblib.lib.config.IRankConfigHandler;
import com.feed_the_beast.ftblib.lib.config.RankConfigAPI;
import com.feed_the_beast.ftbutilities.FTBUtilitiesConfig;
import com.feed_the_beast.ftbutilities.handlers.FTBUtilitiesPlayerEventHandler;
import com.feed_the_beast.ftbutilities.ranks.FTBUtilitiesPermissionHandler;
import net.dirtcraft.ftbutilities.spongeintegration.command.debug.Debug;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.FTBPlayerDataHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.FTBProtectionHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.SpongePermissionHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.SpongeRankConfigHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.BlockEventHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.EntityEventHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.NucleusHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.PlayerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraftforge.fml.common.Mod.EventHandler;

@Mod(   modid = FtbUtilitiesSpongeIntegration.MODID,
        name = FtbUtilitiesSpongeIntegration.NAME,
        version = FtbUtilitiesSpongeIntegration.VERSION,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:ftbutilities")
public class FtbUtilitiesSpongeIntegration {
    public static final String MODID = "ftbu-sponge-integration";
    public static final String NAME = "FTB-Utilities Sponge Integration";
    public static final String VERSION = "${version}";
    public static FtbUtilitiesSpongeIntegration INSTANCE;
    private final FTBProtectionHandler defaultHandler = new FTBProtectionHandler();
    final List<Object> listeners = Stream.of(
            new NucleusHandler(),
            new BlockEventHandler(),
            new PlayerEventHandler(),
            new EntityEventHandler()
    ).collect(Collectors.toList());

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PermissionAPI.setPermissionHandler(SpongePermissionHandler.INSTANCE);
        Sponge.getEventManager().registerListeners(this, this);
        initRankHandler();
        INSTANCE = this;
    }

    @EventHandler
    public void init(FMLServerAboutToStartEvent event) {
        MinecraftForge.EVENT_BUS.unregister(FTBUtilitiesPlayerEventHandler.class);
        MinecraftForge.EVENT_BUS.register(new FTBPlayerDataHandler());
    }

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

    private void initRankHandler(){
        try {
            Method meth = RankConfigAPI.class.getDeclaredMethod("setHandler", IRankConfigHandler.class);
            meth.setAccessible(true);
            meth.invoke(null, new SpongeRankConfigHandler());
        } catch (Exception ignored){ }
    }

    public void registerListeners(){
        MinecraftForge.EVENT_BUS.unregister(defaultHandler);
        listeners.forEach(handler->Sponge.getEventManager().registerListeners(this, handler));
    }

    public void deregisterListeners(){
        MinecraftForge.EVENT_BUS.register(defaultHandler);
        Sponge.getEventManager().unregisterPluginListeners(this);
    }
}
