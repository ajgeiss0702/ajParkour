package us.ajg0702.parkour.top;

public class TopEntry {
    private final int position;
    private final String name;
    private final int score;
    private final int time;
    public TopEntry(int position, String name, int score, int time) {
        this.position = position;
        this.name = name;
        this.score = score;
        this.time = time;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getTime() {
        return time;
    }

}
