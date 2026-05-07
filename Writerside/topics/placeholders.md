# Placeholders
<link-summary>Reference guide for built-in Relique placeholders</link-summary>

Relique registers several placeholders into AbyssalLib's central placeholder registry. These can be used to display a player's equipped items or their slot capacities in GUIs, scoreboards, or chat messages.

### Equipped
**ID:** `relique:equipped`

Returns the display name of the item(s) equipped in the specified slot. If no index is provided, it returns a comma-separated list of all items currently equipped in that slot. The returned text automatically includes hover events showing the item's full tooltip. Returns empty if no items are equipped.

<table>
<tr>
<th>Parameter</th>
<th>Information</th>
</tr>
<tr>
<td><code>&lt;slot&gt;</code></td>
<td>The ID of the slot to check (e.g., <code>ring</code>, <code>charm</code>).</td>
</tr>
<tr>
<td><code>[index]</code></td>
<td>The specific index within the slot to check (starts at 0). Optional.</td>
</tr>
</table>

**Examples:**
* `relique:equipped:ring` -> Returns all equipped rings.
* `relique:equipped:ring:0` -> Returns only the first equipped ring.

---

### Limit
**ID:** `relique:limit`

Returns the maximum number of items the player is allowed to equip in the specified slot based on their current entity attributes and limits.

<table>
<tr>
<th>Parameter</th>
<th>Information</th>
</tr>
<tr>
<td><code>&lt;slot&gt;</code></td>
<td>The ID of the slot to check the capacity for.</td>
</tr>
</table>

**Example:**
* `relique:limit:ring` -> Returns `2` (by default).