package com.github.darksoulq.relique.core;

import com.github.darksoulq.abyssallib.server.permission.PermissionNamespace;
import com.github.darksoulq.abyssallib.server.permission.PermissionNode;
import com.github.darksoulq.relique.Relique;
import org.bukkit.permissions.PermissionDefault;

public class PluginPermissions {
    public static final PermissionNamespace NAMESPACE = PermissionNamespace.create(Relique.PLUGIN_ID);
    public static final PermissionNode OPEN_GUI = NAMESPACE.register("gui", id -> new PermissionNode(id).defaultValue(PermissionDefault.TRUE));
    public static final PermissionNode RELOAD = NAMESPACE.register("reload", id -> new PermissionNode(id).defaultValue(PermissionDefault.OP));
    public static final PermissionNode MODIFY_SLOTS = NAMESPACE.register("modify_slots", id -> new PermissionNode(id).defaultValue(PermissionDefault.OP));
}