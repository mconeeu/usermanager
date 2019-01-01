package eu.mcone.usermanager.user;

import com.mongodb.client.MongoCollection;
import eu.mcone.networkmanager.api.ModuleHost;
import eu.mcone.networkmanager.core.api.database.Database;
import eu.mcone.usermanager.UserManager;
import eu.mcone.usermanager.api.user.Group;
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
    private String name, teamspeakUid, discordUid, passwordHash, mailAddress;
    @Getter
    private int coins, emeralds;
    @Getter
    private PlayerState state;
    @Getter
    private Set<Group> groups;
    @Getter
    private long onlinetime;
    @Getter
    Map<String, String> permissionMap, userPermissions;
    @Getter
    private UserSettings settings;
    @Getter @Setter
    private boolean firstJoin;

    public User(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.groups = new HashSet<>(Collections.singletonList(Group.SPIELER));
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

        this.userPermissions = new HashMap<>();
        this.permissionMap = UserManager.getManager().getPermissionManager().getPermissions(uuid, userPermissions, groups);
    }

    public User(UUID uuid, String name, Set<Group> groups, int coins, int emeralds, Map<String, String> userPermissions, String teamspeakUid, String discordUid, PlayerState state, long onlinetime, UserSettings settings, String passwordHash, String mailAddress) {
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
        this.passwordHash = passwordHash;
        this.mailAddress = mailAddress;

        this.userPermissions = userPermissions;
        this.permissionMap = UserManager.getManager().getPermissionManager().getPermissions(uuid, userPermissions, groups);
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
        for (Group g : Group.values()) {
            if (groups.contains(g)) return g;
        }

        log.severe("User " + name + " has no groups");
        return null;
    }

    public Set<String> getPermissions(String template) {
        Set<String> permissions = new HashSet<>();
        for (Map.Entry<String, String> e : this.permissionMap.entrySet()) {
            if (e.getValue() == null || e.getValue().equalsIgnoreCase(template)) {
                permissions.add(e.getKey());
            }
        }
        return permissions;
    }

    @Override
    public Set<String> getAllPermissions() {
        return new HashSet<>(permissionMap.keySet());
    }

    @Override
    public Set<String> getUserPermissions(String template) {
        Set<String> permissions = new HashSet<>();
        for (Map.Entry<String, String> e : this.userPermissions.entrySet()) {
            if (e.getValue() == null || e.getValue().equalsIgnoreCase(template)) {
                permissions.add(e.getKey());
            }
        }
        return permissions;
    }

    @Override
    public Set<String> getAllUserPermissions() {
        return new HashSet<>(userPermissions.keySet());
    }

    @Override
    public boolean hasPermission(String permission) {
        return UserManager.getManager().getPermissionManager().hasPermission(getAllPermissions(), permission);
    }

    @Override
    public boolean hasPermission(String permission, String template) {
        return UserManager.getManager().getPermissionManager().hasPermission(getPermissions(template), permission);
    }

    @Override
    public void addPermission(String permission) {
        UserManager.getManager().getPermissionManager().addUserPermission(this, permission, null);
    }

    @Override
    public void addPermission(String permission, String template) {
        UserManager.getManager().getPermissionManager().addUserPermission(this, permission, template);
    }

    @Override
    public void removePermission(String permission) {
        UserManager.getManager().getPermissionManager().removeUserPermission(this, permission, null);
    }

    @Override
    public void removePermission(String permission, String template) {
        UserManager.getManager().getPermissionManager().removeUserPermission(this, permission, template);
    }

    @Override
    public void setCoins(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot set negative coin amount!");
        } else {
            this.coins = amount;
            USERINFO.updateOne(eq("uuid", uuid.toString()), set("coins", amount));
        }
    }

    @Override
    public void addCoins(int amount) {
        this.coins += amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("coins", amount));
    }

    @Override
    public void removeCoins(int amount) {
        if (coins - amount < 0) {
            amount = coins;
            System.err.println("§7Tried to remove more coins than Player §f" + name + "§7 has! (" + coins + "-" + amount + ")");
        }

        this.coins -= amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("coins", -amount));
    }

    @Override
    public void setEmeralds(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot set negative emerald amount!");
        } else {
            this.coins = amount;
            USERINFO.updateOne(eq("uuid", uuid.toString()), set("emeralds", amount));
        }
    }

    @Override
    public void addEmeralds(int amount) {
        this.coins += amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("emeralds", amount));
    }

    @Override
    public void removeEmeralds(int amount) {
        if (coins - amount < 0) {
            amount = coins;
            System.err.println("§7Tried to remove more emeralds than Player §f" + name + "§7 has! (" + coins + "-" + amount + ")");
        }

        this.coins -= amount;
        USERINFO.updateOne(eq("uuid", uuid.toString()), inc("emeralds", -amount));
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
        return passwordHash != null;
    }

}
