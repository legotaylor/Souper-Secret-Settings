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
    protected static final int headerHeight = 20;

    private ScreenType currentScreenType;

    public SoupGui() {
        header = new ArrayList<>();
        int radioWidth = 70;
        int x = listGap;
        for (ScreenType screenType : ScreenType.values()) {
            header.add(ButtonWidget.builder(Text.literal(screenType.name().toLowerCase()), (widget) -> open(screenType)).dimensions(x, listGap, radioWidth, headerHeight).build());
            x += listGap + radioWidth;
        }

        header.add(ButtonWidget.builder(Text.literal("undo"), (widget) -> undo()).dimensions(x, listGap, 34, headerHeight).build());
        header.add(ButtonWidget.builder(Text.literal("redo"), (widget) -> redo()).dimensions(x+36, listGap, 34, headerHeight).build());

        header.add(ButtonWidget.builder(Text.literal(SouperSecretSettingsClient.soupRenderer.renderType.toString()), SouperSecretSettingsClient.soupRenderer::cycleRenderType).dimensions(x+72, listGap, 34, headerHeight).build());


        currentScroll = new int[ScreenType.values().length];
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
            ClickableWidget clickableWidget = header.get(i);
            clickableWidget.active = index != i;
            clickableWidget.setFocused(false);
        }
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
        header.get(ScreenType.values().length).active = undo;
        header.get(ScreenType.values().length+1).active = redo;
    }

    public ScreenType getCurrentScreenType() {
        return Objects.requireNonNullElse(currentScreenType, ScreenType.SHADERS);
    }

    public enum ScreenType {
        LAYERS,
        SHADERS,
        EFFECTS,
        PARAMETERS
    }
}
