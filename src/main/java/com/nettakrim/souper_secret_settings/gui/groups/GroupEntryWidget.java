package com.nettakrim.souper_secret_settings.gui.groups;

import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class GroupEntryWidget extends ListWidget {
    protected final GroupEditScreen groupEditScreen;

    protected String entry;

    public GroupEntryWidget(int x, int width, GroupEditScreen groupEditScreen, String entry) {
        super(x, width, Text.literal(entry), groupEditScreen);
        this.groupEditScreen = groupEditScreen;
        this.entry = entry;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected Toggleable getToggleable() {
        return new Toggleable.CallableToggle(this::toggle);
    }

    @Override
    protected void createChildren(int x, int width) {

    }

    @Override
    protected boolean getStoredExpanded() {
        return false;
    }

    @Override
    protected void setStoredExpanded(boolean to) {

    }

    @Override
    protected void setExpanded(boolean to) {
        toggle();
    }

    private void toggle() {
        entry = (entry.charAt(0) == '-' ? "+" : "-")+entry.substring(1);
        updateValue();
    }

    private void updateValue() {
        groupEditScreen.getListValues().set(groupEditScreen.getListWidgets().indexOf(this), entry);
        setMessage(Text.literal(entry));
        groupEditScreen.group.requestUpdate();
    }
}
