package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectProcessorInterface;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.List;

public class ShaderWidget extends ListWidget {
    public ShaderStack stack;
    public ShaderData shaderData;

    public ShaderWidget(ShaderStack stack, ShaderData shaderData, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(shaderData.shader.getShaderId().toString()), listScreen);

        this.stack = stack;
        this.shaderData = shaderData;

        List<PostEffectPass> passes = ((PostEffectProcessorInterface)shaderData.shader.getPostProcessor()).luminance$getPasses(null);
        int i = 0;
        for (PostEffectPass postEffectPass : passes) {
            PassWidget passWidget = new PassWidget(this, postEffectPass, i, x, width, listScreen);
            children.add(passWidget);
            listScreen.addSelectable(passWidget);
            i++;
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean isActive() {
        return shaderData.active;
    }

    @Override
    public void setActive(boolean to) {
        shaderData.active = to;
    }
}
