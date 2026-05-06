# Using RelicAPI
<link-summary>Guide to interacting with relics and slots programmatically</link-summary>

The `RelicAPI` class provides the primary entry point for developers to interact with the Relique system. It allows you to query equipped items, modify slot limits, and safely equip or unequip relics for any `LivingEntity`.

Because Relique supports custom entities and mobs, all API methods accept a `LivingEntity` rather than strictly a `Player`.

### Retrieving Equipped Relics
When retrieving items through the API, the returned `ItemStack` instances are **safe clones**. Modifying the returned items will not alter the entity's actual equipment. If you need to update an item's data, you must modify the clone and pass it back through `equip()`.

<table>
<tr>
<th>Method</th>
<th>Description</th>
</tr>
<tr>
<td><code>getEquipped(LivingEntity, String slotId)</code></td>
<td>Returns a list of all items currently equipped in the specified slot. Empty slots are represented by <code>ItemStack.empty()</code>.</td>
</tr>
<tr>
<td><code>getEquipped(LivingEntity, String slotId, int index)</code></td>
<td>Returns an <code>Optional&lt;ItemStack&gt;</code> for a specific index within a slot.</td>
</tr>
<tr>
<td><code>getAllEquipped(LivingEntity)</code></td>
<td>Returns a flat list of <code>SlotResult</code> records, detailing every relic equipped across all of the entity's active slots. Empty spaces are omitted.</td>
</tr>
</table>

**Example:**
```Java
// Print all equipped relics to the console
for (RelicAPI.SlotResult result : RelicAPI.getAllEquipped(entity)) {
    System.out.println("Slot: " + result.slotId() + " | Item: " + result.item().getType());
}
```

---

### Managing Equipment
The API provides methods to safely equip and unequip items. Using these methods guarantees that all necessary events (`RelicEquipEvent`, `RelicUnequipEvent`) are fired, attribute modifiers are correctly applied or removed, and equip sounds are played.

<table>
<tr>
<th>Method</th>
<th>Description</th>
</tr>
<tr>
<td><code>equip(LivingEntity, String slotId, int index, ItemStack)</code></td>
<td>Attempts to equip an item into the specified slot and index. Returns <code>true</code> if successful. Fails if the item fails validation, the index exceeds the slot limit, or the current item cannot be unequipped (e.g., Curse of Binding).</td>
</tr>
<tr>
<td><code>unequip(LivingEntity, String slotId, int index)</code></td>
<td>Removes and returns the item at the specified slot index. Returns <code>ItemStack.empty()</code> if the slot is empty or the item cannot be removed.</td>
</tr>
<tr>
<td><code>canUnequip(LivingEntity, String slotId, int index)</code></td>
<td>Checks if the item at the index can be unequipped. Evaluates the Curse of Binding enchantment and Creative mode bypasses.</td>
</tr>
</table>

<note>
Passing <code>ItemStack.empty()</code> or <code>null</code> into <code>equip()</code> will act exactly like <code>unequip()</code>, clearing the slot and removing its modifiers.
</note>

---

### Modifying Slot Limits
Slot capacities are backed by AbyssalLib entity attributes. This means limits are persistent, and can be modified dynamically per-entity.

<table>
<tr>
<th>Method</th>
<th>Description</th>
</tr>
<tr>
<td><code>getSlotLimit(LivingEntity, String slotId)</code></td>
<td>Returns the maximum number of items the entity can hold in the specified slot.</td>
</tr>
<tr>
<td><code>addSlotLimit(LivingEntity, String slotId, int amount)</code></td>
<td>Increases the capacity of the specified slot.</td>
</tr>
<tr>
<td><code>removeSlotLimit(LivingEntity, String slotId, int amount)</code></td>
<td>Decreases the capacity of the specified slot.</td>
</tr>
<tr>
<td><code>setSlotLimit(LivingEntity, String slotId, int amount)</code></td>
<td>Explicitly sets the capacity of the specified slot.</td>
</tr>
</table>

<tip>
If you reduce a slot's limit below the number of currently equipped relics (e.g., dropping a ring slot limit from 2 down to 1 while the player has 2 rings equipped), the API will automatically unequip the overflow items and drop them safely at the entity's location.
</tip>

---

### Utilities and Validation
These methods allow you to query the global configuration of the Relique ecosystem and validate items against slot rules.

<table>
<tr>
<th>Method</th>
<th>Description</th>
</tr>
<tr>
<td><code>isValid(LivingEntity, String slotId, ItemStack)</code></td>
<td>Runs the item through all registered <code>RelicValidator</code> logic for the specified slot. Returns <code>true</code> if the item is permitted to be equipped.</td>
</tr>
<tr>
<td><code>getAvailableSlots()</code></td>
<td>Returns a sorted list of all registered slot IDs across all loaded namespaces.</td>
</tr>
<tr>
<td><code>getSlotDefinition(String slotId)</code></td>
<td>Returns the immutable <code>RelicSlot</code> configuration record (containing the drop rule, order, base size, and icon) for a specific ID.</td>
</tr>
</table>