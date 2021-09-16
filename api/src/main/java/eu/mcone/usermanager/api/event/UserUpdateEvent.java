package eu.mcone.usermanager.api.event;

import group.onegaming.networkmanager.host.api.event.Event;
import eu.mcone.usermanager.api.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserUpdateEvent extends Event {

    private final User user;

}
