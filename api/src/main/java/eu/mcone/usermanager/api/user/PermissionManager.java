package eu.mcone.usermanager.api.user;

public interface PermissionManager {

    void addGroupPermission(Group group, String permission, String template);

    void removeGroupPermission(Group group, String permission, String template);

    void addParent(Group target, Group parent);

    void removeParent(Group target, Group parent);

}
