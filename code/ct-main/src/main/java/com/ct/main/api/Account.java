package com.ct.main.api;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public record Account(String name, String uuid, String accessToken, String xuid, String clientId,
        String userType) {
    public static final String TYPE_MICROSOFT = "msa";
    public static final String TYPE_OFFLINE = "legacy";

    private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");

    public static Account offline(String playerName) {
        String name = playerName == null ? "" : playerName.trim();
        if (!PLAYER_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Offline profile names must be 3 to 16 characters using letters, numbers, and underscores.");
        }
        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "");
        return new Account(name, uuid, "0", "0", "", TYPE_OFFLINE);
    }

    public boolean isOffline() {
        return TYPE_OFFLINE.equals(userType);
    }

    public String label() {
        return name + (isOffline() ? " (offline profile)" : "");
    }
}
