package com.nettakrim.souper_secret_settings.gui.parameters;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.nettakrim.souper_secret_settings.actions.ArrSetAction;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public class CalculationWidget extends DisplayWidget<OverrideSource> {
    public Calculation calculation;
    protected ShaderLayer layer;

    protected ParameterTextWidget[] outputs;

    public CalculationWidget(Calculation calculation, ShaderLayer layer, Text name, int x, int width, ListScreen<?> listScreen) {
        super(calculation.inputs.length, name, x, width, listScreen);
        this.calculation = calculation;
        this.layer = layer;
        active = false;
    }

    @Override
    protected void createChildren(int x, int width) {
        int outputCount = calculation.outputs.length;
        outputs = new ParameterTextWidget[outputCount];

        int split = (getWidth()-displayWidth)/outputCount;
        for (int i = 0; i < outputCount; i++) {
            ParameterTextWidget parameterTextWidget = new ParameterTextWidget(getX() + split*i, split, 20, Text.literal("output"+i), layer, "");
            listScreen.addSelectable(parameterTextWidget);
            int finalI = i;
            parameterTextWidget.setText(calculation.outputs[i]);
            parameterTextWidget.setChangedListener((s) -> onOutputChanged(finalI, s));
            outputs[i] = parameterTextWidget;
        }

        super.createChildren(x, width);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ParameterTextWidget parameterTextWidget : outputs) {
            parameterTextWidget.renderWidget(context, mouseX, mouseY, delta);
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected ClickableWidget createChildWidget(OverrideSource data, int i) {
        String value = data.getString();
        ParameterTextWidget parameterTextWidget = new CalculationInputWidget(getX(), getWidth(), 20, Text.literal(calculation.inputNames[i]), layer, value);
        parameterTextWidget.setText(value);
        parameterTextWidget.setChangedListener((s) -> onInputChanged(i, s));
        return parameterTextWidget;
    }

    @Override
    protected List<OverrideSource> getChildData() {
        return Arrays.asList(calculation.inputs);
    }

    @Override
    protected List<Float> getDisplayFloats() {
        return calculation.getLastOutput();
    }

    protected void onOutputChanged(int i, String s) {
        new ArrSetAction<>(calculation.outputs, i).addToHistory();
        calculation.outputs[i] = s;
    }

    protected void onInputChanged(int i, String s) {
        new ArrSetAction<>(calculation.inputs, i).addToHistory();
        calculation.inputs[i] = ParameterOverrideSource.parameterSourceFromString(s);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (ParameterTextWidget parameterTextWidget : outputs) {
            parameterTextWidget.setY(y);
        }
    }

    @Override
    protected void setExpanded(boolean to) {

    }

    @Override
    protected boolean getStoredExpanded() {
        return calculation.expanded;
    }

    @Override
    protected void setStoredExpanded(boolean to) {

    }

    public void setExpandedWithoutUpdate(boolean to) {
        expanded = to;
        tryCreateChildren();
        for (ParameterTextWidget parameterTextWidget : outputs) {
            parameterTextWidget.setVisible(expanded);
        }
    }
}
