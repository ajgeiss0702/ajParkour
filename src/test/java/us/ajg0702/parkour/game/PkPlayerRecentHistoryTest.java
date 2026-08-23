package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PkPlayerRecentHistoryTest {

    private final World world = mock(World.class);

    @Test
    void recentHistoryRetainsLastFiveConsumedJumpsInOrder() {
        Deque<Location> history = new ArrayDeque<>();

        for(int i = 0; i < 7; i++) {
            PkPlayer.recordRecentJump(history, new Location(world, i, 64, 0), 5);
        }

        List<Location> snapshot = new ArrayList<>(history);
        assertEquals(5, snapshot.size());
        assertEquals(2, snapshot.get(0).getBlockX());
        assertEquals(6, snapshot.get(4).getBlockX());
    }

    @Test
    void recentHistoryStoresLocationCopies() {
        Deque<Location> history = new ArrayDeque<>();
        Location consumed = new Location(world, 10, 64, 10);

        PkPlayer.recordRecentJump(history, consumed, 5);
        consumed.setX(99);

        assertEquals(10, history.peekFirst().getBlockX());
    }

    @Test
    void recentHistoryCanBeClearedOnSessionEnd() {
        Deque<Location> history = new ArrayDeque<>();
        PkPlayer.recordRecentJump(history, new Location(world, 10, 64, 10), 5);

        PkPlayer.clearRecentJumpHistory(history);

        assertEquals(0, history.size());
    }
}
