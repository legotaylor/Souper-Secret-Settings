package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectProcessorInterface;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.OverrideManager;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ShaderWidget extends ListWidget {
    public ShaderLayer layer;
    public ShaderData shaderData;

    public ShaderWidget(ShaderLayer layer, ShaderData shaderData, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(shaderData.shader.getShaderId().toString()), listScreen);

        this.layer = layer;
        this.shaderData = shaderData;

        for (Identifier customPasses : ((ShaderScreen)listScreen).customPasses) {
            addPasses(customPasses);
        }
    }

    protected void addPasses(Identifier customPasses) {
        List<PostEffectPass> passes = ((PostEffectProcessorInterface)shaderData.shader.getPostProcessor()).luminance$getPasses(customPasses);
        if (passes == null) {
            return;
        }

        int i = 0;
        for (PostEffectPass postEffectPass : passes) {
            PassWidget passWidget = new PassWidget(this, postEffectPass, customPasses, i, getX(), width, listScreen);
            children.add(passWidget);
            listScreen.addSelectable(passWidget);
            i++;
        }
    }

    @Override
    protected boolean isActive() {
        return shaderData.active;
    }

    @Override
    protected void setActive(boolean to) {
        shaderData.active = to;
    }

    @Override
    protected boolean getStoredExpanded() {
        return shaderData.expanded;
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        shaderData.expanded = expanded;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (shaderData.active) {
            OverrideManager.currentShaderIndex++;
        }
    }
}
