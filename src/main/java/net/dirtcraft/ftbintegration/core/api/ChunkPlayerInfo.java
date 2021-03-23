package net.dirtcraft.ftbintegration.core.api;

public interface ChunkPlayerInfo {

    int getTotalClaims();

    int getTotalLoaders();

    int getAddClaims();

    int getAddLoaders();

    int getBaseClaims();

    int getBaseLoaders();

    void setBaseChunks();

    void setBaseChunks(int claims, int loaders);

    void setExtraChunks(int claims, int loaders);

    void modifyExtraChunks(int claims, int loaders);

    void recalculateTeamChunks();
}
