package com.nettakrim.souper_secret_settings.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(InputConstants.Key.class)
public interface KeyAccessor {
    @Accessor("NAME_MAP")
    static Map<String, InputConstants.Key> getNameMap() {
        throw new UnsupportedOperationException();
    }
}
