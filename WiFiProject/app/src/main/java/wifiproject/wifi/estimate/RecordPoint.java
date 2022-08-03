package wifilocation.wifi.estimate;

import java.util.HashMap;
import java.util.Map;

public class RecordPoint {
    private double[] location;
    private Map<String, String> method;
    private Map<String, Integer> RSSI;
    private Map<String, Integer> frequency;

    public RecordPoint() {
        this.location = new double[2];
        this.location[0] = 0;
        this.location[1] = 0;
        method = new HashMap<>();
        RSSI = new HashMap<>();
        frequency = new HashMap<>();
    }

    public RecordPoint(double[] location) {
        this.location = location;
        method = new HashMap<>();
        RSSI = new HashMap<>();
        frequency = new HashMap<>();
    }

    public RecordPoint(double[] location, Map<String, String> method, Map<String, Integer> RSSI, Map<String, Integer> frequency) {
        this.location = location;
        this.method = method;
        this.RSSI = RSSI;
        this.frequency = frequency;
    }

    public double[] getLocation() {
        return this.location;
    }

    public void setLocation(double[] location) {
        this.location = location;
    }

    public Map<String, String> getMethod() {
        return this.method;
    }

    public void setMethod(Map<String, String> method) {
        this.method = method;
    }

    public Map<String, Integer> getRSSI() {
        return this.RSSI;
    }

    public void setRSSI(Map<String, Integer> RSSI) {
        this.RSSI = RSSI;
    }

    public Map<String, Integer> getFrequency() {
        return this.frequency;
    }

    public void setFrequency(Map<String, Integer> frequency) {
        this.frequency = frequency;
    }
}
