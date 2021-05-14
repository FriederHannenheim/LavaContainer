package fhannenheim.lavacontainer.items;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class LavaContainerItem extends Item {

    public LavaContainerItem(Settings settings) {
        super(settings);
    }

    // IDEA: Only raycast still lava but when a block is reached look at placepos and if its lava scoop that shit up
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        } else if (hitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(itemStack);
        } else {
            BlockHitResult blockHitResult = hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos placePos = blockPos.offset(direction);

            CompoundTag tag = itemStack.getOrCreateTag();
            if (!tag.contains("fill_level"))
                tag.putInt("fill_level", 0);

            if (world.getBlockState(blockPos).getBlock() == Blocks.LAVA && world.getBlockState(blockPos).getFluidState().isStill() && world.canPlayerModifyAt(user, blockPos) && tag.getInt("fill_level") < 64) {
                tag.putInt("fill_level", tag.getInt("fill_level") + 1);
                user.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                return TypedActionResult.success(itemStack);
            }
            else if (tag.getInt("fill_level") > 0 && (world.getBlockState(blockPos).getBlock() != Blocks.LAVA || !world.getBlockState(blockPos).getFluidState().isStill()) && world.canPlayerModifyAt(user, placePos)) {
                world.setBlockState(placePos, Blocks.LAVA.getDefaultState(), 11);
                world.playSound(user, placePos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
                tag.putInt("fill_level", tag.getInt("fill_level") - 1);
                return TypedActionResult.success(itemStack);
            }
        }
        return TypedActionResult.fail(itemStack);
    }
}
