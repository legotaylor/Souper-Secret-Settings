package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterScreen extends ListScreen<Calculation> {
    public final ShaderLayer layer;

    public ParameterScreen(int scrollIndex, ShaderLayer layer) {
        super(scrollIndex);
        this.layer = layer;
    }

    @Override
    protected List<Calculation> getListValues() {
        return layer.calculations;
    }

    @Override
    protected ListWidget createListWidget(Calculation value) {
        return new CalculationListWidget(value, layer, this, listX, listWidth);
    }

    @Override
    public List<String> getAdditions() {
        List<String> calculations = new ArrayList<>(Calculations.getIds());
        Collections.sort(calculations);
        return calculations;
    }

    @Override
    public Calculation tryGetAddition(String addition) {
        return Calculations.createCalcultion(addition);
    }
}
