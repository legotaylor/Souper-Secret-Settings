package com.nettakrim.souper_secret_settings.gui.config;

import com.mclegoman.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends ScrollScreen {
    public final List<ClickableWidget> widgets = new ArrayList<>();

    private final int scrollIndex;

    public ConfigScreen(int scrollIndex) {
        super(Text.literal(""));
        this.scrollIndex = scrollIndex;
    }

    @Override
    protected void init() {
        for (ClickableWidget clickableWidget : SouperSecretSettingsClient.soupGui.getHeader()) {
            addDrawableChild(clickableWidget);
        }

        createScrollWidget(SoupGui.listStart);

        Text blank = Text.literal("");

        int widgetWidth = SoupGui.headerWidth - SoupGui.scrollWidth - SoupGui.listGap;

        widgets.clear();

        widgets.add(new TextWidget(SoupGui.listX, 0, widgetWidth, 8, Text.literal("Options"), ClientData.minecraft.textRenderer));
        widgets.add(new CycleWidget(SoupGui.listX, widgetWidth,
                (direction) -> SouperSecretSettingsClient.soupData.config.disableState = CycleWidget.cycleInt(SouperSecretSettingsClient.soupData.config.disableState + direction, 2),
                () -> SouperSecretSettingsClient.translate("option.toggle."+SouperSecretSettingsClient.soupData.config.disableState))
        );
        widgets.add(new CycleWidget(SoupGui.listX, widgetWidth,
                (direction) -> SouperSecretSettingsClient.soupData.config.messageFilter = CycleWidget.cycleInt(SouperSecretSettingsClient.soupData.config.messageFilter - direction, 2),
                () -> SouperSecretSettingsClient.translate("option.filter."+SouperSecretSettingsClient.soupData.config.messageFilter))
        );

        widgets.add(new TextWidget(SoupGui.listX, 0, widgetWidth, 8, Text.literal("Eating Items"), ClientData.minecraft.textRenderer));
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, Text.literal("Clear"),
                (x, width) -> new SuggestionTextFieldWidget(x, width, 20, blank, false))
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, Text.literal("Random"),
                (x, width) -> new SuggestionTextFieldWidget(x, width, 20, blank, false))
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, Text.literal("Shader"),
                (x, width) -> new SuggestionTextFieldWidget(x, width, 20, blank, false))
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, Text.literal("Duration"),
                (x, width) -> new DraggableTextFieldWidget(x, width, 20, blank))
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, Text.literal("Count"),
                (x, width) -> new DraggableTextFieldWidget(x, width, 20, blank))
        );

        widgets.add(new TextWidget(SoupGui.listX, 0, widgetWidth, 8, Text.literal("Other Options"), ClientData.minecraft.textRenderer));
        widgets.add(new CycleWidget(SoupGui.listX, widgetWidth,
                (direction) -> SouperSecretSettingsClient.soupData.config.warning = !SouperSecretSettingsClient.soupData.config.warning,
                () -> SouperSecretSettingsClient.translate("option.warning."+(SouperSecretSettingsClient.soupData.config.warning ? "on" : "off")))
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, Text.literal("Undo Limit"),
                (x, width) -> new DraggableTextFieldWidget(x, width, 20, blank))
        );

        int height = 0;
        for (ClickableWidget clickableWidget : widgets) {
            if (clickableWidget instanceof TextWidget) {
                height += 3;
            }

            if (clickableWidget instanceof LabelledWidget labelledWidget) {
                addSelectableChild(labelledWidget.widget);
            } else {
                addSelectableChild(clickableWidget);
            }
            height += clickableWidget.getHeight() + SoupGui.listGap;
        }
        scrollWidget.setContentHeight(height - SoupGui.listGap);

        scrollWidget.offsetScroll(SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex]);
    }

    @Override
    public void setScroll(int scroll) {
        int y = (SoupGui.headerHeight - scroll) + SoupGui.listGap*2;

        for (ClickableWidget widget : widgets) {
            widget.setY(y);
            y += widget.getHeight() + SoupGui.listGap;
        }

        SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex] = scroll;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        SouperSecretSettingsClient.soupGui.drawCurrentHoverText(context, mouseX, mouseY);
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
}
