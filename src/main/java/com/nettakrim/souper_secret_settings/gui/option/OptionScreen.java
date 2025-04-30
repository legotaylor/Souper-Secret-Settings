package com.nettakrim.souper_secret_settings.gui.option;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.keybindings.Keybindings;
import com.mclegoman.luminance.client.screen.config.ConfigScreen;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.common.util.DateHelper;
import com.mojang.brigadier.StringReader;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.Actions;
import com.nettakrim.souper_secret_settings.gui.*;
import com.nettakrim.souper_secret_settings.gui.shaders.ShaderScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OptionScreen extends ScrollScreen {
    public final List<ClickableWidget> widgets = new ArrayList<>();

    private final int scrollIndex;

    private final String[] previousItems = new String[2];
    private final CompletableFuture<?>[] itemFutures = new CompletableFuture[2];

    public OptionScreen(int scrollIndex) {
        super(Text.empty());
        this.scrollIndex = scrollIndex;
    }

    @Override
    protected void init() {
        for (ClickableWidget clickableWidget : SouperSecretSettingsClient.soupGui.getHeader()) {
            addDrawableChild(clickableWidget);
        }

        createScrollWidget(SoupGui.listStart);

        int widgetWidth = SoupGui.headerWidthLarge - SoupGui.scrollWidth - SoupGui.listGap;

        widgets.clear();

        widgets.add(new TextWidget(SoupGui.listX, 0, widgetWidth, 8, SouperSecretSettingsClient.translate("option.gui.main"), ClientData.minecraft.textRenderer));
        widgets.add(new CycleWidget(SoupGui.listX, widgetWidth,
                (direction) -> SouperSecretSettingsClient.soupData.config.disableState = CycleWidget.cycleInt(SouperSecretSettingsClient.soupData.config.disableState + direction, 2),
                () -> SouperSecretSettingsClient.translate("option.gui.toggle."+SouperSecretSettingsClient.soupData.config.disableState))
        );
        SliderWidget sliderWidget = new SoupAlphaSlider(SoupGui.listX, 0, widgetWidth, 20, Uniforms.getRawAlpha() / 100.0F, () -> Uniforms.updatingAlpha = true);
        widgets.add(sliderWidget);
        widgets.add(new CycleWidget(SoupGui.listX, widgetWidth,
                (direction) -> SouperSecretSettingsClient.soupData.config.messageFilter = CycleWidget.cycleInt(SouperSecretSettingsClient.soupData.config.messageFilter - direction, 2),
                () -> SouperSecretSettingsClient.translate("option.gui.filter."+SouperSecretSettingsClient.soupData.config.messageFilter))
        );

        widgets.add(new TextWidget(SoupGui.listX, 0, widgetWidth, 8, SouperSecretSettingsClient.translate("option.gui.eating"), ClientData.minecraft.textRenderer));
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.random"),
                (x, width) -> {
                    SuggestionTextFieldWidget widget = new SuggestionTextFieldWidget(x, width, 20, Text.empty(), false);
                    widget.setMaxLengthMin(1024);
                    CompletableFuture.runAsync(() -> widget.setText(itemStackToString(SouperSecretSettingsClient.soupData.config.randomItem)));
                    widget.setListeners(this::getItemSuggestions, null, true);
                    widget.setChangedListener(value -> getItemStack(value, 1, itemStack -> SouperSecretSettingsClient.soupData.config.randomItem = itemStack));
                    widget.disableDrag = true;
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.shader"),
                (x, width) -> {
                    SuggestionTextFieldWidget widget = new SuggestionTextFieldWidget(x, width, 20, Text.empty(), false);
                    widget.setText(SouperSecretSettingsClient.soupData.config.randomShader);
                    widget.setListeners(() -> ShaderScreen.calculateAdditions(Shaders.getMainRegistryId()), null, true);
                    widget.setChangedListener((value) -> SouperSecretSettingsClient.soupData.config.randomShader = value);
                    widget.disableDrag = true;
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.count"),
                (x, width) -> {
                    DraggableIntWidget widget = new DraggableIntWidget(x, width, 20, Text.empty(), 1, 256, 1, (value) -> SouperSecretSettingsClient.soupData.config.randomCount = value);
                    widget.setText(Integer.toString(SouperSecretSettingsClient.soupData.config.randomCount));
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.duration"),
                (x, width) -> {
                    DraggableIntWidget widget = new DraggableIntWidget(x, width, 20, Text.empty(), 0, Integer.MAX_VALUE, 0, (value) -> SouperSecretSettingsClient.soupData.config.randomDuration = value);
                    widget.setText(Integer.toString(SouperSecretSettingsClient.soupData.config.randomDuration));
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.clear"),
                (x, width) -> {
                    SuggestionTextFieldWidget widget = new SuggestionTextFieldWidget(x, width, 20, Text.empty(), false);
                    widget.setMaxLengthMin(1024);
                    CompletableFuture.runAsync(() -> widget.setText(itemStackToString(SouperSecretSettingsClient.soupData.config.clearItem)));
                    widget.setListeners(this::getItemSuggestions, null, true);
                    widget.setChangedListener(value -> getItemStack(value, 0, itemStack -> SouperSecretSettingsClient.soupData.config.clearItem = itemStack));
                    widget.disableDrag = true;
                    return widget;
                })
        );
        widgets.add(new CycleWidget(SoupGui.listX, widgetWidth,
                (direction) -> SouperSecretSettingsClient.soupData.config.randomSound = !SouperSecretSettingsClient.soupData.config.randomSound,
                () -> SouperSecretSettingsClient.translate("option.gui.sound."+(SouperSecretSettingsClient.soupData.config.randomSound ? "on" : "off")))
        );

        widgets.add(new TextWidget(SoupGui.listX, 0, widgetWidth, 8, SouperSecretSettingsClient.translate("option.gui.misc"), ClientData.minecraft.textRenderer));
        widgets.add(ButtonWidget.builder(SouperSecretSettingsClient.translate("option.gui.luminance"), (buttonWidget) -> ClientData.minecraft.setScreen(new ConfigScreen(this, false, DateHelper.isPride()))).dimensions(SoupGui.listX, 0, widgetWidth, 20).build());
        widgets.add(new CycleWidget(SoupGui.listX, widgetWidth,
                (direction) -> SouperSecretSettingsClient.soupData.config.warning = !SouperSecretSettingsClient.soupData.config.warning,
                () -> SouperSecretSettingsClient.translate("option.gui.warning."+(SouperSecretSettingsClient.soupData.config.warning ? "on" : "off")))
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.undo_limit"),
                (x, width) -> {
                    DraggableIntWidget widget = new DraggableIntWidget(x, width, 20, Text.empty(), 16, Integer.MAX_VALUE, Actions.defaultLength, (value) -> SouperSecretSettingsClient.soupData.config.undoLimit = value);
                    widget.setText(Integer.toString(SouperSecretSettingsClient.soupData.config.undoLimit));
                    return widget;
                })
        );

        int height = -2;
        for (ClickableWidget widget : widgets) {
            if (widget instanceof TextWidget) {
                height += 5;
            }

            if (widget instanceof LabelledWidget labelledWidget) {
                addSelectableChild(labelledWidget.widget);
            } else {
                addSelectableChild(widget);
            }
            height += widget.getHeight() + SoupGui.listGap;
        }
        scrollWidget.setContentHeight(height - SoupGui.listGap);

        scrollWidget.offsetScroll(SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex]);
    }

    @Override
    public void setScroll(int scroll) {
        int y = (SoupGui.headerHeight - scroll) + SoupGui.listGap*2 - 2;

        for (ClickableWidget widget : widgets) {
            if (widget instanceof TextWidget) {
                y += 5;
            }

            widget.setY(y);
            y += widget.getHeight() + SoupGui.listGap;
        }

        SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex] = scroll;
    }

    @Override
    protected void renderScrollables(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ClickableWidget widget : widgets) {
            widget.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void close() {
        SouperSecretSettingsClient.soupGui.onClose();
    }

    private List<String> getItemSuggestions() {
        List<String> items = new ArrayList<>(Registries.ITEM.size());
        Registries.ITEM.forEach((item) -> items.add(item.toString()));
        return items;
    }

    private void getItemStack(String s, int index, Consumer<ItemStack> consumer) {
        if (s.equals(previousItems[index])) {
            return;
        }
        previousItems[index] = s;

        if (itemFutures[index] != null) {
            itemFutures[index].cancel(false);
        }

        itemFutures[index] = CompletableFuture.runAsync(() -> {
            try {
                StringReader stringReader = new StringReader(s);
                ItemStringReader.ItemResult result = new ItemStringReader(BuiltinRegistries.createWrapperLookup()).consume(stringReader);
                ItemStack itemStack = new ItemStack(result.item(), 1);
                itemStack.applyUnvalidatedChanges(result.components());
                consumer.accept(itemStack);
            } catch (Exception ignored) {}
        });
    }

    private String itemStackToString(ItemStack itemStack) {
        return new ItemStackArgument(itemStack.getRegistryEntry(), itemStack.getComponentChanges()).asString(BuiltinRegistries.createWrapperLookup());
    }

    private static class SoupAlphaSlider extends ConfigScreen.AlphaSlider {
        public SoupAlphaSlider(int x, int y, int width, int height, double value, Runnable onChange) {
            super(x, y, width, height, value, onChange);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            if (hovered) {
                SouperSecretSettingsClient.soupGui.setHoverText(SouperSecretSettingsClient.translate("option.gui.alpha", Keybindings.adjustAlpha.getBoundKeyLocalizedText()));
            }
        }
    }
}
