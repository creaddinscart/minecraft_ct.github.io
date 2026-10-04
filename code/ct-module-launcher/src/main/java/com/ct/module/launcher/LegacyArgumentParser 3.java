package com.ct.module.launcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

final class LegacyArgumentParser {
    private LegacyArgumentParser() {
    }

    static JsonArray gameArguments(JsonObject metadata) {
        JsonArray result = new JsonArray();
        if (!metadata.has("minecraftArguments")) {
            return result;
        }
        StringBuilder token = new StringBuilder();
        char quote = 0;
        String arguments = metadata.get("minecraftArguments").getAsString();
        for (int index = 0; index < arguments.length(); index++) {
            char character = arguments.charAt(index);
            if (quote != 0) {
                if (character == quote) {
                    quote = 0;
                } else {
                    token.append(character);
                }
            } else if (character == '\'' || character == '"') {
                quote = character;
            } else if (Character.isWhitespace(character)) {
                if (!token.isEmpty()) {
                    result.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(character);
            }
        }
        if (!token.isEmpty()) {
            result.add(token.toString());
        }
        return result;
    }
}
