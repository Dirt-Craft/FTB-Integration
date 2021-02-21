package net.dirtcraft.ftbutilities.spongeintegration.data.sponge;

import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public interface ImmutablePlayerSettings extends ImmutableDataManipulator<ImmutablePlayerSettings, PlayerSettings> {
    ImmutableValue<Boolean> canBypass();
    ImmutableValue<Boolean> isDebug();

}
