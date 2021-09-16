package eu.mcone.usermanager.api.packet;

import group.onegaming.networkmanager.api.packet.ClientMessageResponsePacket;
import eu.mcone.usermanager.api.user.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

@NoArgsConstructor
@Getter
public class UserInfoResponsePacket extends ClientMessageResponsePacket {

    private UUID uuid;
    private String name, teamspeakUid, discordUid, mailAdress;
    private int coins, emeralds;
    private long onlineTime;
    private Map<String, String> permissionMap;
    private UserSettings settings;
    private PlayerState state;
    private Set<Group> groups;

    public UserInfoResponsePacket(String requestUuid, HttpResponseStatus status, User u) {
        super(requestUuid, status);
        this.uuid = u.getUuid();
        this.name = u.getName();
        this.teamspeakUid = u.getTeamspeakUid();
        this.discordUid = u.getDiscordUid();
        this.mailAdress = u.getMailAddress();
        this.coins = u.getCoins();
        this.emeralds = u.getEmeralds();
        this.onlineTime = u.getOnlinetime();
        this.permissionMap = u.getPermissionMap();
        this.settings = u.getSettings();
        this.state = u.getState();
        this.groups = u.getGroups();
    }

    @Override
    public void onWrite(DataOutputStream out) throws IOException {
        super.onWrite(out);
        out.writeUTF(uuid.toString());
        out.writeUTF(name);
        out.writeUTF(teamspeakUid);
        out.writeUTF(discordUid);
        out.writeUTF(mailAdress);
        out.writeInt(coins);
        out.writeInt(emeralds);
        out.writeLong(onlineTime);

        out.writeInt(permissionMap.size());
        for (Map.Entry<String, String> e : permissionMap.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }

        out.writeBoolean(settings.isEnableFriendRequests());
        out.writeBoolean(settings.isAcceptedAgbs());
        out.writeUTF(settings.getLanguage().toString());
        out.writeUTF(settings.getPrivateMessages().toString());
        out.writeUTF(settings.getPartyInvites().toString());

        out.writeInt(state.getId());

        out.writeInt(groups.size());
        for (Group g : groups) {
            out.writeInt(g.getId());
        }
    }

    @Override
    public void onRead(DataInputStream in) throws IOException {
        super.onRead(in);
        uuid = UUID.fromString(in.readUTF());
        name = in.readUTF();
        teamspeakUid = in.readUTF();
        discordUid = in.readUTF();
        mailAdress = in.readUTF();
        coins = in.readInt();
        emeralds = in.readInt();
        onlineTime = in.readLong();

        permissionMap = new HashMap<>();
        int pSize = in.readInt();
        for (int i = 0; i < pSize; i++) {
            permissionMap.put(in.readUTF(), in.readUTF());
        }

        settings = new UserSettings(
                in.readBoolean(),
                in.readBoolean(),
                Language.valueOf(in.readUTF()),
                UserSettings.Sender.valueOf(in.readUTF()),
                UserSettings.Sender.valueOf(in.readUTF())
        );

        state = PlayerState.getPlayerStateById(in.readInt());

        groups = new HashSet<>();
        int gSize = in.readInt();
        for (int i = 0; i < gSize; i++) {
            groups.add()
        }
    }

}
