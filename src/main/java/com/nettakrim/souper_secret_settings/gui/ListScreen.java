package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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
            addSelectableChild(listWidget);
            listWidgets.add(listWidget);
        }

        suggestionTextFieldWidget = new SuggestionTextFieldWidget(listX, listWidth, 20, Text.literal("list addition"));
        suggestionTextFieldWidget.setListeners(this::getAdditions, this::addAddition);
        addDrawableChild(suggestionTextFieldWidget);

        updateSpacing();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.enableScissor(listX, listStart, width, height);
        for (Drawable drawable : listWidgets) {
            drawable.render(context, mouseX, mouseY, delta);
        }
        context.disableScissor();
    }

    //TODO: needs to be a proper header system
    protected abstract ButtonWidget getToggleButton();

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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollWidget.offsetScroll(verticalAmount*-20);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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

    public void swapEntry(ListWidget listWidget, int direction) {
        int index = listWidgets.indexOf(listWidget);
        V entry = removeEntry(index);

        addEntry(MathHelper.clamp(index+direction, 0, listWidgets.size()), entry, listWidget);

        updateSpacing();
    }

    public void removeEntry(ListWidget listWidget) {
        remove(listWidget);
        removeEntry(listWidgets.indexOf(listWidget));

        updateSpacing();
    }

    protected void addEntry(int index, V entry, ListWidget widget) {
        listWidgets.add(index, widget);
        getListValues().add(index, entry);
    }

    protected V removeEntry(int index) {
        listWidgets.remove(index);
        return getListValues().remove(index);
    }
}
