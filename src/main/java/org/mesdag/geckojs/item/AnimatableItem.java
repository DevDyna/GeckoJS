package org.mesdag.geckojs.item;

import dev.latvian.mods.kubejs.item.custom.BasicItemJS;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class AnimatableItem extends BasicItemJS implements GeoItem {
    private final AnimatableInstanceCache CACHE = GeckoLibUtil.createInstanceCache(this);
    private final AnimatableItemBuilder itemBuilder;

    public AnimatableItem(AnimatableItemBuilder builder) {
        super(builder);
        this.itemBuilder = builder;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel && itemBuilder.useAnimationCallback != null) {
            itemBuilder.useAnimationCallback.call(this, serverLevel, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (level instanceof ServerLevel serverLevel && itemBuilder.finishUsingAnimationCallback != null) {
            itemBuilder.finishUsingAnimationCallback.call(this, serverLevel, livingEntity);
        }
        return super.finishUsingItem(itemStack, level, livingEntity);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity, int tick) {
        if (level instanceof ServerLevel serverLevel && itemBuilder.releaseUsingAnimationCallback != null) {
            itemBuilder.releaseUsingAnimationCallback.call(this, serverLevel, livingEntity, tick);
        }
        super.releaseUsing(itemStack, level, livingEntity, tick);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AnimatableItemRenderer<AnimatableItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    AnimatableItemRenderer<AnimatableItem> itemRenderer = new AnimatableItemRenderer<>(itemBuilder.itemModel);
                    if (itemBuilder.useEntityGuiLighting) itemRenderer.useAlternateGuiLighting();
                    this.renderer = itemRenderer;
                }
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        itemBuilder.controllers.forEach(controller -> registrar.add(controller.build(this)));
        itemBuilder.animations.forEach(animation -> registrar.add(new AnimationController<>(this, animation::create)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return CACHE;
    }
}
