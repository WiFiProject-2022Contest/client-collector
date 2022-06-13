package wifilocation.wifi;

import com.google.gson.annotations.SerializedName;

public class WiFiDataModel {
    // https://www.notion.so/f61f9f892b254d1d8dbdf2f101ef5fb2?v=e9a507783cde4e3ca3410d92fd295e48
    // TODO: @Expose 추가 고려
    @SerializedName("SSID")
    String SSID;
    @SerializedName("BSSID")
    String BSSID;
    @SerializedName("frequency")
    int freq;
    @SerializedName("level")
    int RSSI;

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

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }
}
