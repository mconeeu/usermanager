package eu.mcone.usermanager.api.event;

import group.onegaming.networkmanager.host.api.event.Event;
import eu.mcone.usermanager.api.user.Group;
import eu.mcone.usermanager.api.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
@Getter
public class GroupUpdateEvent extends Event {

    private final Group group;
    private final Collection<User> relevantUsers;

}
