package net.dirtcraft.ftbintegration.core.api;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;

public interface FlagTeamInfo {

    boolean blockMobSpawns();
    void setBlockMobSpawns(boolean value);

    boolean allowEntry(ForgePlayer player);
    EnumTeamStatus allowEntry();
    void setAllowEntryRank(EnumTeamStatus rank);

    boolean ejectEntrantSpawn();
    void setEjectEntrantSpawn(boolean val);
}
