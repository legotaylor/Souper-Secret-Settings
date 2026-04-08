package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.nettakrim.souper_secret_settings.gui.CollapseWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BlockWidget extends CollapseWidget {
    public PassWidget pass;

    public UniformBlock block;
    public String blockName;

    public BlockWidget(PassWidget pass, UniformBlock block, String blockName, int x, int width, ListScreen<?> listScreen) {
        super(x, width, Component.literal(blockName), listScreen);
        this.pass = pass;
        this.block = block;
        this.blockName = blockName;
    }

    @Override
    protected void createChildren(int x, int width) {
        int i = 0;
        for (UniformInstance uniform : block.uniforms) {
            UniformWidget uniformWidget = new UniformWidget(this, uniform, Component.literal(uniform.name), i++, x, width, listScreen);
            listScreen.addSelectable(uniformWidget);
            children.add(uniformWidget);
        }
    }

    @Override
    protected boolean getStoredExpanded() {
        return true;
    }

    @Override
    protected void setStoredExpanded(boolean to) {

    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
