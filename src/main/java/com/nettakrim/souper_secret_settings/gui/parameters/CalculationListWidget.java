package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.text.Text;

public class CalculationListWidget extends ListWidget {
    protected Calculation calculation;

    public CalculationListWidget(Calculation calculation, ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(calculation.getID()), listScreen);

        CalculationDisplayWidget calculationDisplayWidget = new CalculationDisplayWidget(calculation, layer, Text.literal(""), x, width, listScreen);
        children.add(calculationDisplayWidget);
        listScreen.addSelectable(calculationDisplayWidget);

        this.calculation = calculation;
    }

    @Override
    protected void setExpanded(boolean to) {
        ((CalculationDisplayWidget)children.getFirst()).setExpandedWithoutUpdate(to);
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
