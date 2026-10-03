package com.ct.module.launcher;

import com.ct.main.api.Account;
import com.ct.main.api.LaunchOptions;
import com.ct.module.installer.Installation;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class PlaceholderMap {
    private PlaceholderMap() {
    }

    static Map<String, String> build(Installation installation, Account account,
            LaunchOptions options, Path gameDirectory, String launcherVersion, String gameClasspath) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("auth_player_name", account.name());
        placeholders.put("auth_uuid", account.uuid());
        placeholders.put("auth_access_token", account.accessToken());
        placeholders.put("auth_xuid", account.xuid());
        placeholders.put("auth_session", account.accessToken());
        placeholders.put("clientid", account.clientId());
        placeholders.put("user_type", account.userType());
        placeholders.put("version_name", installation.versionId());
        placeholders.put("version_type", options.versionType());
        placeholders.put("game_directory", gameDirectory.toString());
        placeholders.put("game_assets", installation.assetsDirectory().toString());
        placeholders.put("assets_root", installation.assetsDirectory().toString());
        placeholders.put("assets_index_name", installation.assetIndexId());
        placeholders.put("natives_directory", installation.nativesDirectory().toString());
        placeholders.put("launcher_name", "ct-client");
        placeholders.put("launcher_version", launcherVersion);
        placeholders.put("classpath", gameClasspath);
        placeholders.put("classpath_separator", System.getProperty("path.separator"));
        placeholders.put("library_directory", installation.librariesDirectory().toString());
        placeholders.put("resolution_width", Integer.toString(options.width()));
        placeholders.put("resolution_height", Integer.toString(options.height()));
        placeholders.put("quickPlayPath", "");
        placeholders.put("quickPlaySingleplayer", "");
        placeholders.put("quickPlayMultiplayer", "");
        placeholders.put("quickPlayRealms", "");
        if (installation.logConfig() != null) {
            placeholders.put("path", installation.logConfig().toString());
        }
        return placeholders;
    }
}
