# Permissions
<link-summary>Reference guide for Relique permission nodes</link-summary>

Relique utilizes a standard permission node system to control access to its features and administrative commands. All permissions fall under the `relique` namespace.

### Permission Nodes

<table>
<tr>
<th>Node</th>
<th>Default</th>
<th>Description</th>
</tr>
<tr>
<td><code>relique.gui</code></td>
<td><code>TRUE</code></td>
<td>Allows the player to open their relic equipment GUI using the base <code>/relique</code> command.</td>
</tr>
<tr>
<td><code>relique.reload</code></td>
<td><code>OP</code></td>
<td>Allows the user to execute the <code>/relique reload</code> command to refresh configurations and assets.</td>
</tr>
<tr>
<td><code>relique.modify_slots</code></td>
<td><code>OP</code></td>
<td>Allows the user to modify player slot limits using the <code>/relique slot &lt;add/remove/set&gt;</code> commands.</td>
</tr>
</table>