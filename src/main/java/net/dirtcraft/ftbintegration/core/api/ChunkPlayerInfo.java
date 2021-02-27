package net.dirtcraft.ftbintegration.core.api;

public interface ChunkPlayerInfo {

    int getClaims();

    int getLoaders();

    void setClaims(int amount);

    void setLoaders(int amount);

    void loadChunkData();
}
