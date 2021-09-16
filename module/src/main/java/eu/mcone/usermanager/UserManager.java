package eu.mcone.usermanager;

import group.onegaming.networkmanager.core.api.database.Database;
import group.onegaming.networkmanager.host.api.ModuleHost;
import eu.mcone.usermanager.api.messaging.URIs;
import eu.mcone.usermanager.api.packet.GroupUpdateRequestPacket;
import eu.mcone.usermanager.api.packet.UserInfoRequestPacket;
import eu.mcone.usermanager.api.packet.UserInfoResponsePacket;
import eu.mcone.usermanager.api.packet.UserUpdateRequestPacket;
import eu.mcone.usermanager.api.user.PlayerState;
import eu.mcone.usermanager.api.user.User;
import eu.mcone.usermanager.api.user.UserSettings;
import eu.mcone.usermanager.listener.GroupUpdateRequestListener;
import eu.mcone.usermanager.listener.UserRequestInfoListener;
import eu.mcone.usermanager.listener.UserUpdateRequestListener;
import eu.mcone.usermanager.user.MojangUtils;
import eu.mcone.usermanager.user.PermissionManager;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserManager extends eu.mcone.usermanager.api.UserManager {

    @Getter
    private static UserManager manager;
    @Getter
    private Set<User> userCache;

    @Getter
    private PermissionManager permissionManager;
    @Getter
    private MojangUtils mojangUtils;

    @Override
    public void onLoad() {
        setInstance(this);
        manager = this;

        registerPacket(UserInfoResponsePacket.class);
        registerPacket(UserInfoRequestPacket.class);
        registerPacket(UserUpdateRequestPacket.class);
        registerPacket(GroupUpdateRequestPacket.class);

        registerClientMessageListener(URIs.USER_UPDATE, new UserUpdateRequestListener());
        registerClientMessageListener(URIs.USER_REQUEST_INFO, new UserRequestInfoListener());
        registerClientMessageListener(URIs.GROUP_UPDATE, new GroupUpdateRequestListener());
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
                            entry.get("permissions", Document.class),
                            entry.getString("teamspeak_uid"),
                            entry.getString("discord_uid"),
                            PlayerState.getPlayerStateById(entry.getInteger("state")),
                            entry.getLong("online_time"),
                            ModuleHost.getInstance().getGson().fromJson(entry.get("player_settings", Document.class).toJson(), UserSettings.class),
                            entry.getString("email")
                    )
            );
        }
    }

    @Override
    public void onDisable() {}

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

    @Override
    public User getOfflineUser(UUID uuid) {
        for (User u : userCache) {
            if (u.getUuid().equals(uuid)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User getOfflineUser(String name) {
        for (User u : userCache) {
            if (u.getName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return null;
    }
}
