package eu.mcone.usermanager.api.packet;

import group.onegaming.networkmanager.api.packet.ClientMessageRequestPacket;
import eu.mcone.usermanager.api.messaging.URIs;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class UserUpdateRequestPacket extends ClientMessageRequestPacket {

    private UUID uuid;

    public UserUpdateRequestPacket(UUID uuid) {
        super(URIs.USER_UPDATE, HttpMethod.POST);
        this.uuid = uuid;
    }

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        super.onWrite(out);
        out.writeUTF(uuid.toString());
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        super.onRead(in);
        uuid = UUID.fromString(in.readUTF());
    }

}
