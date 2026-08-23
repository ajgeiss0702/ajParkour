package us.ajg0702.parkour.game;

import us.ajg0702.parkour.Main;

final class NayatsuGenerationConfig {

	static final String ENABLED = "nayatsu-generation.enabled";
	static final String ANTI_U_TURN_ENABLED = "nayatsu-generation.anti-u-turn.enabled";
	static final String DEBUG_FALLBACK_LOG = "nayatsu-generation.debug-fallback-log";

	private NayatsuGenerationConfig() { }

	static boolean antiUTurnGuardEnabled(Main main) {
		return generationEnabled(main.getAConfig().get(ENABLED)) &&
				booleanValue(main.getAConfig().get(ANTI_U_TURN_ENABLED), true);
	}

	static boolean generationEnabled(Object raw) {
		return booleanValue(raw, false);
	}

	static boolean debugFallbackLogEnabled(Main main) {
		return booleanValue(main.getAConfig().get(DEBUG_FALLBACK_LOG), false);
	}

	static boolean booleanValue(Object raw, boolean fallback) {
		if(raw instanceof Boolean) {
			return (Boolean) raw;
		}
		if(raw instanceof String) {
			return Boolean.parseBoolean((String) raw);
		}
		return fallback;
	}
}
