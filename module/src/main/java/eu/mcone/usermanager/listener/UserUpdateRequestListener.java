package eu.mcone.usermanager.listener;

import group.onegaming.networkmanager.api.messaging.request.CustomClientMessageRequestListener;
import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;
import group.onegaming.networkmanager.host.api.ModuleHost;
import eu.mcone.usermanager.UserManager;
import eu.mcone.usermanager.api.event.UserUpdateEvent;
import eu.mcone.usermanager.api.packet.UserUpdateRequestPacket;
import eu.mcone.usermanager.user.User;

public class UserUpdateRequestListener extends CustomClientMessageRequestListener<UserUpdateRequestPacket> {

    @Override
    protected ClientMessageResponsePacket onCustomClientRequest(UserUpdateRequestPacket packet) {
        User u = (User) UserManager.getManager().getOfflineUser(packet.getUuid());
        u.updateUserFromDatabase();

        ModuleHost.getInstance().getEventManager().callEvent(UserManager.getManager(), new UserUpdateEvent(u));
        return ClientMessageResponsePacket.success(packet.getRequestUuid());
    }

}
