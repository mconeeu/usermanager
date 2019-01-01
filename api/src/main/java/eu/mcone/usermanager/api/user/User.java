package eu.mcone.usermanager.api.user;

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

    Set<String> getPermissions();

    UserSettings getSettings();

    Group getMainGroup();

    Set<String> getPermissions(String template);

    Set<String> getAllPermissions();

    Set<String> getUserPermissions(String template);

    Set<String> getAllUserPermissions();

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
