package com.github.darksoulq.relique.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.darksoulq.abyssallib.common.serialization.ops.JsonOps;
import com.github.darksoulq.abyssallib.common.util.Try;
import com.github.darksoulq.abyssallib.server.registry.Registries;
import com.github.darksoulq.abyssallib.world.data.attribute.Attribute;
import com.github.darksoulq.abyssallib.world.item.Item;
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemModel;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.api.DropRule;
import com.github.darksoulq.relique.api.EntitySlotConfig;
import com.github.darksoulq.relique.api.RelicSlot;
import com.github.darksoulq.relique.api.SlotOperation;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class RelicLoader {
    private static final Path ROOT_DIR = new File(Relique.INSTANCE.getDataFolder(), "relic").toPath();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final Map<String, EntitySlotConfig> ENTITY_CONFIGS = new HashMap<>();
    public static final Map<String, byte[]> LOADED_ICONS = new HashMap<>();
    public static final Map<String, Map<String, String>> LOADED_LANG = new HashMap<>();

    public static void clear() {
        RelicManager.clear();
        ENTITY_CONFIGS.clear();
        LOADED_ICONS.clear();
        LOADED_LANG.clear();
    }

    public static void load() {
        if (!Files.exists(ROOT_DIR)) {
            try {
                Files.createDirectories(ROOT_DIR);
            } catch (IOException ignored) {
                return;
            }
        }

        try (Stream<Path> namespaces = Files.list(ROOT_DIR)) {
            namespaces.filter(Files::isDirectory).forEach(nsDir -> {
                String namespace = nsDir.getFileName().toString();
                loadNamespace(namespace, nsDir);
            });
        } catch (IOException ignored) {}
    }

    private static void loadNamespace(String namespace, Path nsDir) {
        Path slotsDir = nsDir.resolve("slots");
        if (Files.exists(slotsDir)) {
            try (Stream<Path> stream = Files.walk(slotsDir)) {
                stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                    String id = getFilenameWithoutExtension(path);
                    try {
                        JsonNode node = MAPPER.readTree(path.toFile());
                        applySlotDef(id, namespace, node);
                    } catch (IOException ignored) {}
                });
            } catch (IOException ignored) {}
        }

        Path iconsDir = nsDir.resolve("icons");
        if (Files.exists(iconsDir)) {
            try (Stream<Path> stream = Files.walk(iconsDir)) {
                stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".png")).forEach(path -> {
                    String iconName = getFilenameWithoutExtension(path);
                    try {
                        byte[] data = Files.readAllBytes(path);
                        LOADED_ICONS.put(namespace + ":" + iconName, data);
                    } catch (IOException ignored) {}
                });
            } catch (IOException ignored) {}
        }

        Path entitiesDir = nsDir.resolve("entities");
        if (Files.exists(entitiesDir)) {
            try (Stream<Path> stream = Files.walk(entitiesDir)) {
                stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                    EntitySlotConfig config = Try.of(() -> EntitySlotConfig.CODEC.decode(JsonOps.INSTANCE, MAPPER.readTree(path.toFile()))).orElse(null);
                    mergeEntityConfig(config);
                });
            } catch (IOException ignored) {}
        }

        Path langDir = nsDir.resolve("lang");
        if (Files.exists(langDir)) {
            try (Stream<Path> stream = Files.walk(langDir)) {
                stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                    String langCode = getFilenameWithoutExtension(path);
                    try {
                        Map<String, String> langData = MAPPER.readValue(path.toFile(), new TypeReference<Map<String, String>>() {});
                        LOADED_LANG.computeIfAbsent(langCode, k -> new HashMap<>()).putAll(langData);
                    } catch (IOException ignored) {}
                });
            } catch (IOException ignored) {}
        }
    }

    public static void loadResource(Plugin plugin) {
        try {
            File pluginFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            try (JarFile jar = new JarFile(pluginFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith("relic/") && !entry.isDirectory()) {
                        String[] parts = name.split("/");
                        if (parts.length >= 4) {
                            String namespace = parts[1];
                            String folder = parts[2];
                            String file = parts[parts.length - 1];
                            String id = file.contains(".") ? file.substring(0, file.lastIndexOf('.')) : file;

                            try (InputStream in = jar.getInputStream(entry)) {
                                if (folder.equals("slots") && name.endsWith(".json")) {
                                    JsonNode node = MAPPER.readTree(in);
                                    applySlotDef(id, namespace, node);
                                } else if (folder.equals("entities") && name.endsWith(".json")) {
                                    EntitySlotConfig config = EntitySlotConfig.CODEC.decode(JsonOps.INSTANCE, MAPPER.readTree(in));
                                    mergeEntityConfig(config);
                                } else if (folder.equals("icons") && name.endsWith(".png")) {
                                    LOADED_ICONS.put(namespace + ":" + id, in.readAllBytes());
                                } else if (folder.equals("lang") && name.endsWith(".json")) {
                                    Map<String, String> langData = MAPPER.readValue(in, new TypeReference<Map<String, String>>() {});
                                    LOADED_LANG.computeIfAbsent(id, k -> new HashMap<>()).putAll(langData);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void mergeEntityConfig(EntitySlotConfig config) {
        if (config == null || config.entities() == null || config.slots() == null) return;
        for (String entityType : config.entities()) {
            EntitySlotConfig existing = ENTITY_CONFIGS.get(entityType);
            if (existing == null) {
                ENTITY_CONFIGS.put(entityType, new EntitySlotConfig(List.of(entityType), new ArrayList<>(config.slots())));
            } else {
                List<String> mergedSlots = new ArrayList<>(existing.slots());
                for (String slot : config.slots()) {
                    if (!mergedSlots.contains(slot)) {
                        mergedSlots.add(slot);
                    }
                }
                ENTITY_CONFIGS.put(entityType, new EntitySlotConfig(List.of(entityType), mergedSlots));
            }
        }
    }

    private static void applySlotDef(String id, String namespace, JsonNode node) {
        RelicSlot existing = RelicManager.getSlot(id);

        int order = node.has("order") ? node.get("order").asInt() : (existing != null ? existing.order() : 0);
        String icon = node.has("icon") ? node.get("icon").asText() : (existing != null ? existing.icon() : "");

        DropRule dropRule = existing != null ? existing.dropRule() : DropRule.DEFAULT;
        if (node.has("drop_rule")) {
            dropRule = Try.of(() -> DropRule.valueOf(node.get("drop_rule").asText().toUpperCase())).orElse(dropRule);
        }

        List<Key> validators = existing != null ? new ArrayList<>(existing.validators()) : new ArrayList<>(List.of(Key.key("relique:tag")));
        if (node.has("validators")) {
            validators.clear();
            for (JsonNode valNode : node.get("validators")) {
                validators.add(Key.key(valNode.asText()));
            }
        }

        int sizeMod = node.has("size") ? node.get("size").asInt() : 0;
        SlotOperation op = SlotOperation.ADD;
        if (node.has("operation")) {
            op = Try.of(() -> SlotOperation.valueOf(node.get("operation").asText().toUpperCase())).orElse(SlotOperation.ADD);
        }

        int finalSize;
        if (existing == null) {
            finalSize = op == SlotOperation.SET ? sizeMod : Math.max(1, sizeMod);
        } else {
            finalSize = existing.size();
            if (op == SlotOperation.ADD) finalSize += sizeMod;
            else if (op == SlotOperation.REMOVE) finalSize -= sizeMod;
            else if (op == SlotOperation.SET) finalSize = sizeMod;

            finalSize = Math.max(0, finalSize);
        }

        RelicSlot slot = new RelicSlot(order, icon, finalSize, dropRule, validators);
        RelicManager.registerSlot(id, slot);
        RelicTags.register(namespace, id);

        String attrKey = "relique:" + id;
        if (!Registries.ATTRIBUTES.contains(attrKey)) {
            Registries.ATTRIBUTES.register(attrKey, new Attribute(Key.key("relique", id), finalSize));
        }

        if (icon != null && icon.contains(":") && !icon.startsWith("minecraft:")) {
            if (!Registries.ITEMS.contains(icon)) {
                String[] parts = icon.split(":", 2);
                Item item = new Item(Key.key(parts[0], parts[1]), Material.PAPER);
                item.setData(new ItemModel(new NamespacedKey(parts[0], parts[1])));
                item.tooltip.setVisible(false);
                item.updateTooltip();
                Registries.ITEMS.register(icon, item);
            }
        }
    }

    public static void loadResource(Plugin plugin, String ignoredPath) {
        loadResource(plugin);
    }

    private static String getFilenameWithoutExtension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}