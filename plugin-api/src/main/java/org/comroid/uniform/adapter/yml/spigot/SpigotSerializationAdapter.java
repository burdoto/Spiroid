package org.comroid.uniform.adapter.yml.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SpigotSerializationAdapter extends SerializationAdapter<ConfigurationSection, ConfigurationSection, ConfigurationSection> {
    protected SpigotSerializationAdapter(String mimeType, Class<ConfigurationSection> configurationSectionClass, Class<ConfigurationSection> configurationSectionClass2) {
        super("application/x-yml", ConfigurationSection.class, ConfigurationSection.class);
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
    public UniObjectNode createUniObjectNode(ConfigurationSection configurationSection) {
        return new UniObjectNode(this, new SectionAdapter(configurationSection));
    }

    @Override
    public UniArrayNode createUniArrayNode(ConfigurationSection configurationSection) {
        throw new UnsupportedOperationException("Cannot create UniArrayNodes");
    }
}
