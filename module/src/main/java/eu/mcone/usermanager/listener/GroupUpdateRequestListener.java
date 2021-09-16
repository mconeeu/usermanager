package eu.mcone.usermanager.listener;

import group.onegaming.networkmanager.api.messaging.request.CustomClientMessageRequestListener;
import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;
import group.onegaming.networkmanager.host.api.ModuleHost;
import eu.mcone.usermanager.UserManager;
import eu.mcone.usermanager.api.event.GroupUpdateEvent;
import eu.mcone.usermanager.api.packet.GroupUpdateRequestPacket;
import eu.mcone.usermanager.api.user.Group;
import eu.mcone.usermanager.api.user.User;

import java.util.HashSet;
import java.util.Set;

public class GroupUpdateRequestListener extends CustomClientMessageRequestListener<GroupUpdateRequestPacket> {

    @Override
    protected ClientMessageResponsePacket onCustomClientRequest(GroupUpdateRequestPacket packet) {
        Group g = packet.getGroup();
        UserManager.getManager().getPermissionManager().reload();

        Set<eu.mcone.usermanager.api.user.Group> relevantGroups = new HashSet<>();
        relevantGroups.add(g);
        relevantGroups.addAll(g.getRecursiveParents());

        Set<User> relevantUsers = new HashSet<>();
        for (eu.mcone.usermanager.api.user.User u : UserManager.getManager().getUserCache()) {
            for (Group group : u.getGroups()) {
                if (relevantGroups.contains(group)) {
                    relevantUsers.add(u);
                    ((eu.mcone.usermanager.user.User) u).updateGroupPermissions();
                }
            }
        }

        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new GroupUpdateEvent(g, relevantUsers));
        return ClientMessageResponsePacket.success(packet.getRequestUuid());
    }

}
