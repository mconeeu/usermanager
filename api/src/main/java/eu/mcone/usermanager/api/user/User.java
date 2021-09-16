package eu.mcone.usermanager.api.user;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface User {

    UUID getUuid();

    String getName();

    String getTeamspeakUid();

    String getDiscordUid();

    String getMailAddress();

    int getCoins();

    int getEmeralds();

    PlayerState getState();

    Set<Group> getGroups();

    long getOnlinetime();

    Map<String, String> getPermissionMap();

    UserSettings getSettings();

    Group getMainGroup();

    Set<String> getAllPermissions();

    Set<String> getTemplatePermissions(String template);

    boolean hasPermission(String permission);

    boolean hasPermission(String permission, String template);

    void addPermission(String permission, String template);

    void addPermission(String permission);

    void removePermission(String permission, String template);

    void removePermission(String permission);

    void setCoins(int amount);

    void addCoins(int amount);

    void removeCoins(int amount);

    void setEmeralds(int amount);

    void addEmeralds(int amount);

    void removeEmeralds(int amount);

    boolean isTeamspeakIdLinked();

    boolean isDiscordIdLinked();

    void setState(PlayerState state);

    boolean isRegistered();

}
