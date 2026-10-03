package com.ct.module.launcher;

import com.ct.module.rules.RuleEvaluator;
import com.ct.module.rules.RuleFeatures;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ArgumentExpander {
    private ArgumentExpander() {
    }

    static List<String> expand(JsonArray entries, RuleFeatures features,
            Map<String, String> placeholders) throws IOException {
        List<String> values = new ArrayList<>();
        if (entries == null) {
            return values;
        }
        for (JsonElement entry : entries) {
            if (entry.isJsonPrimitive()) {
                values.add(substitute(entry.getAsString(), placeholders));
                continue;
            }
            JsonObject object = entry.getAsJsonObject();
            if (!RuleEvaluator.allows(object, features) || !object.has("value")) {
                continue;
            }
            JsonElement value = object.get("value");
            if (value.isJsonArray()) {
                for (JsonElement element : value.getAsJsonArray()) {
                    values.add(substitute(element.getAsString(), placeholders));
                }
            } else {
                values.add(substitute(value.getAsString(), placeholders));
            }
        }
        return values;
    }

    static String substitute(String template, Map<String, String> placeholders) throws IOException {
        StringBuilder result = new StringBuilder();
        int position = 0;
        while (position < template.length()) {
            int start = template.indexOf("${", position);
            if (start < 0) {
                result.append(template, position, template.length());
                break;
            }
            int end = template.indexOf('}', start);
            if (end < 0) {
                result.append(template, position, template.length());
                break;
            }
            result.append(template, position, start);
            String key = template.substring(start + 2, end);
            String value = placeholders.get(key);
            if (value == null) {
                throw new IOException("Official metadata requested an unsupported launch value: ${"
                        + key + "}.");
            }
            result.append(value);
            position = end + 1;
        }
        return result.toString();
    }
}
