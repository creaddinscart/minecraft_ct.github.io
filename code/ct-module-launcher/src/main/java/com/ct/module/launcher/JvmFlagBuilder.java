package com.ct.module.launcher;

import com.ct.main.api.LaunchOptions;
import com.ct.module.rules.RuleEvaluator;
import com.ct.module.rules.RuleFeatures;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class JvmFlagBuilder {
    private JvmFlagBuilder() {
    }

    static List<String> userJvmArguments(JsonObject arguments, LaunchOptions options)
            throws IOException {
        String minimum = "-Xms" + options.minimumMemoryMb() + "M";
        String maximum = "-Xmx" + options.maximumMemoryMb() + "M";

        List<String> configured = new ArrayList<>();
        if (options.recommendedJvmFlags() && arguments.has("default-user-jvm")) {
            configured.addAll(ArgumentExpander.expand(arguments.getAsJsonArray("default-user-jvm"),
                    RuleFeatures.none(), Map.of()));
        }

        List<String> result = new ArrayList<>();
        boolean memoryApplied = false;
        for (String value : configured) {
            if (value.startsWith("-Xms") || value.startsWith("-Xmx")) {
                if (!memoryApplied) {
                    result.add(minimum);
                    result.add(maximum);
                    memoryApplied = true;
                }
                continue;
            }
            result.add(value);
        }
        if (!memoryApplied) {
            result.add(0, maximum);
            result.add(0, minimum);
        }
        return result;
    }
}
