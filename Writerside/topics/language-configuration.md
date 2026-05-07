# Language Configuration
<link-summary>Guide to translating GUI elements, slot names, and custom icons</link-summary>

Relique uses a data-driven approach for language and translations. By providing JSON language files, you can translate GUI text, slot names, and the names of custom items or icons.

When the server starts, Relique reads these files and automatically injects them into the AbyssalLib resource pack compilation process.

### File Structure
Language files are loaded based on their namespace and the Minecraft language code (e.g., `en_us`, `es_es`, `fr_fr`).

**For Server Owners:**
To add or modify translations, place a JSON file in your server's plugin directory:
`plugins/Relique/relic/<namespace>/lang/<language_code>.json`

For example, to add Spanish translations for the core plugin, create:
`plugins/Relique/relic/relique/lang/es_es.json`

**For Developers:**
To bundle default translations inside your own plugin jar, place the JSON files inside your project's `src/main/resources` folder:
`src/main/resources/relic/<your_plugin_id>/lang/<language_code>.json`

> Bundled resources are automatically loaded when you call `RelicLoader.loadResource(this);` during your plugin's startup.

### JSON Format
Language files use a simple Key-Value format identical to vanilla Minecraft resource packs.

```json
{
  "relique.slot.ring": "Anillo",
  "relique.slot.charm": "Amuleto",
  "relique.slot.custom": "Icono Personalizado"
}
```

### Auto-Generated Translations
If you add a custom slot or icon (e.g., `"icon": "my_addon:ruby_ring"`), Relique will automatically generate a basic, capitalized English name for it inside the `en_us` language file.

For example, `ruby_ring` is automatically registered as "Ruby ring" for both the item name (`item.my_addon.ruby_ring`) and the slot title (`relique.slot.ruby_ring`).

You only need to define these translation keys in your own language files if you want to explicitly override the auto-generated English name, or if you are providing translations for other client languages.