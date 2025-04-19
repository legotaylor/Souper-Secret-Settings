package com.nettakrim.souper_secret_settings.gui.groups;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.gui.SuggestionTextFieldWidget;
import com.nettakrim.souper_secret_settings.shaders.Group;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupEditScreen extends ListScreen<String> {
    protected final GroupScreen groupScreen;
    protected final Group group;
    protected String name;
    protected String startingName;

    protected SuggestionTextFieldWidget nameWidget;

    protected GroupEditScreen(GroupScreen groupScreen, Group group, String name) {
        super(-1);
        this.groupScreen = groupScreen;
        this.group = group;
        this.name = name;
        this.startingName = name;
    }

    @Override
    protected int createHeader() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), (widget) -> close()).dimensions(SoupGui.listGap, SoupGui.listGap, SoupGui.headerWidthSmall, 20).build());

        nameWidget = new SuggestionTextFieldWidget(SoupGui.listGap, SoupGui.headerWidthSmall, 20, Text.literal("name"), false);
        nameWidget.setY(SoupGui.listGap*2 + 20);
        nameWidget.setText(name);
        nameWidget.setChangedListener((s) -> name = s);
        nameWidget.setListeners(() -> List.of(startingName), (s) -> resolveName(false), false);
        nameWidget.setTextPredicate(Identifier::isPathValid);

        addDrawableChild(nameWidget);

        return SoupGui.listStart;
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
        resolveName(true);
        ClientData.minecraft.setScreen(groupScreen);
    }

    protected void resolveName(boolean move) {
        if (startingName.equals(name)) {
            return;
        }

        Map<String, Group> map = groupScreen.getRegistryMap();
        SouperSecretSettingsClient.log(name, map.containsKey(name));
        if (map.containsKey(name)) {
            nameWidget.setText(startingName);
            nameWidget.setCursorToEnd(false);
        }

        if (move) {
            map.put(name, map.remove(startingName));
            startingName = name;
        }
    }

    @Override
    public void updateSpacing() {
        super.updateSpacing();
        group.requestUpdate();
    }
}
