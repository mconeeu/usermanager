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
public class UserInfoRequestPacket extends ClientMessageRequestPacket {

    private UUID uuid;
    private String name;

    public UserInfoRequestPacket(UUID uuid, String name) {
        super(URIs.USER_REQUEST_INFO, HttpMethod.GET);
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        super.onWrite(out);
        out.writeUTF(uuid.toString());
        out.writeUTF(name);
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        super.onRead(in);
        uuid = UUID.fromString(in.readUTF());
        name = in.readUTF();
    }

}
