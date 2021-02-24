package net.dirtcraft.ftbintegration;

import com.feed_the_beast.ftbutilities.handlers.FTBUtilitiesPlayerEventHandler;
import net.dirtcraft.ftbintegration.command.Base;
import net.dirtcraft.ftbintegration.data.context.ClaimContextCalculator;
import net.dirtcraft.ftbintegration.data.sponge.ImmutablePlayerSettings;
import net.dirtcraft.ftbintegration.data.sponge.ImmutablePlayerSettingsImpl;
import net.dirtcraft.ftbintegration.data.sponge.PlayerSettings;
import net.dirtcraft.ftbintegration.data.sponge.PlayerSettingsImpl;
import net.dirtcraft.ftbintegration.handlers.forge.ChunkEventsHandler;
import net.dirtcraft.ftbintegration.handlers.forge.FTBPlayerDataHandler;
import net.dirtcraft.ftbintegration.handlers.forge.SpongePermissionHandler;
import net.dirtcraft.ftbintegration.handlers.sponge.BlockEventHandler;
import net.dirtcraft.ftbintegration.handlers.sponge.EntityEventHandler;
import net.dirtcraft.ftbintegration.handlers.sponge.NucleusHandler;
import net.dirtcraft.ftbintegration.handlers.sponge.PlayerEventHandler;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraftforge.fml.common.Mod.EventHandler;

@Mod(   modid = FtbIntegration.MODID,
        name = FtbIntegration.NAME,
        version = FtbIntegration.VERSION,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:ftbutilities")
public class FtbIntegration {
    public static final String MODID = "ftb-integration";
    public static final String NAME = "FTB Utilities Sponge Integration";
    public static final String VERSION = "${version}";
    public static FtbIntegration INSTANCE;
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

    public void registerListeners(){
        MinecraftForge.EVENT_BUS.register(chunkHandler);
        listeners.forEach(handler->Sponge.getEventManager().registerListeners(this, handler));
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
