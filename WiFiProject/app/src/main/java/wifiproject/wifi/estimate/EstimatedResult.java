package wifilocation.wifi.estimate;

import com.google.gson.annotations.SerializedName;

public class EstimatedResult {
    @SerializedName("building")
    String building;
    @SerializedName("SSID")
    String ssid;
    @SerializedName("pos_x")
    double positionRealX;
    @SerializedName("pos_y")
    double positionRealY;
    @SerializedName("est_x")
    double positionEstimatedX;
    @SerializedName("est_y")
    double positionEstimatedY;
    @SerializedName("uuid")
    String uuid;
    @SerializedName("method")
    String method;
    @SerializedName("k")
    int K;
    @SerializedName("threshold")
    int threshold;
    @SerializedName("algorithmVersion")
    int algorithmVersion;
    @SerializedName("date")
    long date;
    StringBuilder estimateReason;

    public EstimatedResult() {
        this.building = null;
        this.ssid = null;
        this.positionRealX = 0;
        this.positionRealY = 0;
        this.positionEstimatedX = 0;
        this.positionEstimatedY = 0;
        this.uuid = null;
        this.method = null;
        this.K = -1;
        this.threshold = -1;
        this.algorithmVersion = 0;
        this.date = System.currentTimeMillis();
        this.estimateReason = new StringBuilder();
    }

    public EstimatedResult(EstimatedResult estimatedResult) {
        this.building = estimatedResult.getBuilding();
        this.ssid = estimatedResult.getSsid();
        this.positionRealX = estimatedResult.getPositionRealX();
        this.positionRealY = estimatedResult.getPositionRealY();
        this.positionEstimatedX = estimatedResult.getPositionEstimatedX();
        this.positionEstimatedY = estimatedResult.getPositionEstimatedY();
        this.uuid = estimatedResult.getUuid();
        this.method = estimatedResult.getMethod();
        this.K = estimatedResult.getK();
        this.threshold = estimatedResult.getThreshold();
        this.algorithmVersion = estimatedResult.getAlgorithmVersion();
        this.date = estimatedResult.getDate();
        this.estimateReason = estimatedResult.getEstimateReason();
    }

    public EstimatedResult(String building, String ssid, String uuid) {
        this();

        this.building = building;
        this.ssid = ssid;
        this.uuid = uuid;
    }

    public EstimatedResult(String building, String ssid, String uuid, String method, int K, int threshold, int algorithmVersion) {
        this(building, ssid, uuid);

        this.method = method;
        this.K = K;
        this.threshold = threshold;
        this.algorithmVersion = algorithmVersion;
    }

    public EstimatedResult(String building, String ssid, String uuid, String method, int K, int threshold, int algorithmVersion, long date) {
        this(building, ssid, uuid, method, K, threshold, algorithmVersion);

        this.date = date;
    }

    public EstimatedResult(String building, String ssid, double positionRealX, double positionRealY, double positionEstimatedX, double positionEstimatedY,
                            String uuid, String method, int K, int threshold, int algorithmVersion, long date) {
        this();

        this.building = building;
        this.ssid = ssid;
        this.positionRealX = positionRealX;
        this.positionRealY = positionRealY;
        this.positionEstimatedX = positionEstimatedX;
        this.positionEstimatedY = positionEstimatedY;
        this.uuid = uuid;
        this.method = method;
        this.K = K;
        this.threshold = threshold;
        this.algorithmVersion = algorithmVersion;
        this.date = date;
    }

    public String getBuilding() {
        return this.building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public double getPositionRealX() {
        return this.positionRealX;
    }

    public void setPositionRealX(double positionRealX) {
        this.positionRealX = positionRealX;
    }

    public double getPositionRealY() {
        return this.positionRealY;
    }

    public void setPositionRealY(double positionRealY) {
        this.positionRealY = positionRealY;
    }

    public double getPositionEstimatedX() {
        return this.positionEstimatedX;
    }

    public void setPositionEstimatedX(double positionEstimatedX) {
        this.positionEstimatedX = positionEstimatedX;
    }

    public double getPositionEstimatedY() {
        return this.positionEstimatedY;
    }

    public void setPositionEstimatedY(double positionEstimatedY) {
        this.positionEstimatedY = positionEstimatedY;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getK() {
        return this.K;
    }

    public void setK(int K) {
        this.K = K;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getAlgorithmVersion() {
        return this.algorithmVersion;
    }

    public void setAlgorithmVersion(int algorithmVersion) {
        this.algorithmVersion = algorithmVersion;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate() {
        this.date = System.currentTimeMillis();
    }

    public void setDate(long date) {
        this.date = date;
    }

    public StringBuilder getEstimateReason() {
        return this.estimateReason;
    }

    public void setEstimateReason(StringBuilder estimateReason) {
        this.estimateReason = estimateReason;
    }
}
