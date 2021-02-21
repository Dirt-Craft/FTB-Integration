package net.dirtcraft.ftbutilities.spongeintegration;

import com.feed_the_beast.ftblib.lib.config.IRankConfigHandler;
import com.feed_the_beast.ftblib.lib.config.RankConfigAPI;
import com.feed_the_beast.ftbutilities.handlers.FTBUtilitiesPlayerEventHandler;
import net.dirtcraft.ftbutilities.spongeintegration.command.Base;
import net.dirtcraft.ftbutilities.spongeintegration.data.ClaimContextCalculator;
import net.dirtcraft.ftbutilities.spongeintegration.data.sponge.ImmutablePlayerSettings;
import net.dirtcraft.ftbutilities.spongeintegration.data.sponge.ImmutablePlayerSettingsImpl;
import net.dirtcraft.ftbutilities.spongeintegration.data.sponge.PlayerSettings;
import net.dirtcraft.ftbutilities.spongeintegration.data.sponge.PlayerSettingsImpl;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.*;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.BlockEventHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.EntityEventHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.NucleusHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.sponge.PlayerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
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
    final ChunkEventsHandler chunkHandler = new ChunkEventsHandler();
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
        //initRankHandler(); //todo make a nice method for getting all the claims meta during login async.
        INSTANCE = this;
    }

    @EventHandler
    public void init(FMLServerAboutToStartEvent event) {
        ClaimContextCalculator.register();
        MinecraftForge.EVENT_BUS.unregister(FTBUtilitiesPlayerEventHandler.class);
        MinecraftForge.EVENT_BUS.register(new FTBPlayerDataHandler());
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        Base.registerCommands(this);
        this.registerListeners();
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
        MinecraftForge.EVENT_BUS.register(chunkHandler);
        listeners.forEach(handler->Sponge.getEventManager().registerListeners(this, handler));
    }

    public void deregisterListeners(){
        MinecraftForge.EVENT_BUS.register(defaultHandler);
        MinecraftForge.EVENT_BUS.unregister(chunkHandler);
        Sponge.getEventManager().unregisterPluginListeners(this);
    }

    @Listener
    public void onDataRegistry(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        DataRegistration.builder()
                .id(MODID)
                .name(NAME)
                .dataClass(PlayerSettings.class)
                .immutableClass(ImmutablePlayerSettings.class)
                .dataImplementation(PlayerSettingsImpl.class)
                .immutableImplementation(ImmutablePlayerSettingsImpl.class)
                .builder(new PlayerSettingsImpl.Builder())
                .build();
    }

    @Listener
    public void onKeyRegistry(GameRegistryEvent.Register<Key<?>> event) {
        event.register(PlayerSettings.CAN_BYPASS);
        event.register(PlayerSettings.IS_DEBUG);
    }
}
