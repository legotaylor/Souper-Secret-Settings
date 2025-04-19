package com.nettakrim.souper_secret_settings.gui.groups;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.AdditionButton;
import com.nettakrim.souper_secret_settings.gui.ScrollScreen;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.gui.shaders.ShaderScreen;
import com.nettakrim.souper_secret_settings.shaders.Group;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupScreen extends ScrollScreen {
    protected final ShaderScreen shaderScreen;

    protected List<ClickableWidget> children;

    public GroupScreen(ShaderScreen shaderScreen) {
        super(Text.empty());

        this.shaderScreen = shaderScreen;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), (widget) -> close()).dimensions(SoupGui.listGap, SoupGui.listGap, SoupGui.headerWidthSmall, 20).build());
        addDrawableChild(ButtonWidget.builder(SouperSecretSettingsClient.translate("gui.group_create"), this::createNew).dimensions(SoupGui.listGap, SoupGui.listGap*2 + 20, SoupGui.headerWidthSmall, 20).build());

        createScrollWidget(SoupGui.listStart);

        children = new ArrayList<>();

        Map<String, Group> map = getRegistryMap();
        for (String name : map.keySet().stream().sorted().toList()) {
            createGroupButton(name, map.get(name));
        }

        scrollWidget.setContentHeight(children.size()*(20+SoupGui.listGap) - SoupGui.listGap);
    }

    @Override
    public void setScroll(int scroll) {
        int y = SoupGui.listStart - scroll;
        for (ClickableWidget widget : children) {
            widget.setY(y);
            y += widget.getHeight()+SoupGui.listGap;
        }
    }

    @Override
    protected void renderScrollables(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Drawable drawable : children) {
            drawable.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void close() {
        assert client != null;
        shaderScreen.recalculateAdditions();
        client.setScreen(shaderScreen);
    }

    protected void createGroupButton(String name, Group group) {
        Couple<Text, Text> text;
        int recursionIndex = group.getRecursionIndex(shaderScreen.registry);
        if (recursionIndex >= 0) {
            text = new Couple<>(SouperSecretSettingsClient.translate("gui.group_error", name).setStyle(Style.EMPTY.withColor(0xFF1010)), SouperSecretSettingsClient.translate("gui.group_loop_index", recursionIndex));
        } else {
            text = new Couple<>(Text.literal(name), SouperSecretSettingsClient.translate("shader.group_suggestion", group.getComputed(shaderScreen.registry).size()));
        }

        AdditionButton groupButton = new AdditionButton(name, text, SoupGui.listX, SoupGui.listWidth, 20, this::select);
        groupButton.addRemoveListener(this::removeGroup);
        children.add(groupButton);
        addSelectableChild(groupButton);
    }

    protected void select(String name) {
        ClientData.minecraft.setScreen(new GroupEditScreen(this, getRegistryMap().get(name), name));
    }

    protected void createNew(ButtonWidget button) {
        Map<String, Group> map = getRegistryMap();

        String name;
        int i = 1;
        do {
            name = "group_"+i;
            i++;
        } while (map.containsKey(name));

        Group group = new Group();
        map.put(name, group);
        createGroupButton(name, group);

        select(name);
    }

    protected void removeGroup(AdditionButton button) {
        remove(button);
        children.remove(button);
        scrollWidget.setContentHeight(children.size()*(20+SoupGui.listGap) - SoupGui.listGap);

        getRegistryMap().remove(button.addition);
    }

    public Map<String, Group> getRegistryMap() {
        return SouperSecretSettingsClient.soupRenderer.shaderGroups.get(shaderScreen.registry);
    }
}
