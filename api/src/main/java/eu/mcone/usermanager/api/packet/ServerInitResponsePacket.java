package eu.mcone.usermanager.api.packet;

import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerInitResponsePacket extends ClientMessageResponsePacket {



    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        super.onWrite(out);
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        super.onRead(in);
    }

}
