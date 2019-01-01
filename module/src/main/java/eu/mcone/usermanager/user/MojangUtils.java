/*
 * Copyright (c) 2017 - 2018 Dominik Lippl, Rufus Maiwald and the MC ONE Minecraftnetwork. All rights reserved
 * You are not allowed to decompile the code
 *
 */

package eu.mcone.usermanager.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import eu.mcone.networkmanager.core.api.database.MongoDatabase;
import eu.mcone.usermanager.api.exception.MojangApiException;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class MojangUtils {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final MongoCollection<Document> userinfo;
    private final HashMap<String, UUID> uuidCache = new HashMap<>();

    public MojangUtils(MongoDatabase database) {
        userinfo = database.getCollection("userinfo");
    }

    public UUID fetchUuid(final String name) throws MojangApiException {
        if (uuidCache.containsKey(name)) return uuidCache.get(name);

        Document dbEntry = userinfo.find(eq("name", name)).first();
        if (dbEntry != null) {
            return UUID.fromString(dbEntry.getString("uuid"));
        } else {
            return fetchUuidFromMojangAPI(name);
        }
    }

    public String fetchName(final UUID uuid) throws MojangApiException {
        for (HashMap.Entry<String, UUID> entry : uuidCache.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }

        Document dbEntry = userinfo.find(eq("uuid", uuid.toString())).first();
        if (dbEntry != null) {
            return dbEntry.getString("name");
        } else {
            return fetchNameFromMojangAPI(uuid);
        }
    }

    public UUID fetchUuidFromMojangAPI(final String name) throws MojangApiException {
        try {
            JsonElement result = getJsonApiResult("https://api.mojang.com/users/profiles/minecraft/" + name);
            JsonObject obj = result.getAsJsonObject();
            String uuid = obj.get("id").getAsString();

            UUID uuidResult = UUID.fromString(fromTrimmed(uuid));
            uuidCache.put(name, uuidResult);

            return uuidResult;
        } catch (IOException | IllegalStateException e) {
            throw new MojangApiException(e);
        }
    }

    public String fetchNameFromMojangAPI(final UUID uuid) throws MojangApiException {
        try {
            JsonElement result = getJsonApiResult("https://sessionserver.mojang.com/session/minecraft/profile/" + toTrimmed(uuid.toString()));
            JsonObject obj = result.getAsJsonObject();
            String name = obj.get("name").getAsString();

            uuidCache.put(name, uuid);

            return name;
        } catch (IOException | IllegalStateException e) {
            throw new MojangApiException(e);
        }
    }

    private static String fromTrimmed(final String trimmedUUID) throws IllegalArgumentException {
        if (trimmedUUID == null) throw new IllegalArgumentException();

        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }

        return builder.toString();
    }

    private static String toTrimmed(final String uuid) {
        return uuid.replace("-", "");
    }

    private static JsonElement getJsonApiResult(String urlString) throws IOException {
        URL url = new URL(urlString);

        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);

        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = reader.readLine()) != null) {
            sb.append(s);
        }

        reader.close();
        isr.close();
        is.close();

        return JSON_PARSER.parse(sb.toString());
    }

}
