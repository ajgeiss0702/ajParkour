package us.ajg0702.parkour.game;

/**
 * The difficulty of a {@link us.ajg0702.parkour.game.PkArea PkArea}.
 * @author ajgeiss0702
 *
 */
public enum Difficulty {
	EASY, MEDIUM, HARD, EXPERT, BALANCED;

	private int min = 1;
	private int max = 1;

	public int getMin() {
		return min;
	}
	public int getMax() {
		return max;
	}
	public void setMin(int m) {
		min = m;
	}
	public void setMax(int m) {
		max = m;
	}
}
