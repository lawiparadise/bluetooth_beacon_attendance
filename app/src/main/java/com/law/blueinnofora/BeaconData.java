package com.law.blueinnofora;

/**
 * Created by gd2 on 2015-07-02.
 * 비콘과 롼련된 서버사이드 데이타
 */
public interface BeaconData {
    public Double getLatitude();
    public void setLatitude(Double latitude);
    public void setLongitude(Double longitude);
    public Double getLongitude();
    public String get(String key);
    public void set(String key, String value);
    public void sync(BeaconDataNotifier notifier);
    public boolean isDirty();
}
