# Commands
<link-summary>Reference guide for built-in Relique commands</link-summary>

Relique provides a set of commands for opening the GUI, reloading configurations, and modifying player slot capacities. The base command is `/relique`.

### Command List

<table>
<tr>
<th>Command</th>
<th>Permission</th>
<th>Description</th>
</tr>
<tr>
<td><code>/relique</code></td>
<td><code>relique.gui</code></td>
<td>Opens the relic equipment GUI for the executing player.</td>
</tr>
<tr>
<td><code>/relique reload</code></td>
<td><code>relique.reload</code></td>
<td>Reloads all slot configurations, entity mappings, icons, and language files, then rebuilds the resource pack.</td>
</tr>
<tr>
<td><code>/relique slot add &lt;target&gt; &lt;slot&gt; &lt;amount&gt;</code></td>
<td><code>relique.modify_slots</code></td>
<td>Increases the maximum capacity of the specified slot for the target player.</td>
</tr>
<tr>
<td><code>/relique slot remove &lt;target&gt; &lt;slot&gt; &lt;amount&gt;</code></td>
<td><code>relique.modify_slots</code></td>
<td>Decreases the maximum capacity of the specified slot for the target player. Overflowing items are automatically unequipped.</td>
</tr>
<tr>
<td><code>/relique slot set &lt;target&gt; &lt;slot&gt; &lt;amount&gt;</code></td>
<td><code>relique.modify_slots</code></td>
<td>Sets the exact capacity of the specified slot for the target player. Overflowing items are automatically unequipped.</td>
</tr>
</table>