package com.ct.module.installer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class HttpApi {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private HttpApi() {
    }

    public static HttpClient client() {
        return HTTP;
    }

    public static URI httpsUri(String address) throws IOException {
        URI uri = URI.create(address);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IOException("Refusing a non-HTTPS download URL.");
        }
        return uri;
    }

    public static HttpRequest.Builder request(String address) throws IOException {
        return HttpRequest.newBuilder(httpsUri(address));
    }

    public static JsonObject fetchJson(String address) throws IOException, InterruptedException {
        HttpRequest request = request(address)
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Official metadata request failed with HTTP "
                    + response.statusCode() + ".");
        }
        try {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IOException("Official metadata was not valid JSON.", exception);
        }
    }

    public static VerifiedDocument fetchVerifiedJson(String address, String expectedSha1)
            throws IOException, InterruptedException {
        HttpRequest request = request(address)
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();
        HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200 || !Sha1.of(response.body()).equalsIgnoreCase(expectedSha1)) {
            throw new IOException("Official metadata failed its SHA-1 verification.");
        }
        String raw = new String(response.body(), StandardCharsets.UTF_8);
        try {
            return new VerifiedDocument(JsonParser.parseString(raw).getAsJsonObject(), raw);
        } catch (RuntimeException exception) {
            throw new IOException("Official metadata was not valid JSON.", exception);
        }
    }
}
