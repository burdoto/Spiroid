package org.comroid.uniform.adapter.yml.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.comroid.uniform.node.UniObjectNode;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class SectionAdapter extends UniObjectNode.Adapter<ConfigurationSection> {
    SectionAdapter(ConfigurationSection section) {
        super(section);
    }

    @Override
    public Object put(String name, Object value) {
        getBaseNode().set(name, value);
        return null;
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet() {
        final HashSet<Entry<String, Object>> yields = new HashSet<>();
        getBaseNode().getKeys(false)
                .forEach(key -> yields.add(new SimpleImmutableEntry<>(key, getBaseNode().get(key))));

        return yields;
    }
}
