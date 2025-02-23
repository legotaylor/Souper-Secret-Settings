package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ListAdditionScreen<V> extends ScrollScreen {
    public ListScreen<V> listScreen;

    protected List<ClickableWidget> additions;

    protected String lastAddition;

    protected ListAdditionScreen(ListScreen<V> listScreen) {
        super(Text.literal(""));
        this.listScreen = listScreen;
        this.lastAddition = null;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("back"), (widget) -> close()).dimensions(SoupGui.listGap, SoupGui.listGap, ListScreen.listWidth+ListScreen.scrollWidth, SoupGui.headerHeight).build());

        createScrollWidget();

        additions = new ArrayList<>();
        for (String addition : listScreen.getAdditions()) {
            ClickableWidget clickableWidget = ButtonWidget.builder(Text.literal(addition), (widget) -> add(addition)).dimensions(ListScreen.listX, 0, ListScreen.listWidth, 20).build();
            additions.add(clickableWidget);
            addSelectableChild(clickableWidget);
        }

        scrollWidget.setContentHeight(additions.size()*(20+SoupGui.listGap) - SoupGui.listGap);
    }

    protected void add(String addition) {
        if (lastAddition != null) {
            if (lastAddition.equals(addition) && !lastAddition.startsWith("random")) {
                close();
                return;
            }

            SouperSecretSettingsClient.actions.undo();
            listScreen.listWidgets.removeLast();
        }

        listScreen.addAddition(addition);
        lastAddition = addition;
    }

    @Override
    public void renderScrollables(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Drawable drawable : additions) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void setScroll(int scroll) {
        int y = ListScreen.listStart - scroll;
        for (ClickableWidget widget : additions) {
            widget.setY(y);
            y += widget.getHeight()+SoupGui.listGap;
        }
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(listScreen);
    }
}
