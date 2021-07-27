package org.comroid.uniform.adapter.yml.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.comroid.uniform.adapter.AbstractSerializationAdapter;
import org.comroid.uniform.model.DataStructureType;
import org.comroid.uniform.model.ValueAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.uniform.node.impl.UniObjectNodeImpl;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Predicate;

public final class SpigotSerializationAdapter extends AbstractSerializationAdapter<ConfigurationSection, ConfigurationSection, ConfigurationSection> {
    public static final SpigotSerializationAdapter INSTANCE = new SpigotSerializationAdapter();

    private SpigotSerializationAdapter() {
        super("application/x-yml",
                ConfigurationSection.class, YamlConfiguration::new,
                ConfigurationSection.class, YamlConfiguration::new);
    }

    @Override
    public DataStructureType<ConfigurationSection, ? extends ConfigurationSection, ? extends UniNode> typeOfData(String data) {
        return null;
    }

    @Override
    public UniNode parse(@Nullable String data) {
        if (data == null)
            return createUniObjectNode(new YamlConfiguration());

        final YamlConfiguration configuration = new YamlConfiguration();

        try {
            configuration.load(data);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        return createUniObjectNode(configuration);
    }

    @Override
    public UniObjectNode createObjectNode(ConfigurationSection node) {
        return null;
    }

    @Override
    public UniArrayNode createArrayNode(ConfigurationSection node) {
        return null;
    }

    @Override
    public ValueAdapter<Object, Object> createValueAdapter(Object nodeBase, Predicate<Object> setter) {
        return null;
    }

    public UniObjectNode createUniObjectNode(ConfigurationSection configurationSection) {
        return new UniObjectNodeImpl(this, null, new SectionAdapter(configurationSection));
    }

    public UniArrayNode createUniArrayNode(ConfigurationSection configurationSection) {
        throw new UnsupportedOperationException("Cannot create UniArrayNodes");
    }
}
