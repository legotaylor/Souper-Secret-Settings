package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.text.Text;

public class ParameterWidget extends ListWidget {
    protected Calculation calculation;

    public ParameterWidget(Calculation calculation, ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(calculation.getID()), listScreen);

        CalculationWidget calculationWidget = new CalculationWidget(calculation, layer, Text.empty(), x, width, listScreen);
        children.add(calculationWidget);
        listScreen.addSelectable(calculationWidget);

        this.calculation = calculation;
    }

    @Override
    protected void createChildren(int x, int width) {
        // CalculationListWidget is just a wrapper for CalculationDisplayWidget, so it can create its child right away
    }

    @Override
    protected void setExpanded(boolean to) {
        ((CalculationWidget)children.getFirst()).setExpandedWithoutUpdate(to);
        super.setExpanded(to);
    }

    @Override
    protected boolean getStoredExpanded() {
        return calculation.expanded;
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        calculation.expanded = to;
    }

    @Override
    protected Toggleable getToggleable() {
        return calculation;
    }
}
