package com.ct.module.installer;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class AssetPlanner {
    private AssetPlanner() {
    }

    static List<DownloadRequest> collect(JsonObject index, Path assetsDirectory) throws IOException {
        List<DownloadRequest> downloads = new ArrayList<>();
        Path objectsDirectory = assetsDirectory.resolve("objects");
        for (Map.Entry<String, com.google.gson.JsonElement> entry
                : index.getAsJsonObject("objects").entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            String hash = object.get("hash").getAsString();
            String prefix = hash.substring(0, 2);
            downloads.add(new DownloadRequest(OfficialEndpoints.ASSET_BASE + prefix + "/" + hash,
                    SafePaths.resolve(objectsDirectory, prefix + "/" + hash), hash,
                    object.get("size").getAsLong()));
        }
        return downloads;
    }
}
