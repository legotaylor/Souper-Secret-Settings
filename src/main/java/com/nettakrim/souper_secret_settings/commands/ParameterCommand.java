package com.nettakrim.souper_secret_settings.commands;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ArrSetAction;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ToggleAction;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ParameterCommand extends ListCommand<Calculation> {
    public ParameterCommand() {

    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:parameter").build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("id", StringArgumentType.string())
                                .suggests(calculationIDs)
                                .executes(context -> add(StringArgumentType.getString(context, "id")))
                )
                .build();
        commandNode.addChild(addNode);

        LiteralCommandNode<FabricClientCommandSource> modifyNode = ClientCommandManager
                .literal("modify")
                .then(
                        ClientCommandManager.argument("parameter", IntegerArgumentType.integer(0))
                                .suggests(calculationIndexes)
                                .then(
                                        ClientCommandManager.literal("input")
                                                .then(
                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                .suggests(calculationInputs)
                                                                .then(
                                                                        ClientCommandManager.argument("value", StringArgumentType.string())
                                                                                .suggests(calculationInputValue)
                                                                                .executes(this::setInput)
                                                                )
                                                )
                                )
                                .then(
                                        ClientCommandManager.literal("output")
                                                .then(
                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                .suggests(calculationOutputs)
                                                                .then(
                                                                        ClientCommandManager.argument("value", StringArgumentType.string())
                                                                                .suggests(calculationOutputValue)
                                                                                .executes(this::setOutput)
                                                                )
                                                )
                                )
                                .then(
                                        ClientCommandManager.literal("toggle")
                                                .executes(this::toggle)
                                )
                )
                .build();
        commandNode.addChild(modifyNode);

        registerList(commandNode);
    }

    int add(String id) {
        Calculation calculation = Calculations.createCalcultion(id);
        if (calculation == null) {
            return -1;
        }

        new ListAddAction<>(SouperSecretSettingsClient.soupRenderer.activeLayer.calculations, calculation).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.add(calculation);
        return 1;
    }

    int toggle(CommandContext<FabricClientCommandSource> context) {
        Calculation calculation = getCalculation(context);
        if (calculation == null) {
            return 0;
        }

        new ToggleAction(calculation).addToHistory();
        calculation.toggle();
        return 1;
    }

    int setInput(CommandContext<FabricClientCommandSource> context) {
        Calculation calculation = getCalculation(context);
        if (calculation == null) {
            return 0;
        }

        int index = IntegerArgumentType.getInteger(context, "index");
        if (index >= calculation.inputs.length) {
            return 0;
        }

        String value = StringArgumentType.getString(context, "value");
        new ArrSetAction<>(calculation.inputs, index);
        calculation.inputs[index] = ParameterOverrideSource.parameterSourceFromString(value);

        return 1;
    }

    int setOutput(CommandContext<FabricClientCommandSource> context) {
        Calculation calculation = getCalculation(context);
        if (calculation == null) {
            return 0;
        }

        int index = IntegerArgumentType.getInteger(context, "index");
        if (index >= calculation.outputs.length) {
            return 0;
        }

        String value = StringArgumentType.getString(context, "value");
        new ArrSetAction<>(calculation.outputs, index);
        calculation.outputs[index] = value;

        return 1;
    }

    @Nullable
    static Calculation getCalculation(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "parameter");
        if (index < SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.size()) {
            return SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.get(index);
        }
        return null;
    }

    static SuggestionProvider<FabricClientCommandSource> calculationIDs = (context, builder) -> {
        for (String id : Calculations.getIds()) {
            builder.suggest(id);
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    static SuggestionProvider<FabricClientCommandSource> calculationIndexes = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> SouperSecretSettingsClient.soupRenderer.activeLayer.calculations,
            (value) -> Text.literal(value.getID())
    );

    static SuggestionProvider<FabricClientCommandSource> calculationInputs = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context);
                return calculation == null ? null : List.of(calculation.inputNames);
            },
            Text::literal
    );

    static SuggestionProvider<FabricClientCommandSource> calculationInputValue = SouperSecretSettingsCommands.createValueSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context);
                return calculation == null ? null : List.of(calculation.inputs);
            },
            OverrideSource::getString,
            "index"
    );

    static SuggestionProvider<FabricClientCommandSource> calculationOutputs = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context);
                return calculation == null ? null : List.of(calculation.outputs);
            },
            (message) -> null
    );

    static SuggestionProvider<FabricClientCommandSource> calculationOutputValue = SouperSecretSettingsCommands.createValueSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context);
                return calculation == null ? null : List.of(calculation.outputs);
            },
            (value) -> value,
            "index"
    );


    @Override
    List<Calculation> getList() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer.calculations;
    }

    @Override
    SuggestionProvider<FabricClientCommandSource> getIndexSuggestions() {
        return calculationIndexes;
    }
}
