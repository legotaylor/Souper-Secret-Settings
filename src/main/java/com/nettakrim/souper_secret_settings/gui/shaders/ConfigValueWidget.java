package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.SuggestionTextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ConfigValueWidget extends TextWidget {
    public final String name;

    protected boolean visible;

    protected final List<SuggestionTextFieldWidget> children;

    protected Consumer<ConfigValueWidget> onChangeCallback;

    public List<Object> objects;

    public ConfigValueWidget(int x, int width, int height, String name, @NotNull List<Object> objects, @NotNull List<Object> defaultObjects) {
        super(x, 0, width, height, Text.literal(name), ClientData.minecraft.textRenderer);

        this.name = name;
        this.objects = new ArrayList<>(objects);

        children = new ArrayList<>();
        if (!objects.isEmpty()) {
            int childStart = width / 3;
            setWidth(childStart);
            int childWidth = (width - childStart) / objects.size();
            for (int i = 0; i < objects.size(); i++) {
                SuggestionTextFieldWidget textFieldWidget = new SuggestionTextFieldWidget(x + childStart + (childWidth * i), childWidth, height, Text.literal(String.valueOf(i)), true);
                int finalI = i;
                Object object = objects.get(i);
                textFieldWidget.setText(String.valueOf(object));
                textFieldWidget.setChangedListener((s) -> valueChanged(s, finalI));
                textFieldWidget.setListeners(() -> Collections.singletonList(String.valueOf(defaultObjects.get(finalI))), null, false);
                children.add(textFieldWidget);

                if (object instanceof String) {
                    textFieldWidget.disableDrag = true;
                }
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (visible) {
            super.renderWidget(context, mouseX, mouseY, delta);
            for (SuggestionTextFieldWidget textFieldWidget : children) {
                textFieldWidget.renderWidget(context, mouseX, mouseY, delta);
            }
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        for (SuggestionTextFieldWidget textFieldWidget : children) {
            textFieldWidget.setVisible(visible);
        }
    }

    public void addToScreen(ListScreen<?> listScreen) {
        listScreen.addSelectable(this);
        for (SuggestionTextFieldWidget textFieldWidget : children) {
            listScreen.addSelectable(textFieldWidget);
        }
    }

    public void removeFromScreen(ListScreen<?> listScreen) {
        listScreen.removeSelectable(this);
        for (SuggestionTextFieldWidget textFieldWidget : children) {
            listScreen.removeSelectable(textFieldWidget);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (SuggestionTextFieldWidget textFieldWidget : children) {
            textFieldWidget.setY(y);
        }
    }

    public void valueChanged(String s, int i) {
        Object objectAtIndex = objects.get(i);
        Object object;
        if (objectAtIndex == null || objectAtIndex instanceof Number) {
            try {
                object = Float.parseFloat(s);
            } catch (Exception ignored) {
                if (s.equals("null")) {
                    object = null;
                } else {
                    return;
                }
            }
        } else {
            object = s;
        }

        objects.set(i, object);
        if (onChangeCallback != null) {
            onChangeCallback.accept(this);
        }
    }

    public void setChangedListener(Consumer<ConfigValueWidget> callback) {
        onChangeCallback = callback;
    }
}
