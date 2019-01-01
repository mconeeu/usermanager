/*
 * Copyright (c) 2017 - 2018 Dominik Lippl, Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.usermanager.api.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public final class UserSettings {

    private boolean enableFriendRequests = true, acceptedAgbs = false;
    private Language language;
    private Sender privateMessages = Sender.FRIENDS, partyInvites = Sender.ALL;

    public enum Sender {
        ALL, FRIENDS, NOBODY
    }

}
