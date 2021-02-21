package net.dirtcraft.ftbintegration.data.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutablePlayerSettingsImpl extends AbstractImmutableData<ImmutablePlayerSettings, PlayerSettings> implements ImmutablePlayerSettings {

    private final ImmutableValue<Boolean> canBypass;
    private final ImmutableValue<Boolean> isDebug;

    public ImmutablePlayerSettingsImpl(){
        this(false, false);
    }

    public ImmutablePlayerSettingsImpl(boolean bypass, boolean debug){
        this.canBypass = Sponge.getRegistry().getValueFactory()
                .createValue(PlayerSettings.CAN_BYPASS, bypass)
                .asImmutable();

        this.isDebug = Sponge.getRegistry().getValueFactory()
                .createValue(PlayerSettings.IS_DEBUG, debug)
                .asImmutable();

    }

    @Override
    public ImmutableValue<Boolean> canBypass() {
        return this.canBypass;
    }

    @Override
    public ImmutableValue<Boolean> isDebug() {
        return this.isDebug;
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(PlayerSettings.CAN_BYPASS, this::canBypass);
        registerFieldGetter(PlayerSettings.CAN_BYPASS, this::canBypass);

        registerKeyValue(PlayerSettings.IS_DEBUG, this::isDebug);
        registerFieldGetter(PlayerSettings.IS_DEBUG, this::isDebug);
    }

    @Override
    public PlayerSettings asMutable() {
        return new PlayerSettingsImpl(this.canBypass().get(), this.isDebug().get());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }
}
