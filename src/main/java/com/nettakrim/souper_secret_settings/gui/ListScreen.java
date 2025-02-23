package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ListRemoveAction;
import com.nettakrim.souper_secret_settings.actions.ListSwapAction;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ListScreen<V> extends ScrollScreen {
    protected ArrayList<ListWidget> listWidgets;

    protected SuggestionTextFieldWidget suggestionTextFieldWidget;
    protected ButtonWidget suggestionScreenButton;

    protected static final int listWidth = 150;
    protected static final int listStart = SoupGui.headerHeight + SoupGui.listGap*2;
    protected static final int scrollWidth = 6;
    protected static final int listX = SoupGui.listGap*2+scrollWidth;

    protected int currentListSize;

    private final int scrollIndex;

    protected ListScreen(int scrollIndex) {
        super(Text.literal(""));
        this.scrollIndex = scrollIndex;
    }

    @Override
    protected void init() {
        for (ClickableWidget clickableWidget : SouperSecretSettingsClient.soupGui.getHeader()) {
            addDrawableChild(clickableWidget);
        }

        createScrollWidget();

        List<V> listValues = getListValues();
        listWidgets = new ArrayList<>(listValues.size());
        for (V value : listValues) {
            ListWidget listWidget = createListWidget(value);
            addSelectableChild(listWidget);
            listWidgets.add(listWidget);
        }

        suggestionTextFieldWidget = new SuggestionTextFieldWidget(listX, listWidth-22, 20, Text.literal("list addition"), false);
        suggestionTextFieldWidget.setListeners(this::getAdditions, this::addAddition);
        addDrawableChild(suggestionTextFieldWidget);

        suggestionScreenButton = ButtonWidget.builder(Text.literal("+"), (widget) -> enterAdditionScreen()).dimensions(listX+listWidth-20, 0, 20, 20).build();
        addDrawableChild(suggestionScreenButton);

        int scroll = SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex];
        updateSpacing();
        scrollWidget.offsetScroll(scroll);
    }

    @Override
    public void renderScrollables(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Drawable drawable : listWidgets) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }

    protected abstract List<V> getListValues();

    protected abstract ListWidget createListWidget(V value);

    public <T extends Element & Selectable> void addSelectable(T child) {
        addSelectableChild(child);
    }

    public void removeSelectable(Element child) {
        remove(child);
    }

    public void updateSpacing() {
        currentListSize = listStart;
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.visible = true;
            collapseWidget.updateCollapse(currentListSize);
            currentListSize += collapseWidget.getCollapseHeight() + SoupGui.listGap;
        }

        scrollWidget.setContentHeight(currentListSize-listStart + suggestionTextFieldWidget.getHeight());
    }

    @Override
    public void setScroll(int scroll) {
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.setY(collapseWidget.offset - scroll);
        }

        suggestionTextFieldWidget.setY(currentListSize - scroll);
        suggestionScreenButton.setY(suggestionTextFieldWidget.getY());
        SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex] = scroll;
    }

    protected void addAddition(String addition) {
        V entry = tryGetAddition(addition);
        if (entry != null) {
            new ListAddAction<>(getListValues(), entry).addToHistory();
            ListWidget listWidget = createListWidget(entry);
            addEntry(listWidgets.size(), entry, listWidget);
            addSelectable(listWidget);
            updateSpacing();
        }
        suggestionTextFieldWidget.setText("");
    }

    public abstract List<String> getAdditions();

    @Nullable
    public abstract V tryGetAddition(String addition);

    public void swapEntry(ListWidget listWidget, int direction) {
        int index = listWidgets.indexOf(listWidget);
        int newIndex = MathHelper.clamp(index+direction, 0, listWidgets.size()-1);

        if (index != newIndex) {
            new ListSwapAction<>(getListValues(), index, direction).addToHistory();

            V entry = removeEntry(index, false);
            addEntry(newIndex, entry, listWidget);

            updateSpacing();
        }
    }

    public void removeEntry(ListWidget listWidget) {
        int index = listWidgets.indexOf(listWidget);
        new ListRemoveAction<>(getListValues(), index).addToHistory();

        remove(listWidget);
        removeEntry(index, true);

        updateSpacing();
    }

    protected void addEntry(int index, V entry, ListWidget widget) {
        listWidgets.add(index, widget);
        getListValues().add(index, entry);
    }

    protected V removeEntry(int index, boolean delete) {
        ListWidget listWidget = listWidgets.remove(index);
        if (delete) {
            listWidget.onRemove();
        }
        return getListValues().remove(index);
    }

    protected void enterAdditionScreen() {
        assert client != null;
        client.setScreen(new ListAdditionScreen<>(this));
    }
}
