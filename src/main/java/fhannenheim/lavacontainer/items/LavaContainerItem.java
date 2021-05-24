package fhannenheim.lavacontainer.items;

import fhannenheim.lavacontainer.LavaContainer;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LavaContainerItem extends Item {

    public LavaContainerItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 25;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (world.isClient || remainingUseTicks > 0)
            return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("mode_empty", !tag.getBoolean("mode_empty"));
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.7F, 0.7F);
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return true;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        CompoundTag tag = itemStack.getOrCreateTag();

        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            user.setCurrentHand(hand);
            return TypedActionResult.pass(itemStack);
        } else if (hitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(itemStack);
        } else {
            BlockHitResult blockHitResult = hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos placePos = blockPos.offset(direction);

            if (!tag.getBoolean("mode_empty"))
                if (world.getBlockState(blockPos).getBlock() == Blocks.LAVA && world.getBlockState(blockPos).getFluidState().isStill() && world.canPlayerModifyAt(user, blockPos) && tag.getInt("fill_level") < 64) {
                    tag.putInt("fill_level", tag.getInt("fill_level") + 1);
                    user.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                    return TypedActionResult.success(itemStack);
                }
            if (tag.getBoolean("mode_empty"))
                if (tag.getInt("fill_level") > 0 && (world.getBlockState(blockPos).getBlock() != Blocks.LAVA || !world.getBlockState(blockPos).getFluidState().isStill()) && world.canPlayerModifyAt(user, placePos)) {
                    world.setBlockState(placePos, Blocks.LAVA.getDefaultState(), 11);
                    world.playSound(user, placePos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    tag.putInt("fill_level", tag.getInt("fill_level") - 1);
                    return TypedActionResult.success(itemStack);
                }
        }
        return TypedActionResult.fail(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        CompoundTag tag = stack.getOrCreateTag();
        tooltip.add(new LiteralText("Fill Level: " + tag.getInt("fill_level")).formatted(Formatting.GRAY));
        tooltip.add(new LiteralText(tag.getBoolean("mode_empty") ? "Mode: Empty" : "Mode: Fill").formatted(Formatting.GRAY));
    }
}
