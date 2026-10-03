package com.ct.module.accountmicrosoft.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;

public record XboxAuth(String userHash, String xstsToken, String xuid) {
    public static XboxAuth authorize(String clientId, String microsoftToken)
            throws IOException, InterruptedException {
        JsonObject userProperties = new JsonObject();
        userProperties.addProperty("AuthMethod", "RPS");
        userProperties.addProperty("SiteName", "user.auth.xboxlive.com");
        userProperties.addProperty("RpsTicket", ticket(clientId, microsoftToken));
        JsonObject xboxRequest = new JsonObject();
        xboxRequest.add("Properties", userProperties);
        xboxRequest.addProperty("RelyingParty", "http://auth.xboxlive.com");
        xboxRequest.addProperty("TokenType", "JWT");
        JsonObject xbox = AuthHttp.postJson(MicrosoftEndpoints.XBOX_USER_AUTH, xboxRequest);

        JsonObject userClaim = firstClaim(xbox, "the Xbox Live user authentication");
        String userHash = AuthHttp.requireString(userClaim, "uhs", "the Xbox Live user claim");

        JsonArray userTokens = new JsonArray();
        userTokens.add(AuthHttp.requireString(xbox, "Token", "the Xbox Live user authentication"));
        JsonObject xstsProperties = new JsonObject();
        xstsProperties.addProperty("SandboxId", "RETAIL");
        xstsProperties.add("UserTokens", userTokens);
        JsonObject xstsRequest = new JsonObject();
        xstsRequest.add("Properties", xstsProperties);
        xstsRequest.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        xstsRequest.addProperty("TokenType", "JWT");
        JsonObject xsts = AuthHttp.postJson(MicrosoftEndpoints.XSTS_AUTHORIZE, xstsRequest);
        String xstsToken = AuthHttp.requireString(xsts, "Token", "the Xbox XSTS authorization");
        JsonObject xstsClaim = firstClaim(xsts, "the Xbox XSTS authorization");
        String xuid = "";
        if (xstsClaim.has("xid") && xstsClaim.get("xid").isJsonPrimitive()) {
            xuid = xstsClaim.get("xid").getAsString();
        } else if (xstsClaim.has("uhs") && xstsClaim.get("uhs").isJsonPrimitive()) {
            xuid = xstsClaim.get("uhs").getAsString();
        }
        return new XboxAuth(userHash, xstsToken, xuid);
    }

    private static String ticket(String clientId, String microsoftToken) {
        if (com.ct.module.accountmicrosoft.MicrosoftSignIn.BUILT_IN_CLIENT_ID.equals(clientId)) {
            return "d=" + microsoftToken;
        }
        return microsoftToken;
    }

    private static JsonObject firstClaim(JsonObject response, String context) throws IOException {
        if (!response.has("DisplayClaims") || !response.getAsJsonObject("DisplayClaims").has("xui")
                || response.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").size() == 0) {
            throw new IOException("The " + context + " response listed no user claims.");
        }
        return response.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject();
    }
}
