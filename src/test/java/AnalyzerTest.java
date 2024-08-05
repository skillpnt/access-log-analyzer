import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mash.Analyzer;
import com.mash.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnalyzerTest {

    private static final String testLogData =
            "192.168.32.181 - - [06/08/2024:10:00:01 +0000] \"PUT /rest/v1.4/documents?zone=default&_rid=test HTTP/1.1\" 500 2 44.0 \"-\" \"@list-item-updater\" prio:0\n" +
            "192.168.32.181 - - [06/08/2024:10:00:02 +0000] \"PUT /rest/v1.4/documents?zone=default&_rid=test HTTP/1.1\" 200 2 23.0 \"-\" \"@list-item-updater\" prio:0\n" +
            "192.168.32.181 - - [06/08/2024:10:00:03 +0000] \"PUT /rest/v1.4/documents?zone=default&_rid=test HTTP/1.1\" 500 2 50.0 \"-\" \"@list-item-updater\" prio:0\n";

    @Test
    public void testCalculateAvailability() {
        assertEquals(95.0, Analyzer.calculateAvailability(100, 5), 0.001);

        assertEquals(0.0, Analyzer.calculateAvailability(0, 0), 0.001);

        assertEquals(100.0, Analyzer.calculateAvailability(100, 0), 0.001);
    }

    @Test
    public void testMergeTimeIntervals() {
        List<TimeInterval> intervals = new ArrayList<>();

        intervals.add(new TimeInterval(
                LocalDateTime.parse("2024-08-06T10:00:00"),
                LocalDateTime.parse("2024-08-06T10:05:00"),
                95.0, 5, 100
        ));

        intervals.add(new TimeInterval(
                LocalDateTime.parse("2024-08-06T10:05:00"),
                LocalDateTime.parse("2024-08-06T10:10:00"),
                90.0, 10, 100
        ));

        List<TimeInterval> mergedIntervals = Analyzer.mergeTimeIntervals(intervals);

        assertEquals(1, mergedIntervals.size());
        TimeInterval merged = mergedIntervals.get(0);
        assertEquals(LocalDateTime.parse("2024-08-06T10:00:00"), merged.getStartTime());
        assertEquals(LocalDateTime.parse("2024-08-06T10:10:00"), merged.getEndTime());
        assertEquals(92.5, merged.getAvailability(), 0.001);
        assertEquals(15, merged.getFailures());
        assertEquals(200, merged.getTotalRequests());
    }

    @Test
    public void testAnalyzeFailures() throws Exception {

        StringReader stringReader = new StringReader(testLogData);
        List<TimeInterval> intervals = Analyzer.analyzeFailures(stringReader, 99.9, 45);

        assertEquals(1, intervals.size());
        TimeInterval interval = intervals.get(0);
        assertEquals(LocalDateTime.parse("2024-08-06T10:00:01"), interval.getStartTime());
        assertEquals(LocalDateTime.parse("2024-08-06T10:00:03"), interval.getEndTime());
        assertEquals(33.33, interval.getAvailability(), 0.01);
    }

    @Test
    public void testUsingAccessLogFile() throws Exception {

        try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("access.log")))) {

            List<TimeInterval> intervals = Analyzer.analyzeFailures(reader, 99.9, 45);

            assertEquals(8, intervals.size());

            TimeInterval interval = intervals.get(0);
            assertEquals(LocalDateTime.parse("2017-06-14T16:47:02"), interval.getStartTime());
            assertEquals(LocalDateTime.parse("2017-06-14T16:47:28"), interval.getEndTime());
            assertEquals(73.79, interval.getAvailability(), 0.01);

            interval = intervals.get(intervals.size() - 1);
            assertEquals(LocalDateTime.parse("2017-06-14T16:48:50"), interval.getStartTime());
            assertEquals(LocalDateTime.parse("2017-06-14T16:48:52"), interval.getEndTime());
            assertEquals(93.12, interval.getAvailability(), 0.01);
        }
    }
}
