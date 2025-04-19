package com.nettakrim.souper_secret_settings.gui.groups;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class GroupEntryWidget extends ListWidget {
    protected final GroupEditScreen groupEditScreen;

    protected String entry;

    protected boolean error;

    public GroupEntryWidget(int x, int width, GroupEditScreen groupEditScreen, String entry) {
        super(x, width, Text.literal(entry), groupEditScreen);
        this.groupEditScreen = groupEditScreen;
        this.entry = entry;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (error && hovered) {
            SouperSecretSettingsClient.soupGui.setHoverText(SouperSecretSettingsClient.translate("gui.group_loop"));
        }
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
        groupEditScreen.group.requestUpdate();
        setError(error);
    }

    @Override
    public void onRemove() {
        super.onRemove();
        if (entry.startsWith("random_")) {
            groupEditScreen.updateErrors();
        }
    }

    public void setError(boolean error) {
        this.error = error;
        if (error) {
            setMessage(SouperSecretSettingsClient.translate("gui.group_error", entry).setStyle(Style.EMPTY.withColor(0xFF1010)));
        } else {
            setMessage(Text.literal(entry));
        }
    }
}
