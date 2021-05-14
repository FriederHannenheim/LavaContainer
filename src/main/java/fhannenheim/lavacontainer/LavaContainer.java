package fhannenheim.lavacontainer;

import fhannenheim.lavacontainer.items.LavaContainerItem;
import fhannenheim.lavacontainer.mixin.ModelPredicateProviderRegistryMixin;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class LavaContainer implements ModInitializer {
    public static final Item LAVA_CONTAINER = new LavaContainerItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1));

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier("lavacontainer", "lava_container"), LAVA_CONTAINER);

        ModelPredicateProviderRegistryMixin.register(LAVA_CONTAINER, new Identifier("fill_level"), (stack, world, entity) -> {
            CompoundTag tag = stack.getOrCreateTag();
            return tag.getInt("fill_level") == 0 ? 0 : (int) Math.floor(tag.getInt("fill_level") / 8f) + 1;
        });
    }
}
