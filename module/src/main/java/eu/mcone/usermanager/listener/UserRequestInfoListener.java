package eu.mcone.usermanager.listener;

import group.onegaming.networkmanager.api.messaging.request.CustomClientMessageRequestListener;
import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;
import eu.mcone.usermanager.UserManager;
import eu.mcone.usermanager.api.packet.UserInfoRequestPacket;
import eu.mcone.usermanager.api.packet.UserInfoResponsePacket;
import io.netty.handler.codec.http.HttpResponseStatus;

public class UserRequestInfoListener extends CustomClientMessageRequestListener<UserInfoRequestPacket> {

    @Override
    protected ClientMessageResponsePacket onCustomClientRequest(UserInfoRequestPacket packet) {
        return new UserInfoResponsePacket(
                packet.getRequestUuid(),
                HttpResponseStatus.OK,
                UserManager.getManager().getUser(
                        packet.getUuid(),
                        packet.getName()
                )
        );
    }

}
