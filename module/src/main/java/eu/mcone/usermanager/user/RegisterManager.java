package eu.mcone.usermanager.user;

import group.onegaming.networkmanager.core.api.util.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterManager {

    private static Map<UUID, Integer> registerCue = new HashMap<>();

    static int getNewSecret(UUID uuid) {
        int secret = Random.randomInt(1000, 9999);
        registerCue.put(uuid, secret);

        return secret;
    }

    static boolean verifySecret(UUID uuid, int secret) {
        return registerCue.containsKey(uuid) && registerCue.get(uuid).equals(secret);
    }

}
