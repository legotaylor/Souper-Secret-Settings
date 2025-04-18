package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ListRemoveAction;
import com.nettakrim.souper_secret_settings.actions.ListShiftAction;
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

    protected int currentListSize;

    private final int scrollIndex;

    protected List<String> additions;

    protected ListScreen(int scrollIndex) {
        super(Text.empty());
        this.scrollIndex = scrollIndex;
        this.additions = null;
    }

    @Override
    protected void init() {
        for (ClickableWidget clickableWidget : SouperSecretSettingsClient.soupGui.getHeader()) {
            addDrawableChild(clickableWidget);
        }

        createScrollWidget(SoupGui.listStart);

        List<V> listValues = getListValues();
        listWidgets = new ArrayList<>(listValues.size());
        for (V value : listValues) {
            ListWidget listWidget = createListWidget(value);
            addSelectableChild(listWidget);
            listWidgets.add(listWidget);
        }

        suggestionTextFieldWidget = new SuggestionTextFieldWidget(SoupGui.listX, SoupGui.listWidth-20-SoupGui.listGap, 20, Text.literal("list addition"), false);
        suggestionTextFieldWidget.setListeners(this::getAdditions, this::addAddition, matchIdentifiers());
        addDrawableChild(suggestionTextFieldWidget);

        suggestionScreenButton = ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.addition"), (widget) -> enterAdditionScreen()).dimensions(SoupGui.listX+SoupGui.listWidth-20, 0, 20, 20).build();
        addDrawableChild(suggestionScreenButton);

        updateSpacing();
        if (scrollIndex >= 0) {
            scrollWidget.offsetScroll(SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex]);
        }
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
        currentListSize = SoupGui.listStart;
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.visible = true;
            collapseWidget.updateCollapse(currentListSize);
            currentListSize += collapseWidget.getCollapseHeight() + SoupGui.listGap;
        }

        scrollWidget.setContentHeight(currentListSize-SoupGui.listStart + suggestionTextFieldWidget.getHeight());
    }

    @Override
    public void setScroll(int scroll) {
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.setY(collapseWidget.offset - scroll);
        }

        suggestionTextFieldWidget.setY(currentListSize - scroll);
        suggestionScreenButton.setY(suggestionTextFieldWidget.getY());
        if (scrollIndex >= 0) {
            SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex] = scroll;
        }
    }

    protected V addAddition(String addition) {
        V entry = tryGetAddition(addition);
        if (entry != null) {
            if (useHistory()) {
                new ListAddAction<>(getListValues(), entry).addToHistory();
            }
            ListWidget listWidget = createListWidget(entry);
            addEntry(listWidgets.size(), entry, listWidget);
            addSelectable(listWidget);
            updateSpacing();
        }
        suggestionTextFieldWidget.setText("");
        return entry;
    }

    public List<String> getAdditions() {
        if (additions == null) {
            additions = calculateAdditions();
        }

        return additions;
    }

    public abstract List<String> calculateAdditions();

    public void recalculateAdditions() {
        additions = null;
    }

    @Nullable
    public abstract V tryGetAddition(String addition);

    public void swapEntry(ListWidget listWidget, int direction) {
        int index = listWidgets.indexOf(listWidget);
        int newIndex = MathHelper.clamp(index+direction, 0, listWidgets.size()-1);

        if (index != newIndex) {
            if (useHistory()) {
                new ListShiftAction<>(getListValues(), index, direction).addToHistory();
            }

            V entry = removeEntry(index, false);
            addEntry(newIndex, entry, listWidget);

            updateSpacing();
        }
    }

    public void removeEntry(ListWidget listWidget) {
        int index = listWidgets.indexOf(listWidget);
        if (useHistory()) {
            new ListRemoveAction<>(getListValues(), index).addToHistory();
        }

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

    protected boolean canRemoveAddition(String addition) {
        return false;
    }

    protected void removeAddition(String addition) {
        additions.remove(addition);
    }

    protected abstract boolean canUseRandom();

    protected boolean canPreview() {
        return true;
    }

    protected Couple<Text,Text> getAdditionText(String addition) {
        return new Couple<>(Text.literal(addition), null);
    }

    protected abstract boolean matchIdentifiers();

    @Override
    public void close() {
        SouperSecretSettingsClient.soupGui.onClose();
    }

    protected boolean useHistory() {
        return true;
    }
}
