package com.nettakrim.souper_secret_settings.mixin;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EatingMixin extends Entity {
    @Shadow public abstract ItemStack getActiveItem();

    @Inject(at = @At("HEAD"), method = "consumeItem")
    protected void finishUsing(CallbackInfo ci) {
        if (getWorld().isClient && isLocalPlayerOrLogicalSideForUpdatingMovement()) {
            SouperSecretSettingsClient.consumeItem(getActiveItem());
        }
    }

    // needed for getWorld() access
    public EatingMixin(EntityType<?> type, World world) {super(type, world);}
}