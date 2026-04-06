package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.CollapseWidget;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class PassWidget extends CollapseWidget {
    public ShaderWidget shader;
    public PostPassInterface postPass;
    public int passIndex;
    public Identifier customPass;

    protected boolean isFirstCustom;

    protected static int firstCustomHeight = 10;

    public PassWidget(ShaderWidget shader, PostEffectPass postEffectPass, Identifier customPass, int passIndex, int x, int width, ListScreen<?> listScreen) {
        super(x, width, Text.literal(((PostPassInterface)postEffectPass).luminance$getID().replace(":post/",":")), listScreen);

        this.shader = shader;
        this.postPass = (PostPassInterface)postEffectPass;
        this.customPass = customPass;
        this.passIndex = passIndex;

        active = false;
        for (String blockName : postPass.luminance$getUniformBlockNames()) {
            UniformBlock block = postPass.luminance$getUniformBlock(blockName);
            if (!block.uniforms.isEmpty()) {
                active = true;
                break;
            }
        }


        if (customPass != null && passIndex == 0) {
            setHeight(getHeight()+firstCustomHeight);
            isFirstCustom = true;
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int y = getY();
        Style style = Style.EMPTY.withColor((this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
        if (isFirstCustom) {
            context.fill(getX(), y, getX() + getWidth(), y+firstCustomHeight, ColorHelper.fromFloats(0.4f, 0, 0, 0));
            context.getTextConsumer().text(Text.literal(customPass.getPath()).setStyle(style), this.getX()+2, this.getX()+this.getWidth()-2, y, y+firstCustomHeight);
            y += firstCustomHeight;
        }

        context.getTextConsumer().text(this.getMessage(), this.getX()+2, this.getX()+this.getWidth()-2, y, y+20);

        if (expanded) {
            context.fill(getX(), getY() + getCollapseHeight(), getX() + getWidth(), y+20, ColorHelper.fromFloats(0.2f, 0, 0, 0));
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void createChildren(int x, int width) {
        if (!active) {
            return;
        }

        for (String blockName : postPass.luminance$getUniformBlockNames()) {
            UniformBlock block = postPass.luminance$getUniformBlock(blockName);
            for (UniformInstance uniform : block.uniforms) {
                UniformWidget uniformWidget = new UniformWidget(this, uniform, Text.literal(uniform.name), x, width, listScreen);
                listScreen.addSelectable(uniformWidget);
                children.add(uniformWidget);
            }
        }

        if (children.isEmpty()) active = false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick(Click click, boolean doubled) {
        if (isFirstCustom && click.y() < getY() + firstCustomHeight) {
            return;
        }
        super.onClick(click, doubled);
    }

    @Override
    protected boolean getStoredExpanded() {
        return shader.shaderData.getPassData(customPass).expanded.get(passIndex);
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        shader.shaderData.getPassData(customPass).expanded.set(passIndex, to);
    }
}
