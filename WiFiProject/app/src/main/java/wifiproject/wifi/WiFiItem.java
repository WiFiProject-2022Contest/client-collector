package wifilocation.wifi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WiFiItem {
    // https://www.notion.so/f61f9f892b254d1d8dbdf2f101ef5fb2?v=e9a507783cde4e3ca3410d92fd295e48
    // TODO: @Expose 추가 고려
    @SerializedName("SSID")
    String SSID;
    @SerializedName("BSSID")
    String BSSID;
    @SerializedName("level")
    int RSSI;
    @SerializedName("frequency")
    int bandwidth;
    @SerializedName("timestamp")
    int timestamp;

    public WiFiItem(String SSID, String BSSID, int RSSI, int bandwidth, int timestamp) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.RSSI = RSSI;
        this.bandwidth = bandwidth;
        this.timestamp = timestamp;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
