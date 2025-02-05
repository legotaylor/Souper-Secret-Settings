package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.parameters.ParameterScreen;
import com.nettakrim.souper_secret_settings.gui.shaders.StackScreen;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SoupGui {
    private final List<ClickableWidget> header;

    protected static final int listGap = 2;
    protected static final int headerHeight = 20;

    public SoupGui() {
        header = new ArrayList<>();
        int radioWidth = 75;
        int x = listGap;
        for (ScreenType screenType : ScreenType.values()) {
            header.add(ButtonWidget.builder(Text.literal(screenType.name().toLowerCase()), (widget) -> open(screenType)).dimensions(x, listGap, radioWidth, headerHeight).build());
            x += listGap + radioWidth;
        }
    }

    public void open(ScreenType screenType) {
        Screen screen = switch(screenType) {
            case SHADERS -> new StackScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack(), Shaders.getMainRegistryId(),new Identifier[] {null});
            case EFFECTS -> new StackScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack(), SoupRenderer.layerEffectRegistry, new Identifier[] {
                    Identifier.of(SouperSecretSettingsClient.MODID, "before_stack_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "before_shader_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "after_shader_render"),
                    Identifier.of(SouperSecretSettingsClient.MODID, "after_stack_render")
            });
            case PARAMETERS -> new ParameterScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack());
        };
        ClientData.minecraft.setScreen(screen);

        int radio = screenType.ordinal();

        for (int i = 0; i < ScreenType.values().length; i++) {
            ClickableWidget clickableWidget = header.get(i);
            clickableWidget.active = radio != i;
            clickableWidget.setFocused(false);
        }
    }

    public List<ClickableWidget> getHeader() {
        return header;
    }

    public enum ScreenType {
        SHADERS,
        EFFECTS,
        PARAMETERS
    }
}
