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
import com.mojang.brigadier.StringReader;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaderListCommand extends ListCommand<ShaderData> {
    protected final String name;
    protected final Identifier registry;
    protected final int warnLimit;
    protected final SuggestionProvider<FabricClientCommandSource> registrySuggestions;

    protected boolean warned;

    public ShaderListCommand(String name, Identifier registry, int warnLimit) {
        this.name = name;
        this.registry = registry;
        this.warnLimit = warnLimit;

        this.registrySuggestions = getRegistrySuggestions(registry, false);
    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:"+name).build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("shader", IdentifierArgumentType.identifier())
                                .suggests(registrySuggestions)
                                .executes((context -> add(context.getArgument("shader", Identifier.class), 1, false)))
                                .then(
                                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"), false)))
                                                .then(
                                                        ClientCommandManager.literal("force")
                                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"), true)))
                                                )
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

        LiteralCommandNode<FabricClientCommandSource> infoNode = ClientCommandManager
                .literal("info")
                .executes((context) -> info())
                .build();
        commandNode.addChild(infoNode);

        LiteralCommandNode<FabricClientCommandSource> groupNode = ClientCommandManager
                .literal("group")
                .then(
                        ClientCommandManager.literal("create")
                                .executes((context -> createGroup(Group.getNextName(SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry)))))
                                .then(
                                        ClientCommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> createGroup(StringArgumentType.getString(context, "name")))
                                )
                )
                .then(
                        ClientCommandManager.literal("modify")
                                .then(
                                        ClientCommandManager.argument("name", StringArgumentType.string())
                                                .suggests(groupSuggestions)
                                                .then(
                                                        ClientCommandManager.literal("add")
                                                                .then(
                                                                        ClientCommandManager.argument("value", StringArgumentType.string())
                                                                                .suggests(getRegistrySuggestions(registry, true))
                                                                                .executes(context -> addGroupEntry(StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "value"), -1))
                                                                                .then(
                                                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                                                .executes(context -> addGroupEntry(StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "value"), IntegerArgumentType.getInteger(context, "index")))
                                                                                )
                                                                )
                                                )
                                                .then(
                                                        ClientCommandManager.literal("remove")
                                                                .then(
                                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                                .suggests(groupIndexes)
                                                                                .executes(context -> removeGroupEntry(StringArgumentType.getString(context, "name"), IntegerArgumentType.getInteger(context, "index")))
                                                                )
                                                )
                                                .then(
                                                        ClientCommandManager.literal("toggle")
                                                                .then(
                                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                                .suggests(groupIndexes)
                                                                                .executes(context -> toggleGroupEntry(StringArgumentType.getString(context, "name"), IntegerArgumentType.getInteger(context, "index")))
                                                                )
                                                )
                                )
                )
                .then(
                        ClientCommandManager.literal("remove")
                                .then(
                                        ClientCommandManager.argument("name", StringArgumentType.string())
                                                .suggests(userGroupSuggestions)
                                                .executes((context -> removeGroup(StringArgumentType.getString(context, "name"))))
                                )
                )
                .build();
        commandNode.addChild(groupNode);

        registerList(commandNode);
    }

    public int add(Identifier id, int amount, boolean force) {
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(registry, id, amount, layer, true);
        if (shaders == null) {
            return 0;
        }

        List<ShaderData> list = layer.getList(registry);
        if (list.size()+amount > warnLimit) {
            if (!warned && SouperSecretSettingsClient.soupData.config.warning && !force) {
                warned = true;
                SouperSecretSettingsClient.say("shader.warn_stacking", warnLimit);
                return 1;
            }
        } else {
            warned = false;
        }

        for (ShaderData shaderData : shaders) {
            new ListAddAction<>(list, shaderData).addToHistory();
        }
        list.addAll(shaders);
        return 1;
    }

    @Override
    protected void onRemove() {
        if (getList().size() < warnLimit) {
            warned = false;
        }
    }

    public int toggle(CommandContext<FabricClientCommandSource> context) {
        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = layer.getList(registry);
        if (shaderIndex >= shaders.size()) {
            return 0;
        }

        ShaderData shaderData = shaders.get(shaderIndex);
        new ToggleAction(shaderData).addToHistory();
        shaderData.toggle();

        return 1;
    }


    public int setValue(CommandContext<FabricClientCommandSource> context) {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context, true);
        if (uniforms == null) {
            return 0;
        }

        String uniform = StringArgumentType.getString(context, "uniform");
        if (!uniforms.getFirst().containsKey(uniform)) {
            SouperSecretSettingsClient.say("shader.error.uniform", 1, uniform);
            return 0;
        }

        String name = StringArgumentType.getString(context, "name");
        int breakIndex = name.lastIndexOf('.');
        if (breakIndex <= 0) {
            SouperSecretSettingsClient.say("shader.error.name", 1, name);
            return 0;
        }

        int index = -1;
        try {
            index = Integer.parseInt(name.substring(breakIndex+1));
        } catch (Exception ignored) {}

        if (index < 0) {
            SouperSecretSettingsClient.say("shader.error.name", 1, name);
            return 0;
        }

        LuminanceUniformOverride override = (LuminanceUniformOverride)uniforms.getFirst().get(uniform).value;
        MapConfig config = (MapConfig)uniforms.getSecond().get(uniform).value;

        String text = name.substring(0, breakIndex);
        String value = StringArgumentType.getString(context, "value");

        if (text.equals("value")) {
            if (index >= override.overrideSources.size()) {
                SouperSecretSettingsClient.say("shader.error.value", 1, index, override.overrideSources.size()-1);
                return 0;
            }

            new UniformChangeAction(uniform, index, override, config).addToHistory();

            OverrideSource source = ParameterOverrideSource.parameterSourceFromString(value);
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
        } else {
            List<Object> values = config.getObjects(text);
            if (values == null) {
                SouperSecretSettingsClient.say("shader.error.object", 1, text);
                return 0;
            }
            if (index >= values.size()) {
                SouperSecretSettingsClient.say("shader.error.object_index", 1, index, values.size()-1);
                return 0;
            }

            int variable = -1;
            try {
                variable = Integer.parseInt(text.substring(0, text.indexOf('_')));
            } catch (Exception ignored) {}

            if (variable < 0 || variable >= override.overrideSources.size()) {
                SouperSecretSettingsClient.say("shader.error.value", 1, variable, override.overrideSources.size()-1);
                return 0;
            }

            new UniformChangeAction(uniform, variable, override, config).addToHistory();

            Object objectAtIndex = values.get(index);
            Object object;
            if (objectAtIndex == null || objectAtIndex instanceof Number) {
                try {
                    object = Float.parseFloat(value);
                } catch (Exception ignored) {
                    if (value.equals("null")) {
                        object = null;
                    } else {
                        SouperSecretSettingsClient.say("shader.error.number", 1, value);
                        return 0;
                    }
                }
            } else {
                object = value;
            }

            try {
                values.set(index, object);
            } catch (Exception e) {
                ArrayList<Object> valuesMutable = new ArrayList<>(values);
                valuesMutable.set(index, object);
                config.config.put(text, valuesMutable);
            }
        }

        return 1;
    }

    public int info() {
        List<ShaderData> shaders = getList().stream().filter(shaderData -> shaderData.active).toList();

        if (shaders.isEmpty()) {
            SouperSecretSettingsClient.say("shader.info.none", 1);
        } else {
            MutableText text = Text.empty();

            int count = 1;
            String key = SouperSecretSettingsClient.MODID+".shader.info";
            ShaderData shaderData = shaders.getFirst();
            ShaderData next = shaderData;

            int i = 0;
            boolean search;
            do {
                search = i != shaders.size()-1;
                if (search) next = shaders.get(i+1);

                if (!search || !shaderData.shader.getShaderId().equals(next.shader.getShaderId())) {
                    String s = shaderData.getTranslatedName().getString();
                    if (count == 1) {
                        text.append(Text.translatable(key, s));
                    } else {
                        text.append(Text.translatable(key+".multiple", s, count));
                    }
                    if (i == count-1) {
                        key = SouperSecretSettingsClient.MODID+".shader.info.join";
                    }
                    shaderData = next;
                    count = 0;
                }
                count++;
                i++;
            } while (search);

            SouperSecretSettingsClient.sayStyled(text, 1);
        }

        return 1;
    }

    private static SuggestionProvider<FabricClientCommandSource> getRegistrySuggestions(Identifier registry, boolean fromGroups) {
        return (context, builder) -> {
            String current = new StringReader(builder.getRemaining()).readString();
            Map<String, Identifier> paths = new HashMap<>();
            boolean searchPaths = true;

            List<ShaderRegistryEntry> registryEntries = Shaders.getRegistry(registry);
            for (ShaderRegistryEntry shaderRegistry : registryEntries) {
                Identifier identifier = shaderRegistry.getID();
                String name = identifier.toString();

                Text text = Text.translatableWithFallback("gui.luminance.shader."+name.replace(':','.')+".description", "");
                builder.suggest(name, text.getString().isBlank() ? null : text);

                if (searchPaths) {
                    if (identifier.getPath().startsWith(current)) {
                        paths.putIfAbsent(identifier.getPath(), identifier);
                    }

                    if (name.startsWith(current)) {
                        searchPaths = false;
                    }
                }
            }

            if (fromGroups) {
                builder.suggest("all", SouperSecretSettingsClient.translate("shader.group_suggestion", registryEntries.size()));
            } else if (registryEntries.size() >= 2) {
                builder.suggest("random", SouperSecretSettingsClient.translate("shader.group_suggestion", registryEntries.size()));
            }

            if (searchPaths) {
                for (Identifier identifier : paths.values()) {
                    builder.suggest(identifier.getPath(), Text.literal(identifier.toString()));
                }
            }

            SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).forEach(((name, shaderRegistryEntries) -> builder.suggest("random_" + name, SouperSecretSettingsClient.translate("shader.group_suggestion", shaderRegistryEntries.getComputed(registry, name).size()))));

            return builder.buildFuture();
        };
    }

    protected SuggestionProvider<FabricClientCommandSource> shaderSuggestions = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> SouperSecretSettingsClient.soupRenderer.activeLayer.getList(getRegistry()),
            ShaderData::getTranslatedName
    );

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

        return builder.buildFuture();
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformSuggestions = (context, builder) -> {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context, false);

        if (uniforms != null) {
            for (String string : uniforms.getFirst().keySet()) {
                builder.suggest(string);
            }
        }

        return builder.buildFuture();
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformNameSuggestions = (context, builder) -> {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context, false);

        if (uniforms != null) {
            String uniform = StringArgumentType.getString(context, "uniform");

            UniformData<UniformOverride> override = uniforms.getFirst().get(uniform);
            if (override != null) {
                for (int i = 0; i < ((LuminanceUniformOverride)override.value).overrideSources.size(); i++) {
                    builder.suggest("value." + i);
                }

                UniformConfig config = uniforms.getSecond().get(uniform).value;
                for (String string : config.getNames()) {
                    List<Object> objects = config.getObjects(string);
                    assert objects != null;
                    for (int i = 0; i < objects.size(); i++) {
                        builder.suggest(string + "." + i);
                    }
                }
            }
        }

        return builder.buildFuture();
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformValueSuggestions = (context, builder) -> {
        Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> uniforms = getUniformData(context, false);

        if (uniforms != null) {
            String uniform = StringArgumentType.getString(context, "uniform");
            UniformData<UniformOverride> override = uniforms.getFirst().get(uniform);
            if (override != null) {
                String name = StringArgumentType.getString(context, "name");
                int breakIndex = name.lastIndexOf('.');
                if (breakIndex > 0) {
                    int index = -1;
                    try {
                        index = Integer.parseInt(name.substring(breakIndex + 1));
                    } catch (Exception ignored) {
                    }

                    if (index >= 0) {
                        String defaultValue = null;
                        String currentValue = null;

                        String text = name.substring(0, breakIndex);
                        if (text.equals("value")) {
                            List<String> values = ((LuminanceUniformOverride) override.value).getStrings();
                            if (index < values.size()) {
                                currentValue = values.get(index);
                                defaultValue = ((LuminanceUniformOverride) override.defaultValue).getStrings().get(index);
                            }
                        } else {
                            UniformData<UniformConfig> config = uniforms.getSecond().get(uniform);
                            List<Object> values = config.value.getObjects(text);
                            if (values != null && index < values.size()) {
                                currentValue = values.get(index).toString();
                                List<Object> defaultObjects = config.defaultValue.getObjects(text);
                                defaultValue = (defaultObjects == null || index >= defaultObjects.size()) ? null : defaultObjects.get(index).toString();
                            }
                        }

                        if (currentValue != null) {
                            if (!currentValue.equals(defaultValue)) {
                                builder.suggest(currentValue, SouperSecretSettingsClient.translate("shader.value.current"));
                            }
                            if (defaultValue != null) {
                                builder.suggest(defaultValue, SouperSecretSettingsClient.translate("shader.value.default"));
                            }
                        }
                    }
                }
            }
        }

        return builder.buildFuture();
    };

    @Nullable
    protected Couple<Map<String, UniformData<UniformOverride>>,Map<String, UniformData<UniformConfig>>> getUniformData(CommandContext<FabricClientCommandSource> context, boolean feedback) {
        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = layer.getList(registry);
        if (shaderIndex >= shaders.size()) {
            if (feedback) {
                SouperSecretSettingsClient.say("shader.error.shader",1, shaderIndex, shaders.size()-1);
            }
            return null;
        }

        int passIndex = IntegerArgumentType.getInteger(context, "pass");
        int pass = passIndex;
        int total = 0;

        ShaderData shader = shaders.get(shaderIndex);

        for (Identifier identifier : SouperSecretSettingsClient.soupRenderer.getRegistryPasses(registry)) {
            PassData passData = shader.passDatas.get(identifier);
            if (passData != null) {
                int size = passData.overrides.size();
                if (pass < size) {
                    return new Couple<>(passData.overrides.get(pass), passData.configs.get(pass));
                }
                pass -= size;
                total += size;
            }
        }

        if (feedback) {
            SouperSecretSettingsClient.say("shader.error.pass", 1, passIndex, total-1);
        }
        return null;
    }

    protected final SuggestionProvider<FabricClientCommandSource> groupSuggestions = (context, builder) -> {
        Map<String, Group> groups = SouperSecretSettingsClient.soupRenderer.getShaderGroups(getRegistry());
        groups.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    };

    protected final SuggestionProvider<FabricClientCommandSource> userGroupSuggestions = (context, builder) -> {
        Map<String, Group> groups = SouperSecretSettingsClient.soupRenderer.getShaderGroups(getRegistry());
        groups.keySet().forEach(name -> {
            if (name.startsWith("user_")) {
                builder.suggest(name);
            }
        });
        return builder.buildFuture();
    };

    protected final SuggestionProvider<FabricClientCommandSource> groupIndexes = SouperSecretSettingsCommands.createIndexSuggestion(context -> SouperSecretSettingsClient.soupRenderer.getShaderGroups(getRegistry()).getOrDefault(StringArgumentType.getString(context, "name"), new Group()).entries, Text::literal);

    @Override
    List<ShaderData> getList() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer.getList(registry);
    }

    @Override
    String getID(ShaderData value) {
        return value.shader.getShaderId().toString();
    }

    @Override
    SuggestionProvider<FabricClientCommandSource> getIndexSuggestions() {
        return shaderSuggestions;
    }

    public Identifier getRegistry() {
        return registry;
    }

    public int createGroup(String name) {
        if (!name.startsWith("user_")) {
            name = "user_"+name;
        }

        Map<String, Group> map = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry);

        if (map.containsKey(name)) {
            SouperSecretSettingsClient.say("group.error.exists", 1, name);
            return 0;
        }

        if (name.isBlank()) {
            SouperSecretSettingsClient.say("group.error.value", 1, name);
            return 0;
        }

        Group group = new Group();
        map.put(name, group);
        SouperSecretSettingsClient.say("group.create", 0, name);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int addGroupEntry(String name, String entry, int index) {
        Group group = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).get(name);
        if (group == null) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }

        if (entry.isBlank()) {
            SouperSecretSettingsClient.say("group.error.value", 1, name);
            return 0;
        }

        int c = entry.charAt(0);
        if (c != '+' && c != '-') {
            entry = '+'+entry;
        }

        if (index < 0 || index > group.entries.size()) {
            group.entries.addLast(entry);
        } else {
            group.entries.add(index, entry);
        }

        SouperSecretSettingsClient.say("group.add", 0, entry);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int removeGroupEntry(String name, int index) {
        Group group = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).get(name);
        if (group == null) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }
        if (index >= group.entries.size()) {
            SouperSecretSettingsClient.say("group.error.index", 1, index, group.entries.size()-1);
            return 0;
        }
        String entry = group.entries.remove(index);
        SouperSecretSettingsClient.say("group.remove_entry", 0, index, entry);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int toggleGroupEntry(String name, int index) {
        Group group = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).get(name);
        if (group == null) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }
        if (index >= group.entries.size()) {
            SouperSecretSettingsClient.say("group.error.index", 1, index, group.entries.size()-1);
            return 0;
        }
        String entry = group.entries.get(index);
        entry = (entry.charAt(0) == '-' ? "+" : "-")+entry.substring(1);
        group.entries.set(index, entry);
        SouperSecretSettingsClient.say("group.toggle", 0, index, entry);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int removeGroup(String name) {
        Map<String, Group> map = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry);
        if (!name.startsWith("user_") && !name.isBlank()) {
            name = "user_"+name;
        }

        if (!(map.containsKey(name))) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }
        SouperSecretSettingsClient.say("group.remove", 0, name);
        map.remove(name).deleteFile();
        groupsChanged();
        return 1;
    }

    private void groupsChanged() {
        for (Group group : SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).values()) {
            group.requestUpdate();
        }
        SouperSecretSettingsClient.soupData.changeData(true);
    }
}
