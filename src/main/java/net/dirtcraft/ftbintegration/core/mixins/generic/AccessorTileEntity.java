package net.dirtcraft.ftbintegration.core.mixins.generic;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TileEntity.class)
public interface AccessorTileEntity {

    @Invoker("markDirty") void markAsDirty();

}
