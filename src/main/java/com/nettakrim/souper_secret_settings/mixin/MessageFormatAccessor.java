package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.command.argument.MessageArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MessageArgumentType.MessageFormat.class)
public interface MessageFormatAccessor {
    @Accessor
    String getContents();
}
