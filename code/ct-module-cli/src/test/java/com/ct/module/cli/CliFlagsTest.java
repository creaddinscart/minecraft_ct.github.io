package com.ct.module.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class CliFlagsTest {
    @Test
    void valueFlagsConsumeTheNextArgument() throws IOException {
        Map<String, String> options = CliRunner.parse(
                new String[] {"--version", "26.3", "--install-only"});
        assertEquals("26.3", options.get("--version"));
        assertEquals("true", options.get("--install-only"));
    }

    @Test
    void missingValuesAreRejected() {
        IOException exception = assertThrows(IOException.class,
                () -> CliRunner.parse(new String[] {"--player"}));
        assertEquals("Missing value for --player.", exception.getMessage());
    }

    @Test
    void plainArgumentsAreRejected() {
        IOException exception = assertThrows(IOException.class,
                () -> CliRunner.parse(new String[] {"launch"}));
        assertEquals("Unexpected argument: launch", exception.getMessage());
    }

    @Test
    void unknownFlagsAreStoredAsSwitches() throws IOException {
        assertTrue(CliRunner.parse(new String[] {"--dry-run"}).containsKey("--dry-run"));
    }
}
