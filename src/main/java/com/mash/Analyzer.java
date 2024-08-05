package com.mash;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Analyzer {

    public static void main(String[] args) throws IOException {
        double minAvailability = 0;
        long maxAnswerTime = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-u")) {
                minAvailability = Double.parseDouble(args[i + 1]);
            } else if (args[i].equals("-t")) {
                maxAnswerTime = Long.parseLong(args[i + 1]);
            }
        }


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        List<TimeInterval> timeIntervals = analyzeFailures(reader, minAvailability, maxAnswerTime);

        for (TimeInterval interval : timeIntervals) {
            System.out.println(interval.getStartTime() + " " + interval.getEndTime() + " " + interval.getAvailability());
        }
    }

    public static List<TimeInterval> analyzeFailures(Reader reader, double minAvailability, long maxResponseTime) throws IOException {
        List<TimeInterval> timeIntervals = new ArrayList<>();
        LocalDateTime currentIntervalStart = null;
        long failCount = 0;
        long totalCount = 0;

        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            AccessLogEntry logEntry = parseLogEntry(line);

            if (logEntry.getStatusCode().startsWith("5") || logEntry.getResponseTime() > maxResponseTime) {
                failCount++;
            }
            totalCount++;

            if (currentIntervalStart == null) {
                currentIntervalStart = logEntry.getTimestamp();
            } else if (logEntry.getTimestamp().isEqual(currentIntervalStart.plusSeconds(1)) || logEntry.getTimestamp().isAfter(currentIntervalStart.plusSeconds(1))) {
                double availability = calculateAvailability(totalCount, failCount);
                if (availability < minAvailability) {
                    timeIntervals.add(new TimeInterval(currentIntervalStart, logEntry.getTimestamp(), availability, failCount, totalCount));
                }
                currentIntervalStart = logEntry.getTimestamp();
                failCount = 0;
                totalCount = 0;
            }
        }

        Collections.sort(timeIntervals);
        return mergeTimeIntervals(timeIntervals);
    }

    public static List<TimeInterval> mergeTimeIntervals(List<TimeInterval> intervals) {
        if (intervals.isEmpty()) {
            return intervals;
        }

        List<TimeInterval> mergedIntervals = new ArrayList<>();
        TimeInterval prev = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            TimeInterval curr = intervals.get(i);

            if (prev.getEndTime().equals(curr.getStartTime())) {
                long totalFailures = prev.getFailures() + curr.getFailures();
                long totalRequests = prev.getTotalRequests() + curr.getTotalRequests();
                double availability = calculateAvailability(totalRequests, totalFailures);
                prev = new TimeInterval(prev.getStartTime(), curr.getEndTime(), availability, totalFailures, totalRequests);
            } else {
                mergedIntervals.add(prev);
                prev = curr;
            }
        }

        mergedIntervals.add(prev);

        return mergedIntervals;
    }

    private static AccessLogEntry parseLogEntry(String line) {
        String[] parts = line.split(" ");
        String dateString = parts[3].substring(1);
        LocalDateTime timestamp = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss"));
        String statusCode = parts[8];
        double responseTime = Double.parseDouble(parts[10]);
        return new AccessLogEntry(timestamp, statusCode, responseTime);
    }

    public static double calculateAvailability(long totalRequests, long failures) {
        if (totalRequests == 0) {
            return 0;
        }
        return (double) (totalRequests - failures) / totalRequests * 100;
    }
}