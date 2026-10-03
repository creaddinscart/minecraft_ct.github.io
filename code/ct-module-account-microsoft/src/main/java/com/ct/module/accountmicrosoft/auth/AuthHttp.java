package com.ct.module.accountmicrosoft.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public final class AuthHttp {
    public static final Map<String, String> XSTS_ERRORS = Map.of(
            "2148916227", "This Microsoft account has been banned from Xbox Live.",
            "2148916233", "This Microsoft account has no Xbox profile. Sign in at xbox.com once, then retry.",
            "2148916235", "Xbox Live is not available in the region of this Microsoft account.",
            "2148916236", "This Microsoft account requires adult verification.",
            "2148916237", "This Microsoft account requires adult verification.",
            "2148916238", "This Microsoft account is managed by an organization and cannot sign in here.");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private AuthHttp() {
    }

    public static JsonObject postForm(String address, String body)
            throws IOException, InterruptedException {
        HttpResponse<String> response = postFormResponse(address, body);
        if (response.statusCode() != 200) {
            JsonObject error = parse(response);
            throw new IOException(error.has("error_description")
                    ? error.get("error_description").getAsString()
                    : "The Microsoft sign-in request failed with HTTP " + response.statusCode() + ".");
        }
        return parse(response);
    }

    public static HttpResponse<String> postFormResponse(String address, String body)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(address))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static JsonObject postJson(String address, JsonObject body)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(address))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(describeFailure(address, response));
        }
        return parse(response);
    }

    public static JsonObject getJson(String address, String bearerToken)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(address))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + bearerToken)
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            throw new IOException("This Microsoft account does not own Minecraft Java Edition.");
        }
        if (response.statusCode() != 200) {
            throw new IOException("The Minecraft profile request failed with HTTP "
                    + response.statusCode() + ".");
        }
        JsonObject body = parse(response);
        if (body.has("error")) {
            throw new IOException("The Minecraft profile request reported: "
                    + body.get("error").getAsString() + ". Start sign-in again.");
        }
        return body;
    }

    public static String describeFailure(String address, HttpResponse<String> response) {
        if (address.contains("xsts.auth.xboxlive.com")) {
            String body = response.body();
            for (Map.Entry<String, String> entry : XSTS_ERRORS.entrySet()) {
                if (body.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return "Xbox Live authorization failed with HTTP " + response.statusCode() + ".";
        }
        return "Authentication failed with HTTP " + response.statusCode() + ".";
    }

    public static JsonObject parse(HttpResponse<String> response) throws IOException {
        try {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IOException("The authentication service returned an invalid response.", exception);
        }
    }

    public static String requireString(JsonObject object, String field, String context) throws IOException {
        if (object == null || object.get(field) == null || !object.get(field).isJsonPrimitive()) {
            throw new IOException("Missing the '" + field + "' value in " + context
                    + ". Start sign-in again; if this repeats, the authentication "
                    + "service changed its response format.");
        }
        return object.get(field).getAsString();
    }

    public static int requireInt(JsonObject object, String field, String context) throws IOException {
        if (object == null || object.get(field) == null || !object.get(field).isJsonPrimitive()) {
            throw new IOException("Missing the '" + field + "' value in " + context
                    + ". Start sign-in again; if this repeats, the authentication "
                    + "service changed its response format.");
        }
        try {
            return object.get(field).getAsInt();
        } catch (NumberFormatException exception) {
            throw new IOException("The " + context + " response sent a '" + field
                    + "' value that is not a number.", exception);
        }
    }

    public static String form(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
