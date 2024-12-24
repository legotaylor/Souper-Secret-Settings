package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class ListScreen<V> extends Screen {
    protected ArrayList<ListWidget> listWidgets;

    protected SuggestionTextFieldWidget suggestionTextFieldWidget;

    protected static final int listWidth = 150;
    protected static final int listGap = 2;
    protected static final int headerHeight = 20;
    protected static final int listStart = headerHeight + listGap*2;
    protected static final int scrollWidth = 6;
    protected static final int listX = listGap*2+scrollWidth;

    protected ScrollWidget scrollWidget;
    protected int currentListSize;

    protected ListScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        addDrawableChild(getToggleButton());

        scrollWidget = new ScrollWidget(listGap, listStart, scrollWidth, height-listStart-listGap, Text.literal("scroll"), this::setScroll);
        addDrawableChild(scrollWidget);

        List<V> listValues = getListValues();
        listWidgets = new ArrayList<>(listValues.size());
        for (V value : listValues) {
            ListWidget listWidget = createListWidget(value);
            addDrawableChild(listWidget);
            listWidgets.add(listWidget);
        }

        suggestionTextFieldWidget = new SuggestionTextFieldWidget(listX, listWidth, 20, Text.literal("list addition"));
        suggestionTextFieldWidget.setListeners(this::getAdditions, this::addAddition);
        addDrawableChild(suggestionTextFieldWidget);

        updateSpacing();
    }

    //TODO: needs to be a proper header system
    protected abstract ButtonWidget getToggleButton();

    protected abstract List<V> getListValues();

    protected abstract ListWidget createListWidget(V value);

    public <T extends Element & Selectable> void addSelectable(T child) {
        addSelectableChild(child);
    }

    public void updateSpacing() {
        currentListSize = listStart;
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.visible = true;
            collapseWidget.updateCollapse(currentListSize);
            currentListSize += collapseWidget.getHeight() + listGap;
        }

        scrollWidget.setContentHeight(currentListSize-listStart + suggestionTextFieldWidget.getHeight());
    }

    public void setScroll(int scroll) {
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.setY(collapseWidget.offset - scroll);
        }

        suggestionTextFieldWidget.setY(currentListSize - scroll);
    }

    @Override
    protected void applyBlur() {

    }

    @Override
    protected void renderDarkening(DrawContext context, int x, int y, int width, int height) {

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public abstract List<String> getAdditions();

    public abstract void addAddition(String addition);
}
