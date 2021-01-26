package net.dirtcraft.ftbutilities.spongeintegration;

import com.feed_the_beast.ftbutilities.handlers.FTBUtilitiesPlayerEventHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.FTBPlayerDataHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.FTBProtectionHandler;
import net.dirtcraft.ftbutilities.spongeintegration.handlers.forge.SpongePermissionHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.server.permission.PermissionAPI;

import static net.minecraftforge.fml.common.Mod.EventHandler;

@Mod(   modid = FtbUtilitiesSpongeIntegrationMod.MODID,
        name = FtbUtilitiesSpongeIntegrationMod.NAME,
        version = FtbUtilitiesSpongeIntegrationMod.VERSION,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:ftbutilities")
public class FtbUtilitiesSpongeIntegrationMod {
    public static final String MODID = "ftbu-sponge-integration";
    public static final String NAME = "FTB-Utilities Sponge Integration";
    public static final String VERSION = "${version}";
    public static FtbUtilitiesSpongeIntegrationMod INSTANCE;

    private final FTBProtectionHandler defaultHandler = new FTBProtectionHandler();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PermissionAPI.setPermissionHandler(SpongePermissionHandler.INSTANCE);
        INSTANCE = this;
    }

    @EventHandler
    public void init(FMLServerAboutToStartEvent event) {
        MinecraftForge.EVENT_BUS.unregister(FTBUtilitiesPlayerEventHandler.class);
        MinecraftForge.EVENT_BUS.register(new FTBPlayerDataHandler());
    }

    public void enableDefaultFtbHandler(){
        MinecraftForge.EVENT_BUS.register(defaultHandler);
    }

    public void disableDefaultFtbHandler(){
        MinecraftForge.EVENT_BUS.unregister(defaultHandler);
    }
}
