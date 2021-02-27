package net.dirtcraft.ftbintegration.core.api;

public interface DebugPlayerInfo {
    void updateLastSeenMs();

    long getLastSeenMs();

    String getElapsedString();
}
