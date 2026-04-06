package com.nettakrim.souper_secret_settings.mixin;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EatingMixin extends Entity {
    @Shadow public abstract ItemStack getUseItem();

    @Inject(at = @At("HEAD"), method = "completeUsingItem")
    protected void finishUsing(CallbackInfo ci) {
        if (level().isClientSide()) {
            SouperSecretSettingsClient.consumeItem(getUseItem());
        }
    }

    // needed for getWorld() access
    public EatingMixin(EntityType<?> type, Level world) {super(type, world);}
}