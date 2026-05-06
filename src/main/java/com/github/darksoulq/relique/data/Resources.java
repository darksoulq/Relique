package com.github.darksoulq.relique.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.darksoulq.abyssallib.server.resource.Namespace;
import com.github.darksoulq.abyssallib.server.resource.ResourcePack;
import com.github.darksoulq.abyssallib.server.resource.asset.*;
import com.github.darksoulq.abyssallib.server.resource.asset.definition.Selector;
import com.github.darksoulq.relique.Relique;
import com.github.darksoulq.relique.core.RelicLoader;
import com.github.darksoulq.relique.core.RelicManager;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Resources {
    public static ResourcePack RESOURCE_PACK;
    public static Namespace NAME_SPACE;

    public static Font.TextureGlyph RELIC_GUI;

    public static void setupAndRegister() {
        RESOURCE_PACK = new ResourcePack(Relique.INSTANCE, Relique.PLUGIN_ID);
        NAME_SPACE = RESOURCE_PACK.namespace(Relique.PLUGIN_ID);

        byte[] oldGuiBytes = null;
        File oldPack = new File(Relique.INSTANCE.getDataFolder().getParentFile().getParentFile(), "pack/resourcepack.zip");

        if (oldPack.exists()) {
            try (ZipFile zip = new ZipFile(oldPack)) {
                ZipEntry guiEntry = zip.getEntry("assets/relique/textures/gui/relic_ui.png");
                if (guiEntry != null) {
                    try (InputStream is = zip.getInputStream(guiEntry)) {
                        oldGuiBytes = is.readAllBytes();
                    }
                }

                Enumeration<? extends ZipEntry> entries = zip.entries();
                ObjectMapper mapper = new ObjectMapper();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().startsWith("assets/") && entry.getName().contains("/lang/") && entry.getName().endsWith(".json")) {
                        String name = entry.getName();
                        String langCode = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
                        try (InputStream is = zip.getInputStream(entry)) {
                            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                            Map<String, String> parsed = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
                            RelicLoader.LOADED_LANG.computeIfAbsent(langCode, k -> new HashMap<>()).putAll(parsed);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        Font font = NAME_SPACE.font("gui", false);
        Texture guiTex = oldGuiBytes != null ? NAME_SPACE.texture("gui/relic_ui", oldGuiBytes) : NAME_SPACE.texture("gui/relic_ui");
        RELIC_GUI = font.glyph(guiTex, 132, 13);

        for (Map.Entry<String, Map<String, String>> langEntry : RelicLoader.LOADED_LANG.entrySet()) {
            Lang lang = NAME_SPACE.lang(langEntry.getKey(), false);
            langEntry.getValue().forEach(lang::put);
        }

        Lang defaultLang = NAME_SPACE.lang("en_us", false);

        for (String slotId : RelicManager.getSlotsSorted()) {
            String iconId = RelicManager.getSlot(slotId).icon();
            if (iconId != null && iconId.contains(":")) {
                String[] parts = iconId.split(":", 2);
                if (!parts[0].equals("minecraft")) {
                    byte[] data = RelicLoader.LOADED_ICONS.get(iconId);
                    createItem(parts[0], parts[1], defaultLang, false, data);
                }
            }
        }

        createItem(Relique.PLUGIN_ID, "next", defaultLang, true, null);
        createItem(Relique.PLUGIN_ID, "prev", defaultLang, true, null);
        createItem(Relique.PLUGIN_ID, "up", defaultLang, true, null);
        createItem(Relique.PLUGIN_ID, "down", defaultLang, true, null);

        RESOURCE_PACK.register(true);
    }

    private static void createItem(String namespace, String name, Lang lang, boolean oversized, byte[] textureData) {
        Namespace ns = RESOURCE_PACK.namespace(namespace);
        Texture texture = textureData != null ? ns.texture("item/" + name, textureData) : ns.texture("item/" + name);
        Model model = ns.model("item/" + name, false);
        model.parent("minecraft:item/generated");
        model.texture("layer0", texture);
        ns.itemDefinition(name, new Selector.Model(model), true, oversized, 1.0);

        lang.put("item." + namespace + "." + name, formatName(name));
        lang.put("relique.slot." + name, formatName(name));
    }

    private static String formatName(String name) {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ");
    }
}