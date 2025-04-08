package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.option.OptionScreen;
import com.nettakrim.souper_secret_settings.gui.layers.LayerScreen;
import com.nettakrim.souper_secret_settings.gui.parameters.ParameterScreen;
import com.nettakrim.souper_secret_settings.gui.shaders.ShaderScreen;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoupGui {
    private final List<ClickableWidget> header;

    public final int[] currentScroll;

    public static final int listGap = 2;
    public static final int headerHeight = 42;
    public static final int listWidth = 150;
    public static final int listStart = headerHeight + listGap*2;
    public static final int scrollWidth = 6;
    public static final int listX = listGap*2+scrollWidth;
    public static final int headerWidth = 214;

    private ScreenType currentScreenType = ScreenType.SHADERS;
    private Text currentHoverText;

    private Screen previous = null;

    public SoupGui() {
        header = new ArrayList<>();

        int x;
        int mainWidth = (headerWidth-listGap*2)/3;
        int smallWidth = 12;

        x = listGap;
        x += addHeaderButton(ButtonWidget.builder(Text.literal(""),  (widget) -> open(ScreenType.LAYERS, false)).dimensions(x, listGap, (mainWidth*3+listGap*2)-(smallWidth*4+listGap*3)-listGap, 20).build());
        x += addHeaderButton(new HoverButtonWidget(x, listGap, smallWidth, 20, SouperSecretSettingsClient.translate("gui.undo"), null, (widget) -> undo()));
        x += addHeaderButton(new HoverButtonWidget(x, listGap, smallWidth, 20, SouperSecretSettingsClient.translate("gui.redo"), null, (widget) -> redo()));
        x += addHeaderButton(ButtonWidget.builder(SouperSecretSettingsClient.soupRenderer.getRenderTypeText(), SouperSecretSettingsClient.soupRenderer::cycleRenderType).dimensions(x, listGap, smallWidth, 20).build());
             addHeaderButton(ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.config"), (widget) -> open(ScreenType.OPTION, false)).dimensions(x, listGap, smallWidth, 20).build());

        x = listGap;
        x += addHeaderButton(ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.shaders"),    (widget) -> open(ScreenType.SHADERS   , false)).dimensions(x, listGap*2 + 20, mainWidth, 20).build());
        x += addHeaderButton(ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.modifiers"),  (widget) -> open(ScreenType.MODIFIERS , false)).dimensions(x, listGap*2 + 20, mainWidth, 20).build());
             addHeaderButton(ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.parameters"), (widget) -> open(ScreenType.PARAMETERS, false)).dimensions(x, listGap*2 + 20, mainWidth, 20).build());

        currentScroll = new int[ScreenType.values().length];
    }

    protected int addHeaderButton(ButtonWidget buttonWidget) {
        header.add(buttonWidget);
        return listGap + buttonWidget.getWidth();
    }

    public void open(ScreenType screenType, boolean openNew) {
        ClientData.minecraft.setScreen(getScreen(screenType, openNew));
    }

    public Screen getScreen(ScreenType screenType, boolean openNew) {
        if (currentScreenType == ScreenType.OPTION) {
            SouperSecretSettingsClient.soupData.saveConfig();
        }

        currentScreenType = screenType;
        int index = screenType.ordinal();

        if (openNew) {
            previous = ClientData.minecraft.currentScreen;
        }
        Screen screen = switch(screenType) {
            case LAYERS -> new LayerScreen(index);
            case SHADERS -> new ShaderScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer, Shaders.getMainRegistryId());
            case MODIFIERS -> new ShaderScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer, SoupRenderer.modifierRegistry);
            case PARAMETERS -> new ParameterScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer);
            case OPTION -> new OptionScreen(index);
        };

        for (int i = 0; i < ScreenType.values().length; i++) {
            ClickableWidget clickableWidget = header.get(i == 4 ? 4 : (i > 0 ? i+4 : i));
            clickableWidget.active = index != i;
            clickableWidget.setFocused(false);
        }
        updateActiveLayer();

        return screen;
    }

    public void onClose() {
        ClientData.minecraft.setScreen(previous);
        previous = null;
    }

    public List<ClickableWidget> getHeader() {
        return header;
    }

    protected void undo() {
        SouperSecretSettingsClient.actions.undo();
        open(currentScreenType, false);
        ClientData.minecraft.send(() -> ((HoverButtonWidget)header.get(1)).deselect());
    }

    protected void redo() {
        SouperSecretSettingsClient.actions.redo();
        open(currentScreenType, false);
        ClientData.minecraft.send(() -> ((HoverButtonWidget)header.get(2)).deselect());
    }

    public void setHistoryButtons(int undoCount, int redoCount) {
        ((HoverButtonWidget)header.get(1)).setActiveText(undoCount > 0 ? SouperSecretSettingsClient.translate("gui.undo_count", undoCount) : null);
        ((HoverButtonWidget)header.get(2)).setActiveText(redoCount > 0 ? SouperSecretSettingsClient.translate("gui.redo_count", redoCount) : null);
    }

    public ScreenType getCurrentScreenType() {
        return Objects.requireNonNullElse(currentScreenType, ScreenType.SHADERS);
    }

    public void updateActiveLayer() {
        header.getFirst().setMessage(SouperSecretSettingsClient.soupRenderer.activeLayer.name.isBlank() ? SouperSecretSettingsClient.translate("gui.layers.unnamed") : SouperSecretSettingsClient.translate("gui.layers", SouperSecretSettingsClient.soupRenderer.activeLayer.name));
    }

    public void setHoverText(Text text) {
        currentHoverText = text;
    }

    public void drawCurrentHoverText(DrawContext context, int mouseX, int mouseY) {
        if (currentHoverText == null) {
            return;
        }
        int offset = (mouseY > 30 && context.scissorContains(mouseX, mouseY-17)) ? -15 : 8;
        RenderSystem.depthMask(false);
        context.fill(mouseX-2, mouseY+offset-2, mouseX + ClientData.minecraft.textRenderer.getWidth(currentHoverText)+2, mouseY+offset+10, 8, ColorHelper.getArgb(128,0,0,0));
        context.drawText(ClientData.minecraft.textRenderer, currentHoverText, mouseX,  mouseY+offset, -1, true);
        RenderSystem.depthMask(true);
        currentHoverText = null;
    }

    public enum ScreenType implements StringIdentifiable {
        LAYERS,
        SHADERS,
        MODIFIERS,
        PARAMETERS,
        OPTION;

        @Override
        public String asString() {
            return name().toLowerCase();
        }
    }

    public static class ScreenTypeArgumentType extends EnumArgumentType<ScreenType> {
        public ScreenTypeArgumentType() {
            super(StringIdentifiable.createCodec(ScreenType::values), ScreenType::values);
        }
    }
}
