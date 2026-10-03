package com.ct.module.installer;

import com.ct.module.rules.RuleEvaluator;
import com.ct.module.rules.RuleFeatures;
import com.google.gson.JsonObject;

final class RuleGate {
    private RuleGate() {
    }

    static boolean allows(JsonObject library) {
        return RuleEvaluator.allows(library, RuleFeatures.none());
    }
}
