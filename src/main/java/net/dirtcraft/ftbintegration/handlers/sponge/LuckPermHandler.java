package net.dirtcraft.ftbintegration.handlers.sponge;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import net.dirtcraft.ftbintegration.core.api.ChunkPlayerInfo;
import net.dirtcraft.ftbintegration.core.api.DebugPlayerInfo;
import net.dirtcraft.ftbintegration.handlers.forge.SpongePermissionHandler;
import net.dirtcraft.ftbintegration.storage.Database;
import net.dirtcraft.ftbintegration.storage.Permission;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.event.player.PlayerLoginProcessEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public enum LuckPermHandler {
    INSTANCE;

    private final LuckPerms api = LuckPermsProvider.get();
    private Database database;
    private Universe universe;
    private String regex;

    public static void register(){
        INSTANCE.registerHandlers();
    }

    private void registerHandlers(){
        EventBus eventBus = api.getEventBus();
        eventBus.subscribe(NodeAddEvent.class, this::onNodeAdd);
        eventBus.subscribe(NodeRemoveEvent.class, this::onNodeRemove);
        eventBus.subscribe(PlayerLoginProcessEvent.class, this::onPlayerLogin);
    }

    public void onNodeRemove(NodeRemoveEvent event){
        onNodeModify(event.getTarget(), event.getNode());
    }

    public void onNodeAdd(NodeAddEvent event) {
        onNodeModify(event.getTarget(), event.getNode());
    }

    private String getMetaRegex(){
        if (regex == null){
            String base = Permission.CLAIMS_BASE;
            String claim = Permission.CHUNK_CLAIM_META.substring(base.length()+1);
            String loader = Permission.CHUNK_LOADER_META.substring(base.length()+1);
            base = base.replaceAll("\\.", "\\\\?.");
            this.regex = String.format("%s\\?.(%s|%s)\\?.\\d+", base, claim, loader);
        }
        return regex;
    }

    private void onNodeModify(PermissionHolder target, Node node) {
        if (target instanceof User && node.getType() == NodeType.INHERITANCE) {
            User user = (User) target;
            ForgePlayer player = getPlayer(user);
            if (player instanceof ChunkPlayerInfo) setPlayerBaseChunkData((ChunkPlayerInfo) player, user);
        } else if (target instanceof Group && node.getType() == NodeType.META) {
            final String key = node.getKey().replaceAll("\\\\", "");
            if (key.matches(getMetaRegex())) return;
            getPlayers().stream()
                    .filter(ForgePlayer::isOnline)
                    .filter(ChunkPlayerInfo.class::isInstance)
                    .map(ChunkPlayerInfo.class::cast)
                    .forEach(ChunkPlayerInfo::setBaseChunks);
        }
    }

    public void onPlayerLogin(PlayerLoginProcessEvent event) {
        User user = event.getUser();
        ForgePlayer player = getPlayer(user);
        if (player == null) return;
        ifInstance(DebugPlayerInfo.class, player, DebugPlayerInfo::updateLastSeenMs);
        ifInstance(ChunkPlayerInfo.class, player, ch-> {
            setPlayerBaseChunkData(ch, user);
            setAdditionalChunkData(ch, user);
        });
    }

    public Collection<ForgePlayer> getPlayers(){
        if (universe == null) universe = Universe.get();
        return universe.getPlayers();
    }

    public ForgePlayer getPlayer(User user){
        if (universe == null) universe = Universe.get();
        return universe.getPlayer(user.getUniqueId());
    }

    private <T> void ifInstance(Class<T> clazz, Object obj, Consumer<T> exec){
        if (clazz.isInstance(obj)) exec.accept(clazz.cast(obj));
    }

    public static void setPlayerBaseChunkData(ChunkPlayerInfo info, User user){
        SpongePermissionHandler handler = SpongePermissionHandler.INSTANCE;
        int baseClaim = handler.getMetaOrDefault(user, Permission.CHUNK_CLAIM_META, Integer::valueOf, info.getBaseClaims());
        int baseLoad = handler.getMetaOrDefault(user, Permission.CHUNK_LOADER_META, Integer::valueOf, info.getBaseLoaders());
        info.setBaseChunks(baseClaim, baseLoad);
    }

    private void setAdditionalChunkData(ChunkPlayerInfo info, User user) {
        if (database == null) database = new Database();
        try {
            Optional<Database.ChunkData> optData = database.getChunkData(user.getUniqueId());
            if (optData.isPresent()) {
                Database.ChunkData data = optData.get();
                info.setExtraChunks(data.claims, data.loaders);
            } else {
                database.createRecord(user.getUniqueId());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
