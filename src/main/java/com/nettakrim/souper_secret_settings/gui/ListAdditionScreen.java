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

    protected List<ClickableWidget> children;

    protected String lastAddition;

    protected int headerHeight;

    protected ListAdditionScreen(ListScreen<V> listScreen) {
        super(Text.empty());
        this.listScreen = listScreen;
        this.lastAddition = null;
    }

    @Override
    protected void init() {
        headerHeight = createHeader();
        createScrollWidget(headerHeight);

        children = new ArrayList<>();
        for (String addition : listScreen.getAdditions()) {
            createAdditionButton(addition);
        }

        scrollWidget.setContentHeight(children.size()*(20+SoupGui.listGap) - SoupGui.listGap);
    }

    protected int createHeader() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), (widget) -> close()).dimensions(SoupGui.listGap, SoupGui.listGap, SoupGui.headerWidthSmall, 20).build());

        return 20+SoupGui.listGap*2;
    }

    protected void createAdditionButton(String addition) {
        AdditionButton additionButton = new AdditionButton(addition, listScreen.getAdditionText(addition), SoupGui.listX, SoupGui.listWidth, 20, this::add);
        if (listScreen.canRemoveAddition(addition)) {
            additionButton.addRemoveListener(this::removeAddition);
        }
        children.add(additionButton);
        addSelectableChild(additionButton);
    }

    protected void add(String addition) {
        if (lastAddition != null) {
            if (lastAddition.equals(addition) && !(listScreen.canUseRandom() && lastAddition.startsWith("random"))) {
                close();
                return;
            }

            SouperSecretSettingsClient.actions.undo();
            listScreen.listWidgets.removeLast();
        }

        if (listScreen.addAddition(addition) == null) {
            lastAddition = null;
        } else {
            lastAddition = addition;
        }

        if (!listScreen.canPreview()) {
            close();
        }
    }

    @Override
    public void renderScrollables(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Drawable drawable : children) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void setScroll(int scroll) {
        int y = headerHeight - scroll;
        for (ClickableWidget widget : children) {
            widget.setY(y);
            y += widget.getHeight()+SoupGui.listGap;
        }
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(listScreen);
    }

    protected void removeAddition(AdditionButton button) {
        remove(button);
        children.remove(button);
        scrollWidget.setContentHeight(children.size()*(20+SoupGui.listGap) - SoupGui.listGap);

        listScreen.removeAddition(button.addition);
    }
}
