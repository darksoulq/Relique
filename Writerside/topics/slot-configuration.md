# Slot Configuration
<link-summary>Guide to creating and modifying relic slots via JSON for both owners and developers</link-summary>

Relique uses a data-driven system for defining equipment slots. Rather than hardcoding slots into the plugin, slots are defined and modified using JSON files. This allows server owners to seamlessly create custom equipment ecosystems or tweak existing ones, while developers can bundle built-in slots directly within their plugins.

By default, Relique registers five standard slots for players:
* `head` (Size: 1)
* `chest` (Size: 1)
* `charm` (Size: 1)
* `belt` (Size: 1)
* `ring` (Size: 2)

### File Structure
Slot configurations are loaded based on their namespace. Where you place these files depends on whether you are configuring a live server or developing a custom add-on.

**For Server Owners:**
To create or modify a slot, place a JSON file in your server's plugin directory:
`plugins/Relique/relic/<namespace>/slots/<slot_id>.json`

For example, to modify the default ring slot, you would create:
`plugins/Relique/relic/relique/slots/ring.json`

**For Developers:**
To bundle default slots inside your own plugin jar, place the JSON files inside your project's `src/main/resources` folder using the exact same structure:
`src/main/resources/relic/<your_plugin_id>/slots/<slot_id>.json`

You must then instruct Relique to load your bundled resources during your plugin's startup phase:
```Java
@Override
public void onEnable() {
    // Scans your jar for the "relic/" directory and loads all slots, entities, and icons
    RelicLoader.loadResource(this);
}
```

### JSON Format
Below is an example of a fully configured slot definition. You do not need to include every field; any omitted fields will fall back to their default values (or the existing values, if you are modifying a pre-registered slot).

```json
{
  "operation": "SET",
  "order": 5,
  "icon": "relique:ring_outline",
  "size": 2,
  "drop_rule": "DEFAULT",
  "validators": [
    "relique:tag"
  ]
}
```

### Configuration Fields
The JSON file accepts the following properties to define the slot's behavior and appearance.

<table>
<tr>
<th>Field</th>
<th>Type</th>
<th>Description</th>
</tr>
<tr>
<td><code>operation</code></td>
<td>String</td>
<td>Determines how this file interacts with existing data. Can be <code>ADD</code>, <code>REMOVE</code>, or <code>SET</code>. Defaults to <code>ADD</code>.</td>
</tr>
<tr>
<td><code>order</code></td>
<td>Integer</td>
<td>The sorting order of the slot in the GUI. Lower numbers appear first. Defaults to <code>0</code>.</td>
</tr>
<tr>
<td><code>icon</code></td>
<td>String</td>
<td>The namespaced ID of the item or custom texture used as the empty slot background in the GUI. Defaults to empty.</td>
</tr>
<tr>
<td><code>size</code></td>
<td>Integer</td>
<td>The number of items this slot can hold. Defaults to <code>1</code>.</td>
</tr>
<tr>
<td><code>drop_rule</code></td>
<td>String</td>
<td>The rule dictating what happens to items in this slot on death. Defaults to <code>DEFAULT</code>.</td>
</tr>
<tr>
<td><code>validators</code></td>
<td>List&lt;String&gt;</td>
<td>A list of validator IDs determining which items are allowed to be equipped here. Defaults to <code>["relique:tag"]</code>.</td>
</tr>
</table>

---

### Custom Icons
The `icon` field dictates the background image of the slot when it is empty. You can use standard vanilla items (e.g., `minecraft:diamond`), or you can supply custom `.png` images.

If you specify a custom namespace (e.g., `"icon": "my_addon:custom_ring"`), Relique will automatically search for a corresponding image file and generate the necessary resource pack models and items under the hood.

To add a custom icon image:
1. Ensure your JSON file defines the custom ID (e.g., `"icon": "my_addon:custom_ring"`).
2. Place a `.png` file matching the ID name into the `icons` folder:
    * **Server Owners:** `plugins/Relique/relic/my_addon/icons/custom_ring.png`
    * **Developers:** `src/main/resources/relic/my_addon/icons/custom_ring.png`

Relique will handle the resource pack compilation and GUI rendering automatically.

---

### Slot Operations
Because Relique merges data from different namespaces and plugins, you must specify how your file affects the slot size using the `operation` field.

* **`ADD`:** Increases the current size of the slot by the specified `size` amount.
* **`REMOVE`:** Decreases the current size of the slot by the specified `size` amount.
* **`SET`:** Completely overrides the slot's size, ignoring previous configurations.

### Drop Rules
The `drop_rule` field controls the death mechanics for items equipped in the slot. It accepts the following values:

* **`DEFAULT`:** Follows standard vanilla gamerules (e.g., `keepInventory`).
* **`ALWAYS_KEEP`:** Items in this slot are never dropped on death, regardless of gamerules.
* **`ALWAYS_DROP`:** Items in this slot are always dropped on death, even if `keepInventory` is true.
* **`DESTROY`:** Items in this slot are deleted permanently upon death.

### Validators
Validators act as the gatekeepers for slots. By default, every slot uses the `relique:tag` validator, meaning an item can only be equipped if it belongs to the ItemTag matching the slot's ID. You can add custom validator keys here if another plugin registers them via the API.