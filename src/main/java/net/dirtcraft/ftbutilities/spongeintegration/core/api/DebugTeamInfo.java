package net.dirtcraft.ftbutilities.spongeintegration.core.api;

import net.minecraft.util.text.ITextComponent;

public interface DebugTeamInfo {

    ITextComponent getDebugTitle();
    void regenerateDebugTitle();

}
