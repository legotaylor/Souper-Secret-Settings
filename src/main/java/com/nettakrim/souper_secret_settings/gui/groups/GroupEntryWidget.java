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

    protected Integer delta;

    public GroupEntryWidget(int x, int width, GroupEditScreen groupEditScreen, String entry) {
        super(x, width, Text.literal(entry), groupEditScreen);
        this.groupEditScreen = groupEditScreen;
        this.entry = entry;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        super.renderWidget(context, mouseX, mouseY, tickDelta);
        if (hovered) {
            SouperSecretSettingsClient.soupGui.setHoverText(this.delta == null ? SouperSecretSettingsClient.translate("gui.group_loop") : SouperSecretSettingsClient.translate("gui.group_delta", this.delta >= 0 ? "+"+this.delta : this.delta));
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
        groupEditScreen.updateDeltas();
    }

    @Override
    public void onRemove() {
        super.onRemove();
        if (entry.startsWith("random_")) {
            groupEditScreen.updateDeltas();
        }
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
        if (delta == null) {
            setMessage(SouperSecretSettingsClient.translate("gui.group_error", entry).setStyle(Style.EMPTY.withColor(0xFF1010)));
        } else {
            setMessage(Text.literal(entry));
        }
    }
}
