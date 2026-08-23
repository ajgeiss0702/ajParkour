package us.ajg0702.parkour.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void debugFallbackLogDefaultsToDisabled() {
        assertFalse(NayatsuGenerationConfig.booleanValue(null, false));
    }

    @Test
    void recentHistorySizeAcceptsNumberOrStringWithFallback() {
        assertEquals(3, NayatsuGenerationConfig.intValue(3, 5));
        assertEquals(7, NayatsuGenerationConfig.intValue("7", 5));
        assertEquals(5, NayatsuGenerationConfig.intValue("bad", 5));
        assertEquals(5, NayatsuGenerationConfig.intValue(null, 5));
    }
}
