package wifilocation.wifi;

import java.util.HashMap;
import java.util.Map;

public class RecordPoint {
    private double[] location;
    private Map<String, Integer> RSSI;

    public RecordPoint() {
        this.location = new double[2];
        this.location[0] = 0;
        this.location[1] = 0;
        RSSI = new HashMap<>();
    }

    public RecordPoint(double[] location) {
        this.location = location;
        RSSI = new HashMap<>();
    }

    public RecordPoint(double[] location, Map<String, Integer> RSSI) {
        this.location = location;
        this.RSSI = RSSI;
    }

    public double[] getLocation() {
        return this.location;
    }

    public void setLocation(double[] location) {
        this.location = location;
    }

    public Map<String, Integer> getRSSI() {
        return this.RSSI;
    }

    public void setRSSI(Map<String, Integer> RSSI) {
        this.RSSI = RSSI;
    }
}
