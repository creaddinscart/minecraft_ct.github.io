package com.ct.module.installer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class MetadataStore {
    private MetadataStore() {
    }

    static VerifiedDocument resolve(Path gameDirectory, String version, JsonObject selectedVersion)
            throws IOException, InterruptedException {
        Path metadataFile = metadataFile(gameDirectory, version);
        if (Files.isRegularFile(metadataFile)
                && Sha1.matches(metadataFile, selectedVersion.get("sha1").getAsString())) {
            String raw = new String(Files.readAllBytes(metadataFile), StandardCharsets.UTF_8);
            return new VerifiedDocument(JsonParser.parseString(raw).getAsJsonObject(), raw);
        }
        VerifiedDocument document = HttpApi.fetchVerifiedJson(selectedVersion.get("url").getAsString(),
                selectedVersion.get("sha1").getAsString());
        if (!document.json().get("id").getAsString().equals(version)) {
            throw new IOException("Official metadata returned an unexpected game version.");
        }
        return document;
    }

    static void store(Path gameDirectory, String version, JsonObject selectedVersion,
            VerifiedDocument document) throws IOException {
        Path metadataFile = metadataFile(gameDirectory, version);
        Files.createDirectories(metadataFile.getParent());
        if (!Files.isRegularFile(metadataFile)
                || !Sha1.matches(metadataFile, selectedVersion.get("sha1").getAsString())) {
            Files.writeString(metadataFile, document.raw(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }
    }

    static Path metadataFile(Path gameDirectory, String version) {
        return gameDirectory.resolve("versions").resolve(version).resolve(version + ".json");
    }

    static JsonObject read(Path metadataFile) throws IOException {
        return JsonParser.parseString(Files.readString(metadataFile)).getAsJsonObject();
    }
}
