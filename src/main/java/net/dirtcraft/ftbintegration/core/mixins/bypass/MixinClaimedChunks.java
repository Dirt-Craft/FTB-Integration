package net.dirtcraft.ftbintegration.core.mixins.bypass;

import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import net.dirtcraft.ftbintegration.data.PlayerData;
import net.dirtcraft.ftbintegration.data.PlayerDataManager;
import net.dirtcraft.ftbintegration.utility.ClaimedChunkHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(value = ClaimedChunks.class, remap = false)
public class MixinClaimedChunks {
    @Shadow public static ClaimedChunks instance;

    @Redirect(method = "blockBlockEditing", at = @At(value = "INVOKE", target = "Lcom/feed_the_beast/ftbutilities/FTBUtilitiesPermissions;hasBlockEditingPermission(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/block/Block;)Z"))
    private static boolean hasBlockEditingPermission(EntityPlayer player, Block block, EntityPlayer p, BlockPos pos, @Nullable IBlockState state){
        ClaimedChunk chunk = instance.getChunk(new ChunkDimPos(pos, player.dimension));
        PlayerData data = PlayerDataManager.getInstance().get(player.getGameProfile());
        if (data == null) return FTBUtilitiesPermissions.hasBlockEditingPermission(player, block);
        else return data.hasBlockEditingPermission(block, chunk);
    }

    @Redirect(method = "blockBlockInteractions", at = @At(value = "INVOKE", target = "Lcom/feed_the_beast/ftbutilities/FTBUtilitiesPermissions;hasBlockInteractionPermission(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/block/Block;)Z"))
    private static boolean hasBlockInteractionPermission(EntityPlayer player, Block block, EntityPlayer p, BlockPos pos, @Nullable IBlockState state){
        ClaimedChunk chunk = instance.getChunk(new ChunkDimPos(pos, player.dimension));
        PlayerData data = PlayerDataManager.getInstance().get(player.getGameProfile());
        if (data == null) return FTBUtilitiesPermissions.hasBlockInteractionPermission(player, block);
        else return data.hasBlockInteractionPermission(block, chunk);
    }

    @Redirect(method = "blockItemUse", at = @At(value = "INVOKE", target = "Lcom/feed_the_beast/ftbutilities/FTBUtilitiesPermissions;hasItemUsePermission(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)Z"))
    private static boolean hasItemUsePermission(EntityPlayer player, net.minecraft.item.Item block, EntityPlayer p, EnumHand hand, BlockPos pos){
        ClaimedChunk chunk = instance.getChunk(new ChunkDimPos(pos, player.dimension));
        PlayerData data = PlayerDataManager.getInstance().get(player.getGameProfile());
        if (data == null) return FTBUtilitiesPermissions.hasItemUsePermission(player, block);
        else return data.hasItemUsePermission(block, chunk);
    }
}
