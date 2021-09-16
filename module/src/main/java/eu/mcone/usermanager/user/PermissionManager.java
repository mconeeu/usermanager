package eu.mcone.usermanager.user;

import com.mongodb.client.MongoDatabase;
import eu.mcone.usermanager.api.user.Group;
import eu.mcone.usermanager.api.user.GroupID;
import eu.mcone.usermanager.api.user.User;
import lombok.Getter;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class PermissionManager implements eu.mcone.usermanager.api.user.PermissionManager {

    private MongoDatabase database;
    @Getter
    private Set<Group> groups;

    public PermissionManager(MongoDatabase database) {
        this.database = database;
        this.groups = new HashSet<>();

        this.reload();
    }

    public void reload() {
        for (Document entry : database.getCollection("groups").find()) {
            groups.add(new UserGroup(
                    entry.getInteger("id"),
                    entry.getString("name"),
                    entry.getString("prefix"),
                    entry.getString("color"),
                    entry.get("permissions", Document.class),
                    entry.get("parents", new ArrayList<>()),
                    entry.getInteger("tsId")
            ));
        }
    }

    public Map<String, List<String>> getPermissions(Map<String, List<String>> playerPermissions, Set<Group> groups) {
        if (groups.size() == 0) groups.add(getGroup(GroupID.SPIELER));
        Map<String, List<String>> permissions = new HashMap<>(playerPermissions);

        for (Group g : groups) {
            addPermissions2Template(permissions, ":all", Collections.singletonList("group."+g.getName().toLowerCase()));
            addTemplatePermissions(permissions, g.getGroupPermissions());
            addParentGroupPermissions(permissions, g);
        }

        return permissions;
    }

    private void addParentGroupPermissions(Map<String, List<String>> permissions, Group group) {
        for (Group parent : group.getParents()) {
            Map<String, List<String>> parentPermissions = parent.getGroupPermissions();
            addPermissions2Template(parentPermissions, ":all", Collections.singletonList("group."+parent.getName().toLowerCase()));
            
            addTemplatePermissions(permissions, parentPermissions);
            addParentGroupPermissions(permissions, parent);
        }
    }

    private void addTemplatePermissions(Map<String, List<String>> permissions, Map<String, List<String>> toAdd) {
        for (Map.Entry<String, List<String>> toAddEntry : toAdd.entrySet()) {
            addPermissions2Template(permissions, toAddEntry.getKey(), toAddEntry.getValue());
        }
    }

    private void addPermissions2Template(Map<String, List<String>> permissions, String template, List<String> toAdd) {
        if (permissions.containsKey(template)) {
            permissions.get(template).addAll(toAdd);
        } else {
            permissions.put(template, new ArrayList<>(toAdd));
        }
    }

    public Group getGroup(int id) {
        for (Group g : groups) {
            if (g.getId() == id) {
                return g;
            }
        }

        return null;
    }

    public Set<Group> getGroups(List<Integer> groupIds) {
        Set<Group> result = new HashSet<>();

        for (Group g : groups) {
            if (groupIds.contains(g.getId())) {
                result.add(g);
            }
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

    void addDatabaseUserPermission(User u, String permission, String template) {
        database.getCollection("userinfo").updateOne(
                eq("uuid", u.getUuid().toString()),
                set("permissions." + (template != null ? template : ":all"), permission)
        );
    }

    void removeDatabaseUserPermission(User u, String permission, String template) {
        database.getCollection("userinfo").updateOne(
                eq("uuid", u.getUuid().toString()),
                pull("permissions." + (template != null ? template : ":all"), permission)
        );
    }

    void addDatabaseGroupPermission(Group g, String permission, String template) {
        database.getCollection("groups").updateOne(
                eq("id", g.getId()),
                set("permissions." + (template != null ? template : ":all"), permission)
        );
    }

    void removeDatabaseGroupPermission(Group g, String permission, String template) {
        database.getCollection("groups").updateOne(
                eq("id", g.getId()),
                pull("permissions." + (template != null ? template : ":all"), permission)
        );
    }

    void addDatabaseParent(Group target, Group parent) {
        database.getCollection("groups").updateOne(
                eq("id", target.getId()),
                push("parents", parent.getId())
        );
    }

    void removeDatabaseParent(Group target, Group parent) {
        database.getCollection("groups").updateOne(
                eq("id", target.getId()),
                pull("parents", parent.getId())
        );
    }

}
