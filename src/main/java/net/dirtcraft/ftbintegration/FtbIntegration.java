package net.dirtcraft.ftbintegration;

import com.feed_the_beast.ftbutilities.handlers.FTBUtilitiesPlayerEventHandler;
import com.google.common.reflect.TypeToken;
import net.dirtcraft.ftbintegration.command.IntegrationBase;
import net.dirtcraft.ftbintegration.data.context.ClaimContextCalculator;
import net.dirtcraft.ftbintegration.data.sponge.ImmutablePlayerSettings;
import net.dirtcraft.ftbintegration.data.sponge.ImmutablePlayerSettingsImpl;
import net.dirtcraft.ftbintegration.data.sponge.PlayerSettings;
import net.dirtcraft.ftbintegration.data.sponge.PlayerSettingsImpl;
import net.dirtcraft.ftbintegration.handlers.forge.ChunkEventsHandler;
import net.dirtcraft.ftbintegration.handlers.forge.FTBPlayerDataHandler;
import net.dirtcraft.ftbintegration.handlers.forge.SpongePermissionHandler;
import net.dirtcraft.ftbintegration.handlers.sponge.*;
import net.dirtcraft.ftbintegration.storage.Configuration;
import net.dirtcraft.ftbintegration.storage.Database;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.config.SpongeConfigManager;

import java.io.IOException;
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
    public static final String VERSION = "@VERSION@";
    public static FtbIntegration INSTANCE;

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode node;
    private Configuration configuration;
    private Database database;

    public FtbIntegration(){
        INSTANCE = this;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, this);
        PermissionAPI.setPermissionHandler(SpongePermissionHandler.INSTANCE);
        PluginContainer container = Sponge.getPluginManager().getPlugin(MODID).orElse(()->MODID);
        this.loader = SpongeConfigManager.getPrivateRoot(container).getConfig();
        database = new Database();
    }

    @EventHandler
    public void init(FMLServerAboutToStartEvent event) {
        ClaimContextCalculator.register();
        MinecraftForge.EVENT_BUS.unregister(FTBUtilitiesPlayerEventHandler.class);
        MinecraftForge.EVENT_BUS.register(new FTBPlayerDataHandler());
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        LuckPermHandler.register();
        loadConfig();
        IntegrationBase.registerCommands(this);
        EventManager manager = Sponge.getEventManager();
        Stream.of(
                new NucleusHandler(),
                new BlockEventHandler(),
                new PlayerEventHandler(),
                new EntityEventHandler()
        ).forEach(h->manager.registerListeners(this, h));
        MinecraftForge.EVENT_BUS.register(new ChunkEventsHandler());
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

    @SuppressWarnings("UnstableApiUsage")
    public void loadConfig() {
        try {
            if (configuration == null) configuration = new Configuration();
            ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
            node = loader.load(options);
            configuration = node.getValue(TypeToken.of(Configuration.class), configuration);
            loader.save(node);
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void saveConfig(){
        try {
            if (node == null) return;
            loader.save(node.setValue(TypeToken.of(Configuration.class), configuration));
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }

    public Configuration getConfig(){
        return configuration;
    }

    public Database getDatabase() {
        return database;
    }
}
