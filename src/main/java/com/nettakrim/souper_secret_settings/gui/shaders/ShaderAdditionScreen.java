package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.AdditionButton;
import com.nettakrim.souper_secret_settings.gui.ListAdditionScreen;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.Group;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Map;

public class ShaderAdditionScreen extends ListAdditionScreen<ShaderData> {
    protected final ShaderScreen shaderScreen;

    protected ShaderAdditionScreen(ShaderScreen shaderScreen) {
        super(shaderScreen);
        this.shaderScreen = shaderScreen;
    }

    boolean isGroups;
    boolean changedGroups;

    int[] scrolls = new int[2];

    ButtonWidget createButton;

    @Override
    protected int createHeader() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), (widget) -> close()).dimensions(SoupGui.listGap, SoupGui.listGap, SoupGui.headerWidthSmall, 20).build());

        int halfWidth = (SoupGui.headerWidthSmall-SoupGui.listGap)/2;
        addDrawableChild(ButtonWidget.builder(SouperSecretSettingsClient.translate(isGroups ? "gui.groups" : (shaderScreen.registry == SoupRenderer.modifierRegistry ? "gui.modifiers" : "gui.shaders")), (widget) -> toggleMode()).dimensions(SoupGui.listGap, SoupGui.listGap*2 + 20, halfWidth, 20).build());
        createButton = addDrawableChild(ButtonWidget.builder(Text.literal("Create New"), (widget) -> createGroup()).dimensions(SoupGui.listGap*2+halfWidth, SoupGui.listGap*2 + 20, halfWidth, 20).build());
        createButton.active = isGroups;

        return SoupGui.listStart;
    }

    @Override
    protected void createAdditionButton(String addition) {
        if (isGroups != addition.startsWith("random_")) {
            return;
        }

        if (isGroups) {
            String name = addition.substring(7);
            createGroupButton(name, getRegistryMap().get(name));
            return;
        }

        super.createAdditionButton(addition);
    }

    protected AdditionButton createGroupButton(String name, Group group) {
        Couple<Text, Text> text;
        group.requestUpdate();
        int recursionIndex = group.getStepAmounts(shaderScreen.registry).indexOf(null);
        if (recursionIndex >= 0) {
            text = new Couple<>(SouperSecretSettingsClient.translate("gui.group_error", name).setStyle(Style.EMPTY.withColor(0xFF1010)), SouperSecretSettingsClient.translate("gui.group_loop_index", recursionIndex));
        } else {
            text = new Couple<>(Text.literal("random_"+name), SouperSecretSettingsClient.translate("shader.group_suggestion", group.getComputed(shaderScreen.registry).size()));
        }

        AdditionButton groupButton = new AdditionButton("random_"+name, text, SoupGui.listX, SoupGui.listWidth, 20, this::add);
        groupButton.addRemoveListener(this::removeGroup);
        groupButton.addEditListener(this::selectGroup);
        children.add(groupButton);
        addSelectableChild(groupButton);

        return groupButton;
    }

    @Override
    public void setScroll(int scroll) {
        super.setScroll(scroll);
        scrolls[isGroups ? 1 : 0] = scroll;
    }

    protected void toggleMode() {
        isGroups = !isGroups;
        int scroll = scrolls[isGroups ? 1 : 0];
        clearAndInit();
        scrollWidget.offsetScroll(scroll);
    }

    protected void selectGroup(AdditionButton additionButton) {
        String name = additionButton.addition.substring(7);
        ClientData.minecraft.setScreen(new GroupEditScreen(this, getRegistryMap().get(name), name));
        changedGroups = true;
    }

    protected void createGroup() {
        Map<String, Group> map = getRegistryMap();

        String name;
        int i = 1;
        do {
            name = "group_"+i;
            i++;
        } while (map.containsKey(name));

        Group group = new Group();
        map.put(name, group);

        selectGroup(createGroupButton(name, group));
        shaderScreen.recalculateAdditions();
    }

    protected void removeGroup(AdditionButton button) {
        removeAddition(button);
        getRegistryMap().remove(button.addition.substring(7));
        changedGroups = true;
    }

    public Map<String, Group> getRegistryMap() {
        return SouperSecretSettingsClient.soupRenderer.shaderGroups.get(shaderScreen.registry);
    }

    @Override
    public void close() {
        if (changedGroups) {
            shaderScreen.recalculateAdditions();
        }
        super.close();
    }
}
