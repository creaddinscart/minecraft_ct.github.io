package com.ct.module.accountmicrosoft.auth;

import com.google.gson.JsonObject;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.function.Consumer;

public final class DeviceCodeFlow {
    public static final String DEVICE_CODE_GRANT = "urn:ietf:params:oauth:grant-type:device_code";
    public static final String SCOPE = "XboxLive.signin offline_access";

    private DeviceCodeFlow() {
    }

    public static DeviceCode requestBuiltIn(String clientId, Consumer<String> status)
            throws IOException, InterruptedException {
        return request(MicrosoftEndpoints.LIVE_DEVICE_CODE,
                "client_id=" + AuthHttp.form(clientId)
                        + "&scope=" + AuthHttp.form(SCOPE)
                        + "&response_type=device_code", status);
    }

    public static DeviceCode requestCustom(String clientId, Consumer<String> status)
            throws IOException, InterruptedException {
        return request(MicrosoftEndpoints.DEVICE_CODE,
                "client_id=" + AuthHttp.form(clientId)
                        + "&scope=" + AuthHttp.form(SCOPE), status);
    }

    private static DeviceCode request(String endpoint, String body, Consumer<String> status)
            throws IOException, InterruptedException {
        JsonObject device = AuthHttp.postForm(endpoint, body);
        String userCode = AuthHttp.requireString(device, "user_code", "the device code request");
        String verificationUri = AuthHttp.requireString(device, "verification_uri",
                "the device code request");
        status.accept("Enter the code " + userCode + " at " + verificationUri
                + " and sign in with your Microsoft account.");
        openBrowser(verificationUri);
        return new DeviceCode(AuthHttp.requireString(device, "device_code", "the device code request"),
                AuthHttp.requireInt(device, "interval", "the device code request"),
                AuthHttp.requireInt(device, "expires_in", "the device code request"));
    }

    public static String pollForMicrosoftToken(String endpoint, String clientId, DeviceCode device,
            Consumer<String> status) throws IOException, InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(device.expiresIn()).toNanos();
        int interval = device.interval();
        String body = "grant_type=" + AuthHttp.form(DEVICE_CODE_GRANT)
                + "&client_id=" + AuthHttp.form(clientId)
                + "&device_code=" + AuthHttp.form(device.code());

        while (System.nanoTime() < deadline) {
            Thread.sleep(Duration.ofSeconds(interval));
            var response = AuthHttp.postFormResponse(endpoint, body);
            JsonObject token = AuthHttp.parse(response);
            if (response.statusCode() == 200) {
                status.accept("Microsoft authorization complete. Validating the Minecraft profile...");
                return AuthHttp.requireString(token, "access_token", "the token poll");
            }

            String error = token.has("error") ? token.get("error").getAsString() : "authorization_failed";
            switch (error) {
                case "authorization_pending" -> {
                }
                case "slow_down" -> interval += 5;
                case "authorization_declined" -> throw new IOException("Microsoft sign-in was declined.");
                case "expired_token" -> throw new IOException(
                        "Microsoft sign-in expired. Start sign-in again for a new code.");
                case "bad_verification_code" -> throw new IOException(
                        "Microsoft rejected the sign-in code. Start sign-in again.");
                default -> throw new IOException("Microsoft sign-in failed: " + error + ".");
            }
        }
        throw new IOException("Microsoft sign-in timed out. Start sign-in again for a new code.");
    }

    private static void openBrowser(String address) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(URI.create(address));
            } catch (IOException | RuntimeException ignored) {
                return;
            }
        }
    }
}
