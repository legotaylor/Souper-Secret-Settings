package com.nettakrim.souper_secret_settings.gui.groups;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.Group;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GroupEditScreen extends ListScreen<String> {
    protected final GroupScreen groupScreen;
    protected final Group group;
    protected String name;

    protected GroupEditScreen(GroupScreen groupScreen, Group group, String name) {
        super(-1);
        this.groupScreen = groupScreen;
        this.group = group;
        this.name = name;
    }

    @Override
    protected List<String> getListValues() {
        return group.entries;
    }

    @Override
    protected ListWidget createListWidget(String value) {
        return new GroupEntryWidget(SoupGui.listX, SoupGui.listWidth, this, value);
    }

    @Override
    public List<String> calculateAdditions() {
        List<String> additions = new ArrayList<>(groupScreen.shaderScreen.getAdditions());
        additions.remove("random");
        return additions;
    }

    @Override
    public @Nullable String tryGetAddition(String addition) {
        char c = addition.charAt(0);
        if ((c == '+' || c == '-') && !getAdditions().contains(addition)) {
            return addition;
        }

        return "+"+addition;
    }

    @Override
    protected boolean canUseRandom() {
        return false;
    }

    @Override
    protected boolean matchIdentifiers() {
        return false;
    }

    @Override
    protected boolean useHistory() {
        return false;
    }

    @Override
    protected boolean canPreview() {
        return false;
    }

    protected List<ListWidget> getListWidgets() {
        return listWidgets;
    }

    @Override
    public void close() {
        ClientData.minecraft.setScreen(groupScreen);
    }

    @Override
    public void updateSpacing() {
        super.updateSpacing();
        group.requestUpdate();
    }
}
