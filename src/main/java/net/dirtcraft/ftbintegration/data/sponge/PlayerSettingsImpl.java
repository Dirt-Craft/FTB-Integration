package net.dirtcraft.ftbintegration.data.sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.mutable.Value;

import javax.annotation.Nonnull;
import java.util.Optional;

public class PlayerSettingsImpl extends AbstractData<PlayerSettings, ImmutablePlayerSettings> implements PlayerSettings {
    public static final DataQuery BYPASS = DataQuery.of("bypass");
    public static final DataQuery DEBUG = DataQuery.of("debug");
    public static final DataQuery BADGE = DataQuery.of("badge");

    private boolean bypass;
    private boolean debug;
    private String badgeUrl;

    public PlayerSettingsImpl(){
        this(false, false, "");
    }

    public PlayerSettingsImpl(boolean bypass, boolean debug, String badgeUrl){
        this.bypass = bypass;
        this.debug = debug;
        this.badgeUrl = badgeUrl;
        registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(CAN_BYPASS, () -> this.bypass);
        registerFieldSetter(CAN_BYPASS, bypass -> this.bypass = bypass);
        registerKeyValue(CAN_BYPASS, this::canBypass);

        registerFieldGetter(IS_DEBUG, () -> this.debug);
        registerFieldSetter(IS_DEBUG, debug -> this.debug = debug);
        registerKeyValue(IS_DEBUG, this::isDebug);

        registerFieldGetter(GET_BADGE, () -> this.badgeUrl);
        registerFieldSetter(GET_BADGE, debug -> this.badgeUrl = debug);
        registerKeyValue(GET_BADGE, this::getBadge);
    }

    @Override
    public Value<Boolean> canBypass() {
        return Sponge.getRegistry()
                .getValueFactory()
                .createValue(CAN_BYPASS, this.bypass);
    }

    @Override
    public Value<Boolean> isDebug() {
        return Sponge.getRegistry()
                .getValueFactory()
                .createValue(IS_DEBUG, this.debug);
    }

    @Override
    public Value<String> getBadge() {
        return Sponge.getRegistry()
                .getValueFactory()
                .createValue(GET_BADGE, this.badgeUrl);
    }

    @Override
    public Optional<PlayerSettings> fill(DataHolder dataHolder, @Nonnull MergeFunction overlap) {
        dataHolder.get(PlayerSettings.class).ifPresent(data -> data.set(overlap.merge(this, data).getValues()));
        return Optional.of(this);
    }

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Optional<PlayerSettings> from(DataContainer container) {
        if (!container.contains(BYPASS, DEBUG)) return Optional.empty();

        final boolean bypass = container.getBoolean(BYPASS).get();
        final boolean debug = container.getBoolean(DEBUG).get();
        final String badge = container.contains(BADGE)? container.getString(BADGE).get() : "";

        this.set(CAN_BYPASS, bypass);
        this.set(IS_DEBUG, debug);
        this.set(GET_BADGE, badge);

        return Optional.of(this);
    }

    @Override
    public PlayerSettingsImpl copy() {
        return new PlayerSettingsImpl(this.bypass, this.debug, this.badgeUrl);
    }
    @Override
    public ImmutablePlayerSettings asImmutable() {
        return new ImmutablePlayerSettingsImpl(this.bypass, this.debug, this.badgeUrl);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(BYPASS, this.bypass)
                .set(DEBUG, this.debug)
                .set(BADGE, this.badgeUrl);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    public static final class Builder extends AbstractDataBuilder<PlayerSettings> implements DataManipulatorBuilder<PlayerSettings, ImmutablePlayerSettings> {

        public Builder() {
            super(PlayerSettings.class, 1);
        }

        @Override
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        protected Optional<PlayerSettings> buildContent(DataView container) throws InvalidDataException {
            if (!container.contains(BYPASS, DEBUG)) return Optional.empty();

            final boolean bypass = container.getBoolean(BYPASS).get();
            final boolean debug = container.getBoolean(DEBUG).get();
            final String badge = container.contains(BADGE)? container.getString(BADGE).get() : "";

            return Optional.of(new PlayerSettingsImpl(bypass, debug, badge));
        }

        @Override
        public PlayerSettingsImpl create() {
            return new PlayerSettingsImpl();
        }

        @Override
        public Optional<PlayerSettings> createFrom(DataHolder dataHolder) {
            Optional<PlayerSettings> settings = dataHolder.get(PlayerSettings.class);
            return Optional.of(settings.orElseGet(this::create));
        }
    }
}
