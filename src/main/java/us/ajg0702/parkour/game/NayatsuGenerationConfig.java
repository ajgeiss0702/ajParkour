package us.ajg0702.parkour.game;

import us.ajg0702.parkour.Main;

final class NayatsuGenerationConfig {

	static final String ENABLED = "nayatsu-generation.enabled";
	static final String RECENT_HISTORY_SIZE = "nayatsu-generation.recent-history-size";
	static final String ANTI_U_TURN_ENABLED = "nayatsu-generation.anti-u-turn.enabled";
	static final String DEBUG_FALLBACK_LOG = "nayatsu-generation.debug-fallback-log";

	private NayatsuGenerationConfig() { }

	static boolean antiUTurnGuardEnabled(Main main) {
		return generationEnabled(main.getAConfig().get(ENABLED)) &&
				booleanValue(main.getAConfig().get(ANTI_U_TURN_ENABLED), true);
	}

	static boolean generationEnabled(Object raw) {
		return booleanValue(raw, true);
	}

	static boolean debugFallbackLogEnabled(Main main) {
		return booleanValue(main.getAConfig().get(DEBUG_FALLBACK_LOG), false) ||
				booleanValue(main.getAConfig().get("nayatsu-generation.telemetry.enabled"), true);
	}

	static int recentHistorySize(Main main) {
		return intValue(main.getAConfig().get(RECENT_HISTORY_SIZE), 5);
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

	static int intValue(Object raw, int fallback) {
		if(raw instanceof Number) {
			return ((Number) raw).intValue();
		}
		if(raw instanceof String) {
			try {
				return Integer.parseInt((String) raw);
			} catch(NumberFormatException ignored) { }
		}
		return fallback;
	}
}
