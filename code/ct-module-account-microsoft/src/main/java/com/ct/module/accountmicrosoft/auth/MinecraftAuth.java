package com.ct.module.accountmicrosoft.auth;

import com.ct.main.api.Account;
import com.google.gson.JsonObject;
import java.io.IOException;

public final class MinecraftAuth {
    private MinecraftAuth() {
    }

    public static Account login(String userHash, String xstsToken, String xuid, String clientId)
            throws IOException, InterruptedException {
        JsonObject loginRequest = new JsonObject();
        loginRequest.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
        JsonObject minecraft = AuthHttp.postJson(
                MicrosoftEndpoints.MINECRAFT_LOGIN_WITH_XBOX, loginRequest);
        String minecraftToken = AuthHttp.requireString(minecraft, "access_token",
                "the Minecraft services login");

        JsonObject profile = AuthHttp.getJson(MicrosoftEndpoints.MINECRAFT_PROFILE, minecraftToken);
        String profileName = AuthHttp.requireString(profile, "name", "the Minecraft profile");
        String profileId = AuthHttp.requireString(profile, "id", "the Minecraft profile");
        return new Account(profileName, profileId, minecraftToken, xuid, clientId,
                Account.TYPE_MICROSOFT);
    }
}
