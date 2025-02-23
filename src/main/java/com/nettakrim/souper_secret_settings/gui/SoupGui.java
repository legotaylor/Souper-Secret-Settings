package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.layers.LayerScreen;
import com.nettakrim.souper_secret_settings.gui.parameters.ParameterScreen;
import com.nettakrim.souper_secret_settings.gui.shaders.ShaderScreen;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SoupGui {
    private final List<ClickableWidget> header;

    public final int[] currentScroll;

    protected static final int listGap = 2;
    protected static final int headerHeight = 42;

    private ScreenType currentScreenType;

    public SoupGui() {
        header = new ArrayList<>();

        int x;
        int mainWidth = 70;
        int smallWidth = 34;

        x = listGap;
        x += addHeaderButton(ButtonWidget.builder(Text.literal("..."),  (widget) -> open(ScreenType.LAYERS)).dimensions(x, listGap, (mainWidth*3+listGap*2)-(smallWidth*3+listGap*2)-listGap, 20).build());
        x += addHeaderButton(ButtonWidget.builder(Text.literal("undo"), (widget) -> undo()).dimensions(x, listGap, smallWidth, 20).build());
        x += addHeaderButton(ButtonWidget.builder(Text.literal("redo"), (widget) -> redo()).dimensions(x, listGap, smallWidth, 20).build());
             addHeaderButton(ButtonWidget.builder(SouperSecretSettingsClient.soupRenderer.getRenderTypeText(), SouperSecretSettingsClient.soupRenderer::cycleRenderType).dimensions(x, listGap, smallWidth, 20).build());

        x = listGap;
        x += addHeaderButton(ButtonWidget.builder(Text.literal("shaders"),    (widget) -> open(ScreenType.SHADERS   )).dimensions(x, listGap*2 + 20, mainWidth, 20).build());
        x += addHeaderButton(ButtonWidget.builder(Text.literal("effects"),    (widget) -> open(ScreenType.EFFECTS   )).dimensions(x, listGap*2 + 20, mainWidth, 20).build());
             addHeaderButton(ButtonWidget.builder(Text.literal("parameters"), (widget) -> open(ScreenType.PARAMETERS)).dimensions(x, listGap*2 + 20, mainWidth, 20).build());

        currentScroll = new int[ScreenType.values().length];
    }

    protected int addHeaderButton(ButtonWidget buttonWidget) {
        header.add(buttonWidget);
        return listGap + buttonWidget.getWidth();
    }

    public void open(ScreenType screenType) {
        currentScreenType = screenType;
        int index = screenType.ordinal();

        Screen screen = switch(screenType) {
            case LAYERS -> new LayerScreen(index);
            case SHADERS -> new ShaderScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer, Shaders.getMainRegistryId(),new Identifier[] {null});
            case EFFECTS -> new ShaderScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer, SoupRenderer.effectRegistry, new Identifier[] {
                    Identifier.of(SouperSecretSettingsClient.MODID, "before_layer_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "before_shader_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "after_shader_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "after_layer_render")
            });
            case PARAMETERS -> new ParameterScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer);
        };
        ClientData.minecraft.setScreen(screen);

        for (int i = 0; i < ScreenType.values().length; i++) {
            ClickableWidget clickableWidget = header.get(i > 0 ? i+3 : i);
            clickableWidget.active = index != i;
            clickableWidget.setFocused(false);
        }
        updateActiveLayer();
    }

    public List<ClickableWidget> getHeader() {
        return header;
    }

    protected void undo() {
        SouperSecretSettingsClient.actions.undo();
        open(currentScreenType);
    }

    protected void redo() {
        SouperSecretSettingsClient.actions.redo();
        open(currentScreenType);
    }

    public void setHistoryButtons(boolean undo, boolean redo) {
        header.get(1).active = undo;
        header.get(2).active = redo;
    }

    public ScreenType getCurrentScreenType() {
        return Objects.requireNonNullElse(currentScreenType, ScreenType.SHADERS);
    }

    public void updateActiveLayer() {
        header.getFirst().setMessage(Text.literal("layer: "+SouperSecretSettingsClient.soupRenderer.activeLayer.name));
    }

    public enum ScreenType {
        LAYERS,
        SHADERS,
        EFFECTS,
        PARAMETERS
    }
}
