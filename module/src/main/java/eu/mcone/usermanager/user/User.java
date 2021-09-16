package eu.mcone.usermanager.user;

import com.mongodb.client.MongoCollection;
import group.onegaming.networkmanager.core.api.database.Database;
import group.onegaming.networkmanager.host.api.ModuleHost;
import eu.mcone.usermanager.UserManager;
import eu.mcone.usermanager.api.event.UserUpdateEvent;
import eu.mcone.usermanager.api.user.Group;
import eu.mcone.usermanager.api.user.GroupID;
import eu.mcone.usermanager.api.user.PlayerState;
import eu.mcone.usermanager.api.user.UserSettings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

@Log
public class User implements eu.mcone.usermanager.api.user.User {

    private static final MongoCollection<Document> USERINFO = ModuleHost.getInstance().getMongoDatabase(Database.SYSTEM).getCollection("userinfo");

    @Getter
    private UUID uuid;
    @Getter
    private String name, teamspeakUid, discordUid, mailAddress;
    @Getter
    private int coins, emeralds;
    @Getter
    private PlayerState state;
    @Getter
    private Set<Group> groups;
    @Getter
    private long onlinetime;
    @Getter
    private Map<String, List<String>> permissionMap;
    @Getter
    private UserSettings settings;
    @Getter @Setter
    private boolean firstJoin;

    public User(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.groups = new HashSet<>(Collections.singletonList(UserManager.getManager().getPermissionManager().getGroup(GroupID.SPIELER)));
        this.coins = 20;
        this.emeralds = 0;
        this.state = PlayerState.OFFLINE;
        this.onlinetime = 0;
        this.settings = new UserSettings();

        USERINFO.insertOne(
                new Document("uuid", uuid.toString())
                        .append("name", name)
                        .append("groups", UserManager.getManager().getPermissionManager().getGroupIDs(groups))
                        .append("coins", coins)
                        .append("emeralds", emeralds)
                        .append("timestamp", System.currentTimeMillis() / 1000)
                        .append("player_settings", settings)
                        .append("state", state.getId())
                        .append("online_time", onlinetime)
        );

        this.permissionMap = new HashMap<>();
        updateGroupPermissions();
    }

    public User(UUID uuid, String name, Set<Group> groups, int coins, int emeralds, Map<String, Object> permissions, String teamspeakUid, String discordUid, PlayerState state, long onlinetime, UserSettings settings, String mailAddress) {
        this.uuid = uuid;
        this.name = name;
        this.groups = groups;
        this.coins = coins;
        this.emeralds = emeralds;
        this.teamspeakUid = teamspeakUid;
        this.discordUid = discordUid;
        this.state = state;
        this.onlinetime = onlinetime;
        this.settings = settings;
        this.mailAddress = mailAddress;

        this.permissionMap = new HashMap<>();
        for (Map.Entry<String, Object> e : permissions.entrySet()) {
            permissionMap.put(e.getKey(), (ArrayList<String>) e.getValue());
        }
        updateGroupPermissions();
    }

    public void updateName(String newName) {
        name = newName;
        USERINFO.updateOne(
                eq("uuid", uuid.toString()),
                set("name", name)
        );
    }

    @Override
    public Group getMainGroup() {
        Map<Integer, Group> groups = new HashMap<>();
        for (Group g : this.groups) {
            groups.put(g.getId(), g);
        }

        return Collections.min(groups.entrySet(), Map.Entry.comparingByKey()).getValue();
    }

    @Override
    public Set<String> getAllPermissions() {
        return new HashSet<>(permissionMap.getOrDefault(":all", Collections.emptyList()));
    }

    @Override
    public Set<String> getTemplatePermissions(String template) {
        Set<String> permissions = new HashSet<>();

        for (Map.Entry<String, List<String>> e : this.permissionMap.entrySet()) {
            if (e.getKey().equalsIgnoreCase(template) || e.getKey().equals(":all")) {
                permissions.addAll(e.getValue());
            }
        }

        return permissions;
    }

    @Override
    public boolean hasPermission(String permission) {
        return hasPermission(getAllPermissions(), permission);
    }

    @Override
    public boolean hasPermission(String permission, String template) {
        return template != null ? hasPermission(getTemplatePermissions(template), permission) : hasPermission(permission);
    }

