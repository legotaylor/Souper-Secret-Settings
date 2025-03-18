package com.nettakrim.souper_secret_settings.commands;

import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectProcessorInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mclegoman.luminance.common.util.Couple;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ToggleAction;
import com.nettakrim.souper_secret_settings.actions.UniformChangeAction;
import com.nettakrim.souper_secret_settings.shaders.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShaderListCommand extends ListCommand<ShaderData> {
    protected final String name;
    protected final Identifier registry;
    protected final SuggestionProvider<FabricClientCommandSource> registrySuggestions;

    public ShaderListCommand(String name, Identifier registry) {
        this.name = name;
        this.registry = registry;

        this.registrySuggestions = getRegistrySuggestions(registry);
    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:"+name).build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("shader", IdentifierArgumentType.identifier())
                                .suggests(registrySuggestions)
                                .executes((context -> add(context.getArgument("shader", Identifier.class), 1)))
                                .then(
                                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"))))
                                )
                )
                .build();
        commandNode.addChild(addNode);

        LiteralCommandNode<FabricClientCommandSource> modifyNode = ClientCommandManager
                .literal("modify")
                .then(
                        ClientCommandManager.argument("shader", IntegerArgumentType.integer(0))
                                .suggests(shaderSuggestions)
                                .then(
                                        ClientCommandManager.literal("toggle")
                                                .executes(this::toggle)
                                )
                                .then(
                                        ClientCommandManager
                                                .literal("parameter")
                                                .then(
                                                        ClientCommandManager.argument("pass", IntegerArgumentType.integer(0))
                                                                .suggests(passSuggestions)
                                                                .then(
                                                                        ClientCommandManager.argument("uniform", StringArgumentType.string())
                                                                                .suggests(uniformSuggestions)
                                                                                .then(
                                                                                        ClientCommandManager.argument("name", StringArgumentType.string())
                                                                                                .suggests(uniformNameSuggestions)
                                                                                                .then(
                                                                                                        ClientCommandManager.argument("value", StringArgumentType.string())
                                                                                                                .suggests(uniformValueSuggestions)
                                                                                                                .executes(this::setValue)
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                )
                .build();
        commandNode.addChild(modifyNode);

        registerList(commandNode);
    }

    public int add(Identifier id, int amount) {
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, id, amount, layer);
        if (shaders == null) {
            return -1;
        }

        List<ShaderData> list = layer.getList(registry);
        for (ShaderData shaderData : shaders) {
            new ListAddAction<>(list, shaderData).addToHistory();
        }
        list.addAll(shaders);
        return 1;
    }

    public int toggle(CommandContext<FabricClientCommandSource> context) {
        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = layer.getList(registry);
        if (shaderIndex >= shaders.size()) {
            return -1;
        }

        ShaderData shaderData = shaders.get(shaderIndex);
        new ToggleAction(shaderData).addToHistory();
        shaderData.toggle();

        return 1;
    }


    public int setValue(CommandContext<FabricClientCommandSource> context) {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context);

        if (uniforms == null) {
            return -1;
        }


        String uniform = StringArgumentType.getString(context, "uniform");
        String name = StringArgumentType.getString(context, "name");
        int breakIndex = name.lastIndexOf('.');
        if (breakIndex <= 0) {
            return -1;
        }

        int index = -1;
        try {
            index = Integer.parseInt(name.substring(breakIndex+1));
        } catch (Exception ignored) {}

        if (index < 0) {
            return -1;
        }

        LuminanceUniformOverride override = (LuminanceUniformOverride)uniforms.getFirst().get(uniform).value;
        MapConfig config = (MapConfig)uniforms.getSecond().get(uniform).value;
        new UniformChangeAction(uniform, index, override, config).addToHistory();

        String text = name.substring(0, breakIndex);
        String value = StringArgumentType.getString(context, "value");

        if (text.equals("value")) {
            if (index < override.overrideSources.size()) {
                OverrideSource source = ParameterOverrideSource.parameterSourceFromString(value);
                if (!value.isEmpty() && source instanceof ParameterOverrideSource) {
                    source = new MixOverrideSource(source);
                }
                override.overrideSources.set(index, source);

                String prefix = index+"_";
                MapConfig mapConfig = new MapConfig(List.of());
                UniformConfig templateConfig = source.getTemplateConfig();
                for (String templateName : templateConfig.getNames()) {
                    List<Object> objects = templateConfig.getObjects(templateName);
                    if (objects != null) {
                        mapConfig.config.put(prefix + templateName, new ArrayList<>(objects));
                    }
                }
                config.config.keySet().removeIf((s) -> s.startsWith(prefix) && !mapConfig.config.containsKey(s));
                config.mergeWithConfig(mapConfig);
            }
        } else {
            List<Object> values = config.getObjects(text);
            if (values != null && index < values.size()) {

                Object objectAtIndex = values.get(index);
                Object object;
                if (objectAtIndex instanceof Number) {
                    try {
                        object = Float.parseFloat(value);
                    } catch (Exception ignored) {
                        return -1;
                    }
                } else {
                    object = value;
                }

                values.set(index, object);
            }
        }

        return 1;
    }

    private static SuggestionProvider<FabricClientCommandSource> getRegistrySuggestions(Identifier registry) {
        return (context, builder) -> {
            List<ShaderRegistryEntry> registryEntries = Shaders.getRegistry(registry);
            for (ShaderRegistryEntry shaderRegistry : registryEntries) {
                builder.suggest(shaderRegistry.getID().toString());
            }
            if (registryEntries.size() >= 2) builder.suggest("random");

            Map<String, List<ShaderRegistryEntry>> registryGroups = SouperSecretSettingsClient.soupRenderer.shaderGroups.get(registry);
            if (registryGroups != null) {
                for (String s : registryGroups.keySet()) {
                    builder.suggest("random_"+s);
                }
            }

            return CompletableFuture.completedFuture(builder.build());
        };
    }

    protected SuggestionProvider<FabricClientCommandSource> shaderSuggestions = (context, builder) -> {
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = layer.getList(getRegistry());
        for (int i = 0; i < shaders.size(); i++) {
            String s = shaders.get(i).shader.getShaderId().toString();
            builder.suggest(i, Text.translatableWithFallback("gui.luminance.shader."+s.replace(':','.'), s));
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    protected SuggestionProvider<FabricClientCommandSource> passSuggestions = (context, builder) -> {
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");

        List<ShaderData> shaders = layer.getList(getRegistry());
        if (shaderIndex < shaders.size()) {
            ShaderData shader = shaders.get(shaderIndex);

            int total = 0;
            for (Identifier identifier : SouperSecretSettingsClient.soupRenderer.getRegistryPasses(getRegistry())) {
                PostEffectProcessorInterface processor = (PostEffectProcessorInterface) shader.shader.getPostProcessor();
                List<PostEffectPass> passes = processor.luminance$getPasses(identifier);
                if (passes != null) {
                    for (PostEffectPass pass : passes) {
                        builder.suggest(total, Text.literal(((PostEffectPassInterface) pass).luminance$getID().replace(":post/", ":")));
                        total++;
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformSuggestions = (context, builder) -> {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context);

        if (uniforms != null) {
            for (String string : uniforms.getFirst().keySet()) {
                builder.suggest(string);
            }
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformNameSuggestions = (context, builder) -> {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context);

        if (uniforms != null) {
            String uniform = StringArgumentType.getString(context, "uniform");

            UniformOverride override = uniforms.getFirst().get(uniform).value;
            for (int i = 0; i < ((LuminanceUniformOverride)override).overrideSources.size(); i++) {
                builder.suggest("value."+i);
            }

            UniformConfig config = uniforms.getSecond().get(uniform).value;
            for (String string : config.getNames()) {
                List<Object> objects = config.getObjects(string);
                assert objects != null;
                for (int i = 0; i < objects.size(); i++) {
                    builder.suggest(string+ "."+i);
                }
            }
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformValueSuggestions = (context, builder) -> {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context);

        if (uniforms != null) {
            String uniform = StringArgumentType.getString(context, "uniform");
            String name = StringArgumentType.getString(context, "name");
            int breakIndex = name.lastIndexOf('.');
            if (breakIndex > 0) {
                int index = -1;
                try {
                    index = Integer.parseInt(name.substring(breakIndex+1));
                } catch (Exception ignored) {}

                if (index >= 0) {
                    String text = name.substring(0, breakIndex);
                    if (text.equals("value")) {
                        UniformData<UniformOverride> override = uniforms.getFirst().get(uniform);
                        List<String> values = ((LuminanceUniformOverride) override.value).getStrings();
                        if (index < values.size()) {
                            builder.suggest(values.get(index), Text.literal("Current"));
                            builder.suggest(((LuminanceUniformOverride) override.defaultValue).getStrings().get(index), Text.literal("Default"));
                        }
                    } else {
                        UniformData<UniformConfig> config = uniforms.getSecond().get(uniform);
                        List<Object> values = config.value.getObjects(text);
                        if (values != null && index < values.size()) {
                            builder.suggest(values.get(index).toString(), Text.literal("Current"));
                            List<Object> defaultObjects = config.defaultValue.getObjects(text);
                            if (defaultObjects != null && index < defaultObjects.size()) {
                                builder.suggest(defaultObjects.get(index).toString(), Text.literal("Default"));
                            }
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    @Nullable
    protected Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> getUniformData(CommandContext<FabricClientCommandSource> context) {
        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = layer.getList(registry);
        if (shaderIndex >= shaders.size()) {
            return null;
        }

        int pass = IntegerArgumentType.getInteger(context, "pass");

        ShaderData shader = shaders.get(shaderIndex);

        for (Identifier identifier : SouperSecretSettingsClient.soupRenderer.getRegistryPasses(registry)) {
            PassData passData = shader.passDatas.get(identifier);
            int size = passData.overrides.size();
            if (pass < size) {
                return new Couple<>(passData.overrides.get(pass), passData.configs.get(pass));
            }
            pass -= size;
        }

        return null;
    }

    @Override
    List<ShaderData> getList() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer.getList(registry);
    }

    public Identifier getRegistry() {
        return registry;
    }
}
