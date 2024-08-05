package com.mash;

import java.time.LocalDateTime;

public class AccessLogEntry {
    private LocalDateTime timestamp;
    private String statusCode;
    private double responseTime;

    public AccessLogEntry(LocalDateTime timestamp, String statusCode, double responseTime) {
        this.timestamp = timestamp;
        this.statusCode = statusCode;
        this.responseTime = responseTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public double getResponseTime() {
        return responseTime;
    }
}
