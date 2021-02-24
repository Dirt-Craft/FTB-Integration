package net.dirtcraft.ftbintegration.core.mixins.generic;

import com.feed_the_beast.ftblib.lib.util.FinalIDObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FinalIDObject.class, remap = false)
public interface AccessorFinalIDObject {
    @Accessor("id") String getTeamIdString();
}
