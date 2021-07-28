package org.comroid.uniform.adapter.yml.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class SectionAdapter implements Map<String, Object> {
    private final ConfigurationSection section;

    @Override
    public boolean isEmpty() {
        return section.getKeys(false).isEmpty();
    }

    SectionAdapter(ConfigurationSection section) {
        this.section = section;
    }

    @Override
    public int size() {
        return section.getKeys(false).size();
    }

    @Override
    public boolean containsKey(Object key) {
        return section.contains(String.valueOf(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return section.getKeys(false)
                .stream()
                .map(section::get)
                .anyMatch(value::equals);
    }

    @Override
    public Object get(Object key) {
        return section.get(String.valueOf(key));
    }

    @Override
    public Object put(String name, Object value) {
        section.set(name, value);
        return null;
    }

    @Override
    public Object remove(Object key) {
        return put(String.valueOf(key), null);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        section.getKeys(false).forEach(this::remove);
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return section.getKeys(false);
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return section.getValues(false).values();
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet() {
        final HashSet<Entry<String, Object>> yields = new HashSet<>();
        section.getKeys(false)
                .forEach(key -> yields.add(new AbstractMap.SimpleImmutableEntry<>(key, section.get(key))));

        return yields;
    }
}
