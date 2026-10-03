package com.ct.module.rules;

final class VersionComparer {
    private VersionComparer() {
    }

    static int compare(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        for (int index = 0; index < Math.max(leftParts.length, rightParts.length); index++) {
            int leftPart = index < leftParts.length ? parsePart(leftParts[index]) : 0;
            int rightPart = index < rightParts.length ? parsePart(rightParts[index]) : 0;
            int comparison = Integer.compare(leftPart, rightPart);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private static int parsePart(String part) {
        String digits = part.replaceFirst("^([0-9]+).*$", "$1");
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
