package com.ct.module.accountmicrosoft.auth;

public final class MicrosoftEndpoints {
    public static final String DEVICE_CODE =
            "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    public static final String TOKEN =
            "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    public static final String LIVE_DEVICE_CODE =
            "https://login.live.com/oauth20_connect.srf";
    public static final String LIVE_TOKEN =
            "https://login.live.com/oauth20_token.srf";
    public static final String XBOX_USER_AUTH =
            "https://user.auth.xboxlive.com/user/authenticate";
    public static final String XSTS_AUTHORIZE =
            "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MINECRAFT_LOGIN_WITH_XBOX =
            "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String MINECRAFT_PROFILE =
            "https://api.minecraftservices.com/minecraft/profile";

    private MicrosoftEndpoints() {
    }
}
