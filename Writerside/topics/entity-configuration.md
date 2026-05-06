# Entity Configurations
<link-summary>Guide to assigning relic slots to specific entity types</link-summary>

Relique does not limit equipment slots exclusively to players. Using Entity Configurations, you can assign relic slots to any living entity. This includes standard vanilla mobs as well as custom entities registered through the AbyssalLib `CustomEntity` API.

This allows mobs to actually equip relics, benefit from their attribute modifiers, and potentially drop them upon death depending on the slot's drop rules.

### File Structure
Entity configurations are loaded based on their namespace. Where you place these files depends on whether you are configuring a live server or developing an add-on.

**For Server Owners:**
To create an entity configuration, place a JSON file in your server's plugin directory:
`plugins/Relique/relic/<namespace>/entities/<config_id>.json`

For example, to give zombies a ring slot, you might create:
`plugins/Relique/relic/relique/entities/zombie_slots.json`

**For Developers:**
To bundle entity configurations inside your own plugin jar, place the JSON files inside your project's `src/main/resources` folder:
`src/main/resources/relic/<your_plugin_id>/entities/<config_id>.json`

### JSON Format
Below is an example of an entity configuration file. It maps a list of entity types to a list of slot IDs.

```json
{
  "entities": [
    "minecraft:zombie",
    "minecraft:skeleton",
    "my_addon:custom_boss"
  ],
  "slots": [
    "ring",
    "charm"
  ]
}
```

### Configuration Fields
The JSON file requires two arrays to function correctly.

<table>
<tr>
<th>Field</th>
<th>Type</th>
<th>Description</th>
</tr>
<tr>
<td><code>entities</code></td>
<td>List&lt;String&gt;</td>
<td>A list of entity type IDs that will receive these slots. This strictly accepts standard vanilla namespaced keys (e.g., <code>minecraft:zombie</code>) as well as custom entity IDs registered natively through AbyssalLib's <code>CustomEntity</code> API.</td>
</tr>
<tr>
<td><code>slots</code></td>
<td>List&lt;String&gt;</td>
<td>A list of registered slot IDs (e.g., <code>ring</code>, <code>charm</code>, or your own custom slots) to grant to the specified entities.</td>
</tr>
</table>

<note>
When an entity spawns, Relique checks if its vanilla type or AbyssalLib CustomEntity ID matches any registered entity configs. If a match is found, the entity's underlying attribute data is updated to grant them the capacity to hold items in those specific slots.
</note>