    private boolean hasPermission(Set<String> permissions, String permission) {
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

    @Override
    public void addPermission(String permission) {
        addPermission(permission, ":all");
    }

    @Override
    public void addPermission(String permission, String template) {
        Set<String> permissions = template != null && !template.equalsIgnoreCase(":all") ? getTemplatePermissions(template) : getAllPermissions();

        if (permissions.contains("-"+permission)) {
            permissionMap.get(template).remove("-"+permission);
            UserManager.getManager().getPermissionManager().removeDatabaseUserPermission(this, "-"+permission, template);
        } else if (!permissions.contains(permission)) {
            permissionMap.get(template).add(permission);
            UserManager.getManager().getPermissionManager().addDatabaseUserPermission(this, permission, template);
        }

        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    @Override
    public void removePermission(String permission) {
        removePermission(permission, ":all");
    }

    @Override
    public void removePermission(String permission, String template) {
        Set<String> permissions = template != null && !template.equalsIgnoreCase(":all") ? getTemplatePermissions(template) : getAllPermissions();

        if (permissions.contains(permission)) {
            permissionMap.get(template).remove(permission);
            UserManager.getManager().getPermissionManager().removeDatabaseUserPermission(this, permission, template);
        } else if (!permissions.contains("-"+permission)) {
            permissionMap.get(template).add("-"+permission);
            UserManager.getManager().getPermissionManager().addDatabaseUserPermission(this, "-"+permission, template);
        }

        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    public void updateUserFromDatabase() {
        Document entry = USERINFO.find(eq("uuid", uuid)).first();

        this.groups = UserManager.getManager().getPermissionManager().getGroups(entry.get("groups", new ArrayList<>()));
        this.coins = entry.getInteger("coins");
        this.emeralds = entry.getInteger("emeralds");
        this.teamspeakUid = entry.getString("teamspeak_uid");
        this.discordUid = entry.getString("discord_uid");
        this.state = PlayerState.getPlayerStateById(entry.getInteger("state"));
        this.onlinetime = entry.getLong("online_time");
        this.settings = ModuleHost.getInstance().getGson().fromJson(entry.get("player_settings", Document.class).toJson(), UserSettings.class);

        this.permissionMap = new HashMap<>();
        for (Map.Entry<String, Object> e : entry.get("permissions", Document.class).entrySet()) {
            permissionMap.put(e.getKey(), (ArrayList<String>) e.getValue());
        }

        updateGroupPermissions();
    }

    public void updateGroupPermissions() {
        this.permissionMap = UserManager.getManager().getPermissionManager().getPermissions(permissionMap, groups);
    }

    @Override
    public void setCoins(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot set negative coin amount!");
        } else {
            this.coins = amount;
            USERINFO.updateOne(eq("uuid", uuid.toString()), set("coins", amount));
        }

        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    @Override
    public void addCoins(int amount) {
        this.coins += amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("coins", amount));
        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    @Override
    public void removeCoins(int amount) {
        if (coins - amount < 0) {
            amount = coins;
            System.err.println("§7Tried to remove more coins than Player §f" + name + "§7 has! (" + coins + "-" + amount + ")");
        }

        this.coins -= amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("coins", -amount));
        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    @Override
    public void setEmeralds(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot set negative emerald amount!");
        } else {
            this.coins = amount;
            USERINFO.updateOne(eq("uuid", uuid.toString()), set("emeralds", amount));
        }

        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    @Override
    public void addEmeralds(int amount) {
        this.coins += amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("emeralds", amount));
        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    @Override
    public void removeEmeralds(int amount) {
        if (coins - amount < 0) {
            amount = coins;
            System.err.println("§7Tried to remove more emeralds than Player §f" + name + "§7 has! (" + coins + "-" + amount + ")");
        }

        this.coins -= amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("emeralds", -amount));
        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(this));
    }

    @Override
    public boolean isTeamspeakIdLinked() {
        return teamspeakUid != null;
    }

    @Override
    public boolean isDiscordIdLinked() {
        return discordUid != null;
    }

    @Override
    public void setState(PlayerState state) {
        this.state = state;
        USERINFO.updateOne(eq("uuid", uuid.toString()), combine(set("state", state.getId())));
    }

    @Override
    public boolean isRegistered() {
        return mailAddress != null;
    }

}
