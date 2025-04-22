package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.gui.SuggestionTextFieldWidget;
import com.nettakrim.souper_secret_settings.shaders.Group;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GroupEditScreen extends ListScreen<String> {
    protected final ShaderAdditionScreen groupScreen;
    protected final Group group;
    protected String name;
    protected String startingName;

    protected SuggestionTextFieldWidget nameWidget;

    public GroupEditScreen(ShaderAdditionScreen groupScreen, Group group, String name) {
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
        nameWidget.setText(name.replace("user_", ""));
        nameWidget.setChangedListener((s) -> name = s);
        nameWidget.setListeners(() -> List.of(startingName), (s) -> resolveName(false), false);
        nameWidget.setTextPredicate(Identifier::isPathValid);
        nameWidget.active = name.startsWith("user_");
        addDrawableChild(nameWidget);

        if (!nameWidget.active) {
            nameWidget.setEditable(false);
            nameWidget.setWidth((nameWidget.getWidth()-SoupGui.listGap)/2);
            addDrawableChild(ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.group_reset"), (widget) -> reset()).dimensions(SoupGui.listGap+nameWidget.getWidth()+SoupGui.listGap, nameWidget.getY(), nameWidget.getWidth(), 20).build());
        }

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
        additions.add("all");
        return additions;
    }

    @Override
    public @Nullable String tryGetAddition(String addition) {
        char c = addition.charAt(0);
        if ((c == '+' || c == '-') && !getAdditions().contains(addition)) {
            addition = addition.substring(1);
        } else {
            c = '+';
        }

        if (!addition.startsWith("random_")) {
            Optional<ShaderRegistryEntry> registryEntry = Shaders.guessPostShader(groupScreen.shaderScreen.registry, addition);
            if (registryEntry.isPresent()) {
                addition = registryEntry.get().getID().toString();
            }
        }

        return c+addition;
    }

    @Override
    protected Couple<Text, Text> getAdditionText(String addition) {
        return groupScreen.shaderScreen.getAdditionText(addition);
    }

    @Override
    protected boolean canUseRandom() {
        return false;
    }

    @Override
    protected boolean matchIdentifiers() {
        return true;
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
        group.changed = true;
        ClientData.minecraft.setScreen(groupScreen);
    }

    protected void resolveName(boolean move) {
        if (startingName.equals(name) || !startingName.startsWith("user_")) {
            return;
        }

        String newName = "user_"+name;

        Map<String, Group> map = groupScreen.getRegistryGroups();
        if (map.containsKey(newName)) {
            nameWidget.setText(startingName);
            nameWidget.setCursorToEnd(false);
        }

        if (move) {
            map.put(newName, map.remove(startingName));
            startingName = newName;
            groupScreen.shaderScreen.recalculateAdditions();
            if (group.file != null) {
                File newFile = SouperSecretSettingsClient.soupData.getGroupLocation(groupScreen.shaderScreen.registry, newName).toFile();
                try {
                    FileUtils.moveFile(group.file, newFile);
                    group.file = newFile;
                } catch (Exception e) {
                    SouperSecretSettingsClient.log("Failed to move file " + group.file);
                }
            }
        }
    }

    @Override
    public void updateSpacing() {
        super.updateSpacing();
        group.requestUpdate();
        updateDeltas();
    }

    public void updateDeltas() {
        List<Integer> deltas = group.getStepAmounts(groupScreen.shaderScreen.registry, name);

        for (int i = 0; i < deltas.size(); i++) {
            ((GroupEntryWidget)listWidgets.get(i)).setDelta(deltas.get(i));
        }
    }

    public void reset() {
        group.entries.clear();

        Group resourceGroup = null;
        Map<String, Group> resourceGroups = SouperSecretSettingsClient.soupData.resourceGroups.get(groupScreen.shaderScreen.registry.toString().replace(':','_'));
        if (resourceGroups != null) {
            resourceGroup = resourceGroups.get(startingName);
        }

        if (resourceGroup != null) {
            group.entries.addAll(resourceGroup.entries);
        } else {
            group.entries.add("+random_"+startingName);
        }
        group.changed = true;
        clearAndInit();
    }
}
