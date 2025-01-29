package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.interfaces.ShaderProgramInterface;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.CollapseWidget;
import com.nettakrim.souper_secret_settings.shaders.PassData;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class PassWidget extends CollapseWidget {
    public ShaderWidget shader;
    public PostEffectPass postEffectPass;
    public int passIndex;
    public Identifier customPass;

    protected boolean isFirstCustom;

    protected static int firstCustomHeight = 10;

    public PassWidget(ShaderWidget shader, PostEffectPass postEffectPass, Identifier customPass, int passIndex, int x, int width, ListScreen<?> listScreen) {
        super(x, width, Text.literal(((PostEffectPassInterface)postEffectPass).luminance$getID()), listScreen);

        this.shader = shader;
        this.postEffectPass = postEffectPass;
        this.customPass = customPass;
        this.passIndex = passIndex;

        ShaderProgram program = postEffectPass.getProgram();
        for (String name : ((ShaderProgramInterface)program).luminance$getUniformNames()) {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null && PassData.allowUniform(name)) {
                UniformWidget uniformWidget = new UniformWidget(this, uniform, Text.literal(uniform.getName()), x, width, listScreen);
                listScreen.addSelectable(uniformWidget);
                children.add(uniformWidget);
            }
        }

        if (children.isEmpty()) active = false;

        if (customPass != null && passIndex == 0) {
            setHeight(getHeight()+firstCustomHeight);
            isFirstCustom = true;
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int y = getY();
        if (isFirstCustom) {
            context.fill(getX(), y, getX() + getWidth(), y+firstCustomHeight, ColorHelper.fromFloats(0.4f, 0, 0, 0));
            drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, Text.literal(customPass.getPath()), this.getX()+2, y, this.getX()+this.getWidth()-2, y+firstCustomHeight, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
            y += firstCustomHeight;
        }

        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, y, this.getX()+this.getWidth()-2, y+20, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);

        if (expanded) {
            context.fill(getX(), getY() + getCollapseHeight(), getX() + getWidth(), y+20, ColorHelper.fromFloats(0.2f, 0, 0, 0));
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isFirstCustom && mouseY < getY() + firstCustomHeight) {
            return;
        }
        super.onClick(mouseX, mouseY);
    }
}
