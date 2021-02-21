package net.dirtcraft.ftbutilities.spongeintegration.data.sponge;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.TypeTokens;

@SuppressWarnings("UnstableApiUsage")
public interface PlayerSettings extends DataManipulator<PlayerSettings, ImmutablePlayerSettings> {
    Key<Value<Boolean>> CAN_BYPASS = Key.builder()
            .type(TypeTokens.BOOLEAN_VALUE_TOKEN)
            .name("Can Bypass Claims")
            .id("bypass")
            .query(PlayerSettingsImpl.BYPASS)
            .build();
    Key<Value<Boolean>> IS_DEBUG = Key.builder()
            .type(TypeTokens.BOOLEAN_VALUE_TOKEN)
            .name("Is Debug Claims")
            .id("debug")
            .query(PlayerSettingsImpl.DEBUG)
            .build();

    Value<Boolean> canBypass();
    Value<Boolean> isDebug();

}
