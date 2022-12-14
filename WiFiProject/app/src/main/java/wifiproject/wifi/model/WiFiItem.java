package wifilocation.wifi.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class WiFiItem {
    // https://www.notion.so/f61f9f892b254d1d8dbdf2f101ef5fb2?v=e9a507783cde4e3ca3410d92fd295e48
    // TODO: @Expose 추가 고려
    @SerializedName("pos_x")
    Float x;
    @SerializedName("pos_y")
    Float y;
    @SerializedName("SSID")
    String SSID;
    @SerializedName("BSSID")
    String BSSID;
    @SerializedName("level")
    int RSSI;
    @SerializedName("frequency")
    int frequency;
    @SerializedName("uuid")
    String uuid;
    @SerializedName("building")
    String building;
    @SerializedName("method")
    String method;
    @SerializedName("date")
    Date date;

    public WiFiItem(float x, float y, String SSID, String BSSID, int RSSI, int frequency, String uuid, String building, String method) {
        this.x = x;
        this.y = y;
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.RSSI = RSSI;
        this.frequency = frequency;
        this.uuid = uuid;
        this.building = building;
        this.method = method;
        this.date = new Date(System.currentTimeMillis());
    }

    public WiFiItem(Float x, Float y, String SSID, String BSSID, int RSSI, int frequency, String uuid, String building, String method, long date) {
        this(x, y, SSID, BSSID, RSSI, frequency, uuid, building, method);
        this.date = new Date(date);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
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

    public int getFrequency() { return frequency; }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String building) {
        this.method = method;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "WiFiItem{" +
                "x=" + x +
                ", y=" + y +
                ", SSID='" + SSID + '\'' +
                ", BSSID='" + BSSID + '\'' +
                ", RSSI=" + RSSI +
                ", frequency=" + frequency +
                ", uuid='" + uuid + '\'' +
                ", building='" + building + '\'' +
                ", method='" + method + '\'' +
                ", date.getTime()=" + date.getTime() +
                '}';
    }
}
