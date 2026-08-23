package us.ajg0702.parkour.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NayatsuGenerationConfigTest {

    @Test
    void missingGenerationEnabledDefaultsToDisabledForOldConfigs() {
        assertFalse(NayatsuGenerationConfig.generationEnabled(null));
    }

    @Test
    void explicitGenerationEnabledAcceptsBooleanOrString() {
        assertTrue(NayatsuGenerationConfig.generationEnabled(true));
        assertTrue(NayatsuGenerationConfig.generationEnabled("true"));
        assertFalse(NayatsuGenerationConfig.generationEnabled(false));
        assertFalse(NayatsuGenerationConfig.generationEnabled("false"));
    }

    @Test
    void nestedAntiUTurnDefaultsToEnabledWhenNamespaceIsEnabled() {
        assertTrue(NayatsuGenerationConfig.booleanValue(null, true));
    }
}
