package eu.mcone.usermanager.user;

import com.mongodb.client.model.UpdateOptions;
import eu.mcone.networkmanager.api.ModuleHost;
import eu.mcone.networkmanager.core.api.database.MongoDatabase;
import eu.mcone.usermanager.UserManager;
import eu.mcone.usermanager.api.event.UserPermissionUpdateEvent;
import eu.mcone.usermanager.api.user.Group;
import eu.mcone.usermanager.api.user.User;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class PermissionManager {

    private MongoDatabase database;

    private Map<Group, Map<String, String>> groups;
    private Map<Group, Set<Group>> parents;

    public PermissionManager(MongoDatabase database) {
        this.database = database;

        this.groups = new HashMap<>();
        this.parents = new HashMap<>();

        this.reload();
    }

    public void reload() {
        for (Document entry : database.getCollection("permission_groups").find()) {
            Group g = Group.getGroupById(entry.getInteger("id"));
            this.groups.put(g, getServerPermissionsFromDocument(entry));

            Set<Group> parents = new HashSet<>();
            for (int id : entry.get("parents", new ArrayList<Integer>())) {
                parents.add(Group.getGroupById(id));
            }
            this.parents.put(g, parents);
        }
    }

    public static Map<String, String> getServerPermissionsFromDocument(Document entry) {
        Map<String, String> permissions = new HashMap<>();
        for (Map.Entry<String, Object> e : entry.get("permissions", new Document()).entrySet()) {
            if (e.getValue() == null || e.getValue() instanceof String) {
                permissions.put(e.getKey().replace('-', '.'), e.getValue() != null ? (String) e.getValue() : null);
            }
        }
        return permissions;
    }

    public Set<Group> getParents(Group group) {
        Set<Group> result = new HashSet<>();
        for (Group parent : this.parents.getOrDefault(group, new HashSet<>())) {
            result.add(parent);
            result.addAll(getParents(parent));
        }
        return result;
    }

    public Set<Group> getChildren(Group group) {
        Set<Group> result = new HashSet<>();
        for (HashMap.Entry<Group, Set<Group>> entry : this.parents.entrySet()) {
            if (entry.getValue().contains(group)) {
                result.add(entry.getKey());
                result.addAll(getChildren(entry.getKey()));
            }
        }
        return result;
    }

    public Map<String, String> getPermissions(UUID uuid, Map<String, String> playerPermissions, Set<Group> groups) {
        if (groups.size() == 0) groups.add(Group.SPIELER);
        Map<String, String> permissions = new HashMap<>(playerPermissions);

        for (Group g : groups) {
            permissions.put("group." + g.getName(), null);
            permissions.putAll(this.groups.getOrDefault(g, Collections.emptyMap()));

            for (Group parent : getParents(g)) {
                permissions.putAll(this.groups.getOrDefault(parent, Collections.emptyMap()));
            }
        }

        return permissions;
    }

    public boolean hasPermission(Set<String> permissions, String permission) {
        if(permissions.contains(permission) || permissions.contains("*") || permission == null) {
            return true;
        } else {
            String[] permissionSplit = permission.replace('.', '-').split("-");
            StringBuilder permConstrutor = new StringBuilder();
            for(int i=0;i<permissionSplit.length-1;i++) {
                permConstrutor.append(permConstrutor.toString().equals("") ? "" : ".").append(permissionSplit[i]);
                if(permissions.contains(permConstrutor+".*")) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getGroupPermissions(Group group) {
        return new HashSet<>(groups.getOrDefault(group, Collections.emptyMap()).keySet());
    }

    public Set<Group> getGroups(List<Integer> groups) {
        Set<Group> result = new HashSet<>();

        for (Integer id : groups) {
            result.add(Group.getGroupById(id));
        }

        return result;
    }

    public List<Integer> getGroupIDs(Set<Group> groups) {
        List<Integer> result = new ArrayList<>();

        for (Group g : groups) {
            result.add(g.getId());
        }

        return result;
    }

    void addUserPermission(User user, String permission, String template) {
        Set<String> permissions = template != null ? user.getUserPermissions(template) : user.getAllUserPermissions();

        if (permissions.contains("-"+permission)) {
            ((eu.mcone.usermanager.user.User) user).userPermissions.remove("-"+permission);

            database.getCollection("permission_players").updateOne(
                    eq("uuid", user.getUuid().toString()),
                    unset("permissions.-"+permission.replace('.', '-')),
                    new UpdateOptions().upsert(true)
            );
        } else if (!permissions.contains(permission)) {
            ((eu.mcone.usermanager.user.User) user).userPermissions.put(permission, template);

            database.getCollection("permission_players").updateOne(
                    eq("uuid", user.getUuid().toString()),
                    set("permissions."+permission.replace('.', '-'), template),
                    new UpdateOptions().upsert(true)
            );
        }

        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserPermissionUpdateEvent());
    }

    void removeUserPermission(User user, String permission, String template) {
        Set<String> permissions = template != null ? user.getUserPermissions(template) : user.getAllUserPermissions();

        if (permissions.contains(permission)) {
            ((eu.mcone.usermanager.user.User) user).userPermissions.remove(permission);

            database.getCollection("permission_players").updateOne(
                    eq("uuid", user.getUuid().toString()),
                    unset("permissions."+permission.replace('.', '-')),
                    new UpdateOptions().upsert(true)
            );
        } else if (!permissions.contains("-"+permission)) {
            ((eu.mcone.usermanager.user.User) user).userPermissions.put("-"+permission, template);

            database.getCollection("permission_players").updateOne(
                    eq("uuid", user.getUuid().toString()),
                    set("permissions.-"+permission.replace('.', '-'), template),
                    new UpdateOptions().upsert(true)
            );
        }
    }

    public void addGroupPermission(Group group, String permission, String template) {
        groups.computeIfAbsent(group, k -> new HashMap<>());
        Map<String, String> permissions = this.groups.get(group);

        if (permissions.containsKey("-"+permission)) {
            permissions.remove("-"+permission);

            database.getCollection("permission_groups").updateOne(
                    eq("id", group.getId()),
                    unset("permissions.-"+permission.replace('.', '-'))
            );
        } else if (!permissions.containsKey(permission)) {
            permissions.put(permission, template);

            database.getCollection("permission_groups").updateOne(
                    eq("id", group.getId()),
                    set("permissions."+permission.replace('.', '-'), template)
            );
        }
    }

    public void removeGroupPermission(Group group, String permission, String template) {
        groups.computeIfAbsent(group, k -> new HashMap<>());
        Map<String, String> permissions = this.groups.get(group);

        if (permissions.containsKey(permission)) {
            permissions.remove(permission);

            database.getCollection("permission_groups").updateOne(
                    eq("id", group.getId()),
                    unset("permissions."+permission.replace('.', '-'))
            );
        } else if (!permissions.containsKey("-"+permission)) {
            permissions.put("-"+permission, template);

            database.getCollection("permission_groups").updateOne(
                    eq("id", group.getId()),
                    set("permissions.-"+permission.replace('.', '-'), template)
            );
        }
    }

    public void addParent(Group target, Group parent) {

    }

    public void removeParent(Group target, Group parent) {

    }

}
