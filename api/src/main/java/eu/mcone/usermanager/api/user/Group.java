package eu.mcone.usermanager.api.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Group {

    int getId();

    int getTsId();

    String getName();

    String getPrefix();

    String getColor();

    Map<String, List<String>> getGroupPermissions();

    Set<String> getAllPermissions();

    Set<String> getTemplatePermissions(String template);

    Set<Group> getParents();

    Set<Group> getRecursiveParents();

    boolean hasParent(Group parent);

    void addPermission(String permission);

    void addPermission(String permission, String template);

    void removePermission(String permission);

    void removePermission(String permission, String template);

    void addParent(Group parent);

    void removeParent(Group parent);

}
