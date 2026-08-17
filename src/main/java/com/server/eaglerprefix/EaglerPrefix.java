package com.server.eaglerprefix;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.GameProfile;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Plugin(
    id = "eaglerprefix",
    name = "EaglerPrefix",
    version = "1.0",
    description = "Forces a protocol prefix for web players"
)
public class EaglerPrefix {

    private final Logger logger;

    @Inject
    public EaglerPrefix(Logger logger) {
        this.logger = logger;
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        InboundConnection connection = event.getConnection();

        try {
            Method getUsernameMethod = connection.getClass().getMethod("getUsername");
            String rawName = (String) getUsernameMethod.invoke(connection);

            if (rawName != null && !rawName.startsWith("*")) {
                String prefixed = "*" + rawName;
                if (prefixed.length() > 16) {
                    prefixed = prefixed.substring(0, 16);
                }

                Field usernameField = connection.getClass().getDeclaredField("username");
                usernameField.setAccessible(true);
                usernameField.set(connection, prefixed);

                try {
                    Field profileField = connection.getClass().getDeclaredField("gameProfile");
                    profileField.setAccessible(true);
                    GameProfile profile = (GameProfile) profileField.get(connection);
                    if (profile != null) {
                        profileField.set(connection, new GameProfile(profile.getId(), prefixed, profile.getProperties()));
                    }
                } catch (NoSuchFieldException ignored) {}

                logger.info("Renamed connection profile: {} -> {}", rawName, prefixed);
            }
        } catch (Exception e) {
            logger.error("Could not set protocol prefix: ", e);
        }
    }
}
