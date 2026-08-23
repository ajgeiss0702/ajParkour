package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PkJumpGenerationBenchmarkTest {

    @Test
    void recordsMinimalCandidateGenerationBaseline() throws IOException {
        World world = mock(World.class);
        int samples = 10000;
        List<Long> durations = new ArrayList<>(samples);
        int candidateCount = 0;

        for(int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            PkJump.JumpShape shape = PkJump.shapeForDistance(3 + (i % 3));
            List<Location> candidates = PkJump.candidateLocations(world, 0, 64, 0, shape.distance, shape.maxY);
            long duration = System.nanoTime() - start;
            durations.add(duration);
            candidateCount = candidates.size();
        }

        Collections.sort(durations);
        assertEquals(13, candidateCount);

        Path report = Paths.get("build", "reports", "nayatsu-parkour", "slice-1-generation-benchmark.md");
        Files.createDirectories(report.getParent());
        Files.write(report, benchmarkReport(samples, candidateCount, durations).getBytes(StandardCharsets.UTF_8));
    }

    private static String benchmarkReport(int samples, int candidateCount, List<Long> durations) {
        return "# Slice 1 Generation Baseline\n\n" +
                "- scope: candidate shape + candidate location generation seam\n" +
                "- samples: " + samples + "\n" +
                "- candidate_count: " + candidateCount + "\n" +
                "- p50_ns: " + percentile(durations, 0.50) + "\n" +
                "- p95_ns: " + percentile(durations, 0.95) + "\n" +
                "- p99_ns: " + percentile(durations, 0.99) + "\n" +
                "- max_ns: " + durations.get(durations.size() - 1) + "\n";
    }

    private static long percentile(List<Long> values, double percentile) {
        int index = (int) Math.ceil(percentile * values.size()) - 1;
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }
}
