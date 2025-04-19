package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderRegistryEntry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.util.Identifier;

import java.util.*;

public class Group {
    public List<String> entries;

    protected List<ShaderRegistryEntry> registryShaders;

    private List<ShaderRegistryEntry> computed = null;
    private boolean needsUpdate = true;

    public Group() {
        this.entries = new ArrayList<>();
        this.registryShaders = new ArrayList<>();
    }

    public List<Integer> getStepAmounts(Identifier registry) {
        int previous = 0;
        List<Integer> steps = new ArrayList<>(entries.size());
        Set<ShaderRegistryEntry> shadersSet = new HashSet<>(computed == null ? 16 : computed.size());

        for (String entry : entries) {
            String id = entry.substring(1);
            if (id.startsWith("random_")) {
                Group group = getGroup(registry, id);
                if (group != null && group != this && group.hasRecursion(registry, Set.of(this))) {
                    steps.add(null);
                    continue;
                }
            }

            computationStep(registry, shadersSet, entry);

            int next = shadersSet.size();
            steps.add(next-previous);
            previous = next;
        }

        return steps;
    }

    protected boolean hasRecursion(Identifier registry, Set<Group> visited) {
        Set<Group> next = new HashSet<>(visited);
        next.add(this);


        for (String entry : entries) {
            String id = entry.substring(1);
            if (id.startsWith("random_")) {
                Group group = getGroup(registry, id);
                if (group != null && group != this) {
                    if (visited.contains(group) || group.hasRecursion(registry, next)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public List<ShaderRegistryEntry> getComputed(Identifier registry) {
        if (needsUpdate) {
            needsUpdate = false;

            if (hasRecursion(registry, Set.of())) {
                computed = List.of();
                return computed;
            }

            Set<ShaderRegistryEntry> shadersSet = new HashSet<>();
            for (String entry : entries) {
                computationStep(registry, shadersSet, entry);
            }
            computed = shadersSet.stream().toList();
        }
        return computed;
    }

    private void computationStep(Identifier registry, Set<ShaderRegistryEntry> shadersSet, String entry) {
        boolean remove = entry.charAt(0) == '-';
        String id = entry.substring(1);
        if (id.startsWith("random_")) {
            Group group = getGroup(registry, id);
            if (group != null) {
                List<ShaderRegistryEntry> groupRegistries;

                if (group == this) {
                    groupRegistries = registryShaders;
                } else {
                    groupRegistries = group.getComputed(registry);
                }

                if (remove) {
                    groupRegistries.forEach(shadersSet::remove);
                } else {
                    shadersSet.addAll(groupRegistries);
                }
            }
        } else {
            ShaderRegistryEntry registryEntry = Shaders.get(registry, Shaders.guessPostShader(registry, id));
            if (remove) {
                shadersSet.remove(registryEntry);
            } else {
                shadersSet.add(registryEntry);
            }
        }
    }

    protected Group getGroup(Identifier registry, String randomID) {
        Map<String, Group> registryGroups = SouperSecretSettingsClient.soupRenderer.shaderGroups.get(registry);
        if (registryGroups == null) {
            return null;
        }
        return registryGroups.get(randomID.substring(7));
    }

    public void requestUpdate() {
        needsUpdate = true;
    }
}
