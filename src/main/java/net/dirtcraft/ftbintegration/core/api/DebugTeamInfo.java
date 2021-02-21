package net.dirtcraft.ftbintegration.core.api;

import net.minecraft.util.text.ITextComponent;

public interface DebugTeamInfo {

    ITextComponent getDebugTitle();
    void regenerateDebugTitle();

}
