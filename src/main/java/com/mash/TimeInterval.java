package com.mash;

import java.time.LocalDateTime;
import java.util.Objects;

public class TimeInterval implements Comparable<TimeInterval> {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double availability;
    private long failures;
    private long totalRequests;

    public TimeInterval(LocalDateTime startTime, LocalDateTime endTime, double availability, long failures, long totalRequests) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.availability = availability;
        this.failures = failures;
        this.totalRequests = totalRequests;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getAvailability() {
        return availability;
    }

    public long getFailures() {
        return failures;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    @Override
    public int compareTo(TimeInterval otherTimeInterval) {
        return startTime.compareTo(otherTimeInterval.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeInterval that = (TimeInterval) o;
        return Double.compare(availability, that.availability) == 0 && failures == that.failures && totalRequests == that.totalRequests && Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, availability, failures, totalRequests);
    }
}
