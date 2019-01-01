package eu.mcone.usermanager.api;

import eu.mcone.networkmanager.api.module.NetworkModule;
import eu.mcone.usermanager.api.user.PermissionManager;
import eu.mcone.usermanager.api.user.User;
import lombok.Getter;

import java.util.UUID;

public abstract class UserManager extends NetworkModule {

    @Getter
    private static UserManager instance;

    protected void setInstance(UserManager instance) {
        if (instance == null) {
            System.err.println("UserManager instance cannot be set twice!");
        } else {
            UserManager.instance = instance;
        }
    }

    public abstract PermissionManager getPermissionManager();

    public abstract User getUser(UUID uuid, String name);

}
