# Attributes & Equip Sounds
<link-summary>Guide to adding attribute modifiers and custom sounds to relic items</link-summary>

Items equipped in relic slots can grant attribute modifiers (like extra health or speed) and play specific sound effects when equipped or unequipped. This data is stored directly on the item using custom data components, allowing you to configure unique stats and sounds per item.

### For Server Owners: NBT Configuration
To create a relic with custom attributes or sounds using in-game commands or vanilla datapacks, you must define them inside the `minecraft:custom_data` component.

AbyssalLib automatically reads any data placed inside a nested `CustomComponents` tag, which itself sits inside a `CustomData` tag.

#### Format Details
<table>
<tr>
<th>Component ID</th>
<th>Expected NBT Structure</th>
</tr>
<tr>
<td><code>relique:relic_properties</code></td>
<td>A list of modifier objects containing <code>attribute</code>, <code>id</code>, <code>amount</code>, <code>operation</code>, and a list of valid <code>slots</code>.</td>
</tr>
<tr>
<td><code>relique:relic_sounds</code></td>
<td>An object containing <code>equip</code> and <code>unequip</code> sound keys.</td>
</tr>
</table>

#### `/give` Command Example
This command gives the player a Diamond that grants +5 Max Health when equipped in the `ring` slot, and plays a specific sound when equipped or unequipped.

```mcfunction
/give @p diamond[minecraft:custom_data={CustomData:{CustomComponents:{"relique:relic_properties":[{attribute:"minecraft:generic.max_health",id:"relique:health_bonus",amount:5.0,operation:"ADD_NUMBER",slots:["ring"]}],"relique:relic_sounds":{equip:"minecraft:item.armor.equip_diamond",unequip:"minecraft:entity.chicken.egg"}}}}] 1
```

**Attribute Operations:**
Relique uses standard Bukkit attribute operations:
* `ADD_NUMBER`: Adds (or subtracts) a flat amount to the base value.
* `ADD_SCALAR`: Adds a scalar of the amount to the base value.
* `MULTIPLY_SCALAR_1`: Multiplies the value by the specified scalar.

---

### For Developers: The ComponentMap API
If you are generating relics dynamically via code, you should not modify raw NBT tags manually. Instead, use AbyssalLib's `ComponentMap` to safely apply the strongly-typed data components to your `ItemStack`.

The `ComponentMap` acts as a bridge, instantly serializing your Java objects into the correct persistent NBT structure on the item.

#### Example: Applying Properties via Code

```Java
public ItemStack createCustomRelic() {
    ItemStack item = new ItemStack(Material.DIAMOND);
    
    // 1. Wrap the Bukkit ItemStack in a ComponentMap
    ComponentMap components = new ComponentMap(item);
    
    // 2. Define our custom attribute modifier
    RelicAttributeModifier healthBonus = new RelicAttributeModifier(
        Key.key("minecraft", "generic.max_health"), // The attribute to modify
        Key.key("relique", "health_bonus"),         // A unique ID for this specific modifier
        5.0,                                     // The amount to add
        Operation.ADD_NUMBER,                    // The mathematical operation
        List.of("ring")                          // Which slots this applies in (use "any" for all)
    );
    
    // 3. Apply the properties component
    components.setData(new RelicProperties(
        new RelicProperties.Properties(List.of(healthBonus))
    ));
    
    // 4. Apply the custom equip sounds
    components.setData(new RelicEquipSound(
        new RelicEquipSound.Sounds(
            Key.key("minecraft:item.armor.equip_diamond"), // Equip sound
            Key.key("minecraft:entity.chicken.egg")        // Unequip sound
        )
    ));
    
    // The underlying ItemStack is now automatically updated with the correct NBT!
    return item;
}
```

<note>
Modifiers define an explicit list of slots they are allowed to function in. If a player attempts to equip a "ring" relic into their "charm" slot, the modifier will safely ignore them until it is placed in the correct slot.
</note>