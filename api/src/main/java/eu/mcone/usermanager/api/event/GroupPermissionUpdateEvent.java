package eu.mcone.usermanager.api.event;

import eu.mcone.networkmanager.api.event.Event;
import eu.mcone.usermanager.api.user.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupPermissionUpdateEvent extends Event {

    @Getter
    private final Group group;

}
