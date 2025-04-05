package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class CollapseWidget extends ClickableWidget implements ListChild {
    protected boolean expanded;

    protected final List<ClickableWidget> children = new ArrayList<>();

    protected ListScreen<?> listScreen;

    protected int offset;

    protected int collapseHeight;

    private boolean initExpanded = true;
    private boolean initChildren = true;

    public CollapseWidget(int x, int width, Text message, ListScreen<?> listScreen) {
        super(x, 0, width, 20, message);

        this.listScreen = listScreen;

        visible = false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (expanded) {
            for (ClickableWidget clickableWidget : children) {
                ((Drawable)clickableWidget).render(context, mouseX, mouseY, delta);
            }
        }
    }

    protected void tryCreateChildren() {
        if (expanded && initChildren) {
            initChildren = false;
            createChildren(getX(), getWidth());
        }
    }

    protected abstract void createChildren(int x, int width);

    public void updateCollapse(int y) {
        if (initExpanded) {
            initExpanded = false;
            expanded = getStoredExpanded();
            tryCreateChildren();
        }

        offset = y;

        int height = getHeight();
        if (expanded) {
            for (ClickableWidget widget : children) {
                setVisible(widget, true);
                if (widget instanceof CollapseWidget collapseWidget) {
                    collapseWidget.updateCollapse(height + y);
                }
                height += getCollapseHeight(widget);
            }
        } else {
            for (ClickableWidget widget : children) {
                setVisible(widget, false);
            }
        }
        collapseHeight = height;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int height = getHeight();
        if (expanded) {
            for (ClickableWidget widget : children) {
                widget.setY(height + y);
                height += getCollapseHeight(widget);
            }
        }
    }

    protected static void setVisible(ClickableWidget clickableWidget, boolean visible) {
        if (clickableWidget instanceof TextFieldWidget textFieldWidget) {
            textFieldWidget.setVisible(visible);
        } else {
            clickableWidget.visible = visible;
        }

        if (!visible && clickableWidget instanceof CollapseWidget collapseWidget) {
            for (ClickableWidget child : collapseWidget.children) {
                if (child.visible) {
                    setVisible(child, false);
                }
            }
        }
    }

    protected static int getCollapseHeight(ClickableWidget widget) {
        if (widget instanceof ListChild listChild) {
            return listChild.getCollapseHeight();
        } else {
            return widget.getHeight();
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setExpanded(!expanded);
    }

    protected void setExpanded(boolean to) {
        expanded = to;
        setStoredExpanded(to);
        tryCreateChildren();
        listScreen.updateSpacing();
    }

    @Override
    public int getCollapseHeight() {
        return collapseHeight;
    }

    @Override
    public void onRemove() {
        listScreen.removeSelectable(this);
        for (ClickableWidget clickableWidget : children) {
            if (clickableWidget instanceof ListChild listChild) {
                listChild.onRemove();
            } else {
                listScreen.removeSelectable(clickableWidget);
            }
        }
    }

    protected abstract boolean getStoredExpanded();

    protected abstract void setStoredExpanded(boolean to);
}
