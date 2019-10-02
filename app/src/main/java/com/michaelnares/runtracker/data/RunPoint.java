package com.michaelnares.runtracker.data;

public class RunPoint {
    private double lat, lon;
    private long timestamp;

    public RunPoint(double lat, double lon, long timestamp) {
        this.lat = lat;
        this.lon = lon;
        this.timestamp = timestamp;
    }

    public final double getLatitude() {
        return lat;
    }

    public final double getLongitude() {
        return lon;
    }

    public final long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "latitude = " + lat + ", longitude = " + lon + " and timestamp = " + timestamp;
    }
}
