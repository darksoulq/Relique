package com.github.darksoulq.relique.data;

import com.github.darksoulq.abyssallib.server.resource.Namespace;
import com.github.darksoulq.abyssallib.server.resource.ResourcePack;
import com.github.darksoulq.abyssallib.server.resource.asset.*;
import com.github.darksoulq.abyssallib.server.resource.asset.definition.Selector;
import com.github.darksoulq.relique.Relique;

public class Resources {
    public static ResourcePack RESOURCE_PACK;
    public static Namespace NAME_SPACE;

    public static Font.TextureGlyph RELIC_GUI;

    public static void setup() {
        RESOURCE_PACK = new ResourcePack(Relique.INSTANCE, Relique.PLUGIN_ID);
        NAME_SPACE = RESOURCE_PACK.namespace(Relique.PLUGIN_ID);

        Font font = NAME_SPACE.font("gui", false);

        RELIC_GUI = font.glyph(NAME_SPACE.texture("gui/relic_ui"), 132, 13);

        Lang lang = NAME_SPACE.lang("en_us", false);

        createIconItem("charm", "Charm", lang);
        createIconItem("head", "Head", lang);
        createIconItem("chest", "Chest", lang);
        createIconItem("belt", "Belt", lang);
        createIconItem("ring", "Ring", lang);

        createItem("next", "Next", lang, true);
        createItem("prev", "Previous", lang, true);
        createItem("up", "Up", lang, true);
        createItem("down", "Down", lang, true);
    }

    public static void register() {
        RESOURCE_PACK.register(false);
    }

    private static void createItem(String name, String displayName, Lang lang, boolean oversized) {
        Texture texture = NAME_SPACE.texture("item/" + name);
        Model model = NAME_SPACE.model("item/" + name, false);
        model.parent("minecraft:item/generated");
        model.texture("layer0", texture);
        Selector.Model selector = new Selector.Model(model);
        NAME_SPACE.itemDefinition(name, selector, true, oversized);
        lang.put("item.relique." + name, displayName);
    }
    private static void createIconItem(String name, String displayName, Lang lang) {
        createItem(name, displayName, lang, false);
        lang.put("relique.slot." + name, displayName);
    }
}
