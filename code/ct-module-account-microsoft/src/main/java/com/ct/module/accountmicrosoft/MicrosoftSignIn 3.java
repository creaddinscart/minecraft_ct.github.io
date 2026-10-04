package com.ct.module.accountmicrosoft;

import com.ct.main.api.Account;
import com.ct.module.accountmicrosoft.auth.DeviceCode;
import com.ct.module.accountmicrosoft.auth.DeviceCodeFlow;
import com.ct.module.accountmicrosoft.auth.MicrosoftEndpoints;
import com.ct.module.accountmicrosoft.auth.MinecraftAuth;
import com.ct.module.accountmicrosoft.auth.XboxAuth;
import java.io.IOException;
import java.util.function.Consumer;

public final class MicrosoftSignIn {
    public static final String BUILT_IN_CLIENT_ID = "00000000402b5328";

    public MicrosoftSignIn() {
    }

    public Account signInDirect(Consumer<String> status) throws IOException, InterruptedException {
        DeviceCode device = DeviceCodeFlow.requestBuiltIn(BUILT_IN_CLIENT_ID, status);
        String microsoftToken = DeviceCodeFlow.pollForMicrosoftToken(
                MicrosoftEndpoints.LIVE_TOKEN, BUILT_IN_CLIENT_ID, device, status);
        return completeSignIn(BUILT_IN_CLIENT_ID, microsoftToken, status);
    }

    public Account signIn(String clientId, Consumer<String> status)
            throws IOException, InterruptedException {
        if (clientId == null || clientId.isBlank()) {
            throw new IOException("Enter an Azure application client ID, or use the built-in "
                    + "Microsoft sign-in.");
        }
        DeviceCode device = DeviceCodeFlow.requestCustom(clientId, status);
        String microsoftToken = DeviceCodeFlow.pollForMicrosoftToken(
                MicrosoftEndpoints.TOKEN, clientId, device, status);
        return completeSignIn(clientId, microsoftToken, status);
    }

    private Account completeSignIn(String clientId, String microsoftToken, Consumer<String> status)
            throws IOException, InterruptedException {
        status.accept("Requesting Xbox Live authorization...");
        XboxAuth xbox = XboxAuth.authorize(clientId, microsoftToken);
        status.accept("Xbox Live authorization accepted. Requesting the Minecraft token...");
        return MinecraftAuth.login(xbox.userHash(), xbox.xstsToken(), xbox.xuid(), clientId);
    }
}
