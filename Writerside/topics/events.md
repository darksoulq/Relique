# Events
<link-summary>Reference guide for built-in Relique events</link-summary>

Events are fired during the lifecycle of an equipped relic. Below is a list of all custom events included in Relique.

### RelicPreEquipEvent
**Cancellable:** `true`

Fired before an item is equipped into a relic slot. If cancelled, the item will not be equipped.

<table>
<tr>
<th>Method</th>
<th>Information</th>
</tr>
<tr>
<td><code>getEntity()</code></td>
<td>The <code>LivingEntity</code> attempting to equip the relic.</td>
</tr>
<tr>
<td><code>getSlotId()</code></td>
<td>The ID of the target slot.</td>
</tr>
<tr>
<td><code>getIndex()</code></td>
<td>The target index within the slot.</td>
</tr>
<tr>
<td><code>getItem()</code></td>
<td>The <code>ItemStack</code> being equipped.</td>
</tr>
</table>

---

### RelicEquipEvent
**Cancellable:** `false`

Fired after an item is successfully equipped and its attribute modifiers and sounds have been applied.

<table>
<tr>
<th>Method</th>
<th>Information</th>
</tr>
<tr>
<td><code>getEntity()</code></td>
<td>The <code>LivingEntity</code> that equipped the relic.</td>
</tr>
<tr>
<td><code>getSlotId()</code></td>
<td>The ID of the slot.</td>
</tr>
<tr>
<td><code>getIndex()</code></td>
<td>The index within the slot.</td>
</tr>
<tr>
<td><code>getItem()</code></td>
<td>The <code>ItemStack</code> that was equipped.</td>
</tr>
</table>

---

### RelicUnequipEvent
**Cancellable:** `false`

Fired after an item is successfully unequipped and its attribute modifiers have been removed.

<table>
<tr>
<th>Method</th>
<th>Information</th>
</tr>
<tr>
<td><code>getEntity()</code></td>
<td>The <code>LivingEntity</code> that unequipped the relic.</td>
</tr>
<tr>
<td><code>getSlotId()</code></td>
<td>The ID of the slot.</td>
</tr>
<tr>
<td><code>getIndex()</code></td>
<td>The index within the slot.</td>
</tr>
<tr>
<td><code>getItem()</code></td>
<td>The <code>ItemStack</code> that was unequipped.</td>
</tr>
</table>

---

### RelicDropEvent
**Cancellable:** `false`

Fired when a relic is forcibly dropped from an entity (e.g., upon death, or if a slot limit is dynamically reduced).

<table>
<tr>
<th>Method</th>
<th>Information</th>
</tr>
<tr>
<td><code>getEntity()</code></td>
<td>The <code>LivingEntity</code> dropping the relic.</td>
</tr>
<tr>
<td><code>getSlotId()</code></td>
<td>The ID of the slot.</td>
</tr>
<tr>
<td><code>getIndex()</code></td>
<td>The index within the slot.</td>
</tr>
<tr>
<td><code>getItem()</code></td>
<td>The <code>ItemStack</code> being dropped.</td>
</tr>
</table>

---

### RelicTickEvent
**Cancellable:** `false`

Fired every server tick for every equipped relic on valid entities.

<table>
<tr>
<th>Method</th>
<th>Information</th>
</tr>
<tr>
<td><code>getEntity()</code></td>
<td>The <code>LivingEntity</code> currently holding the relic.</td>
</tr>
<tr>
<td><code>getSlotId()</code></td>
<td>The ID of the slot.</td>
</tr>
<tr>
<td><code>getIndex()</code></td>
<td>The index within the slot.</td>
</tr>
<tr>
<td><code>getItem()</code></td>
<td>The <code>ItemStack</code> currently equipped.</td>
</tr>
</table>