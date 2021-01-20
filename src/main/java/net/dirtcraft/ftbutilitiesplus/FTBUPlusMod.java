package net.dirtcraft.ftbutilitiesplus;

import net.dirtcraft.ftbutilitiesplus.handlers.SpongePermissionHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.server.permission.PermissionAPI;

@Mod(modid = FTBUPlusMod.MODID, name = FTBUPlusMod.NAME, version = FTBUPlusMod.VERSION, acceptableRemoteVersions = "*", dependencies = "required-after:ftbutilities")
public class FTBUPlusMod {
    public static final String MODID = "ftbutilitiesplus";
    public static final String NAME = "FTB-Utilities-Plus";
    public static final String VERSION = "${version}";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PermissionAPI.setPermissionHandler(SpongePermissionHandler.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }
}
