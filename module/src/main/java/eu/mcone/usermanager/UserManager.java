package eu.mcone.usermanager;

import eu.mcone.networkmanager.api.ModuleHost;
import eu.mcone.networkmanager.core.api.database.Database;
import eu.mcone.usermanager.api.user.PlayerState;
import eu.mcone.usermanager.api.user.User;
import eu.mcone.usermanager.api.user.UserSettings;
import eu.mcone.usermanager.user.PermissionManager;
import eu.mcone.usermanager.user.MojangUtils;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserManager extends eu.mcone.usermanager.api.UserManager {

    @Getter
    private static UserManager manager;
    private Set<User> userCache;

    @Getter
    private PermissionManager permissionManager;
    @Getter
    private MojangUtils mojangUtils;

    @Override
    public void onLoad() {
        setInstance(this);
        manager = this;
    }

    @Override
    public void onEnable() {
        userCache = new HashSet<>();

        permissionManager = new PermissionManager(ModuleHost.getInstance().getMongoDatabase(Database.SYSTEM));
        mojangUtils = new MojangUtils(ModuleHost.getInstance().getMongoDatabase(Database.SYSTEM));

        for (Document entry : ModuleHost.getInstance().getMongoDatabase(Database.SYSTEM).getCollection("userinfo").find()) {
            userCache.add(
                    new eu.mcone.usermanager.user.User(
                            UUID.fromString(entry.getString("uuid")),
                            entry.getString("name"),
                            UserManager.getManager().getPermissionManager().getGroups(entry.get("groups", new ArrayList<>())),
                            entry.getInteger("coins"),
                            entry.getInteger("emeralds"),
                            PermissionManager.getServerPermissionsFromDocument(entry),
                            entry.getString("teamspeak_uid"),
                            entry.getString("discord_uid"),
                            PlayerState.getPlayerStateById(entry.getInteger("state")),
                            entry.getLong("online_time"),
                            ModuleHost.getInstance().getGson().fromJson(entry.get("player_settings", Document.class).toJson(), UserSettings.class),
                            entry.getString("password"),
                            entry.getString("email")
                    )
            );
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public User getUser(UUID uuid, String name) {
        User u = null;

        for (User user : userCache) {
            if (user.getUuid().equals(uuid)) {
                u = user;
            }
        }

        if (u != null) {
            if (!u.getName().equals(name)) ((eu.mcone.usermanager.user.User) u).updateName(name);
            return u;
        } else {
            u = new eu.mcone.usermanager.user.User(uuid, name);
            userCache.add(u);
            return u;
        }
    }

}
