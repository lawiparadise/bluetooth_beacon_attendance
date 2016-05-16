package com.law.blueinnofora.service;

public interface RssiFilter {
    public void addMeasurement(Integer rssi);
    public boolean noMeasurementsAvailable();
    public double calculateRssi();
}
