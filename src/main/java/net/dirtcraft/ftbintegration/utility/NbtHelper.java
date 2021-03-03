package net.dirtcraft.ftbintegration.utility;

import net.minecraft.nbt.NBTTagCompound;

public class NbtHelper {
    public static int getOrDefault(NBTTagCompound nbt, String key, int def) {
        if (nbt.hasKey(key)) return nbt.getInteger(key);
        else return def;
    }
    public static long getOrDefault(NBTTagCompound nbt, String key, long def) {
        if (nbt.hasKey(key)) return nbt.getLong(key);
        else return def;
    }
}
