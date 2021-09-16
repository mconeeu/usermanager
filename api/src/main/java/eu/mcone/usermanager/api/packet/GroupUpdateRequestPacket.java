package eu.mcone.usermanager.api.packet;

import group.onegaming.networkmanager.api.packet.ClientMessageRequestPacket;
import eu.mcone.usermanager.api.UserManager;
import eu.mcone.usermanager.api.messaging.URIs;
import eu.mcone.usermanager.api.user.Group;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@NoArgsConstructor
@Getter
public class GroupUpdateRequestPacket extends ClientMessageRequestPacket {

    private Group group;

    public GroupUpdateRequestPacket(Group group) {
        super(URIs.GROUP_UPDATE, HttpMethod.POST);
        this.group = group;
    }

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        super.onWrite(out);
        out.writeInt(group.getId());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        super.onRead(in);
        group = UserManager.getInstance().getPermissionManager().getGroup(in.readInt());
    }

}
