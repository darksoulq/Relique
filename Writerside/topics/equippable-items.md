# Equippable Items
<link-summary>Guide to defining which items are allowed in slots using the tag system</link-summary>

By default, every relic slot uses the `relique:tag` validator. This validator restricts the slot so that only items belonging to a specific AbyssalLib `ItemTag` can be equipped. The required tag ID always exactly matches the slot's ID (e.g., the `relique:ring` slot requires the item to be in the `relique:ring` tag).

You define these tags using JSON or YAML files, utilizing `ItemPredicate` rules to match specific materials, NBT components, or nested conditions.

### For Server Owners: Creating Tag Files
Tag files are loaded by AbyssalLib, not Relique. To allow items into a slot, create a JSON file in the following directory:
`plugins/AbyssalLib/tags/item/<filename>.json`

The name of the file does not matter, but the `id` declared *inside* the file must match the slot ID you are targeting.

#### JSON Format Example
This example configures the `relique:ring` tag. It allows any Diamond, or any Gold Nugget that has a custom name.

```json
{
  "type": "abyssallib:item",
  "id": "relique:ring",
  "values": [
    {
      "id": "minecraft:diamond"
    },
    {
      "id": "minecraft:gold_nugget",
      "with": [
        "minecraft:custom_name"
      ]
    }
  ]
}
```

#### Predicate Fields
Each object inside the `values` array is an `ItemPredicate`. It supports the following optional fields to filter items:

<table>
<tr>
<th>Field</th>
<th>Type</th>
<th>Description</th>
</tr>
<tr>
<td><code>id</code></td>
<td>String</td>
<td>The exact vanilla material key the item must match (e.g., <code>minecraft:iron_ingot</code>).</td>
</tr>
<tr>
<td><code>with</code></td>
<td>List&lt;Condition&gt;</td>
<td>A list of component keys that the item <strong>must</strong> have (e.g., <code>minecraft:custom_data</code>).</td>
</tr>
<tr>
<td><code>without</code></td>
<td>List&lt;Condition&gt;</td>
<td>A list of component keys that the item <strong>must not</strong> have.</td>
</tr>
<tr>
<td><code>components</code></td>
<td>List&lt;Condition&gt;</td>
<td>A list of exact component objects the item must match perfectly (e.g., matching a specific custom model data integer).</td>
</tr>
</table>

#### Logical Conditions
Lists like `with`, `without`, and `components` evaluate using a `Condition` system. By default, items in a list are evaluated as single requirements. However, you can use `any_of` (OR) and `all_of` (AND) blocks for complex logic.

```json
{
  "id": "minecraft:stick",
  "with": [
    {
      "any_of": [
        "minecraft:enchantment_glint_override",
        "minecraft:custom_name"
      ]
    }
  ]
}
```
*The stick is allowed if it has EITHER an enchantment glint OR a custom name.*

---

### For Developers: Bundling Tags
If you are developing a custom add-on and want to register equippable items natively, do not hardcode item checks. Instead, bundle tag files directly into your plugin's resources.

Place your JSON tag files in your project's resources folder:
`src/main/resources/tags/item/<your_tag_file>.json`

During your plugin's startup phase, instruct AbyssalLib's `TagLoader` to scan and load the file:

```Java
@Override
public void onEnable() {
    // Loads a specific tag file from the jar
    TagLoader.loadResource(this, "tags/item/my_addon_rings.json");
    
    // OR, loads an entire folder of tags
    TagLoader.loadFolder(this, "tags/item");
}
```

Because AbyssalLib safely merges tag files that share the same `id`, your add-on can add items to the default `relique:ring` tag without overwriting the server owner's configurations or other plugins.