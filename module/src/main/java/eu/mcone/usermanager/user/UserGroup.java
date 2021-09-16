package eu.mcone.usermanager.user;

import eu.mcone.usermanager.UserManager;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class UserGroup implements eu.mcone.usermanager.api.user.Group {

    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private String prefix;
    @Getter
    private String color;
    @Getter
    private Map<String, List<String>> groupPermissions;
    @Getter
    private Set<eu.mcone.usermanager.api.user.Group> parents;
    @Getter
    private int tsId;

    UserGroup(int id, String name, String prefix, String color, Map<String, Object> permissions, List<Integer> parents, int tsId) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.color = color;
        this.groupPermissions = new HashMap<>();
        for (Map.Entry<String, Object> e : permissions.entrySet()) {
            groupPermissions.put(e.getKey(), (ArrayList<String>) e.getValue());
        }
        this.parents = new HashSet<>();
        for (int pId : parents) {
            this.parents.add(UserManager.getManager().getPermissionManager().getGroup(pId));
        }
        this.tsId = tsId;
    }

    @Override
    public Set<eu.mcone.usermanager.api.user.Group> getRecursiveParents() {
        return new HashSet<>(addParentsRecursive(new HashSet<>(), this));
    }

    @Override
    public boolean hasParent(eu.mcone.usermanager.api.user.Group parent) {
        return parents.contains(parent);
    }

    public Set<String> getAllPermissions() {
        return new HashSet<>(groupPermissions.getOrDefault(":all", Collections.emptyList()));
    }

    public Set<String> getTemplatePermissions(String template) {
        Set<String> permissions = new HashSet<>();

        for (Map.Entry<String, List<String>> e : this.groupPermissions.entrySet()) {
            if (e.getKey().equalsIgnoreCase(template) || e.getKey().equals(":all")) {
                permissions.addAll(e.getValue());
            }
        }

        return permissions;
    }

    public void addPermission(String permission) {
        addPermission(permission, ":all");
    }

    public void addPermission(String permission, String template) {
        Set<String> permissions = template != null && !template.equalsIgnoreCase(":all") ? getTemplatePermissions(template) : getAllPermissions();

        if (permissions.contains("-"+permission)) {
            this.groupPermissions.get(template).remove("-"+permission);
            UserManager.getManager().getPermissionManager().removeDatabaseGroupPermission(this, "-"+permission, template);
        } else if (!permissions.contains(permission)) {
            this.groupPermissions.get(template).add(permission);
            UserManager.getManager().getPermissionManager().addDatabaseGroupPermission(this, permission, template);
        }
    }

    public void removePermission(String permission) {
        removePermission(permission, ":all");
    }

    public void removePermission(String permission, String template) {
        Set<String> permissions = template != null && !template.equalsIgnoreCase(":all") ? getTemplatePermissions(template) : getAllPermissions();

        if (permissions.contains(permission)) {
            this.groupPermissions.get(template).remove(permission);
            UserManager.getManager().getPermissionManager().removeDatabaseGroupPermission(this, permission, template);
        } else if (!permissions.contains("-"+permission)) {
            this.groupPermissions.get(template).add("-"+permission);
            UserManager.getManager().getPermissionManager().addDatabaseGroupPermission(this, "-"+permission, template);
        }
    }

    @Override
    public void addParent(eu.mcone.usermanager.api.user.Group parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
            UserManager.getManager().getPermissionManager().addDatabaseParent(this, parent);
        }
    }

    @Override
    public void removeParent(eu.mcone.usermanager.api.user.Group parent) {
        if (parents.contains(parent)) {
            parents.remove(parent);
            UserManager.getManager().getPermissionManager().removeDatabaseParent(this, parent);
        }
    }

    private Set<UserGroup> addParentsRecursive(Set<UserGroup> parents, UserGroup g) {
        for (eu.mcone.usermanager.api.user.Group parent : g.parents) {
            parents.add((UserGroup) parent);
            parents.addAll(addParentsRecursive(parents, g));
        }
        return parents;
    }

}
