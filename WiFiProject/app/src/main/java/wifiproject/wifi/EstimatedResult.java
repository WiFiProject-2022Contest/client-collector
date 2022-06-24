package wifilocation.wifi;

import com.google.gson.annotations.SerializedName;

public class EstimatedResult {
    String building;
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
    @SerializedName("k")
    int K;
    @SerializedName("threshold")
    int threshold;
    StringBuilder estimateReason;

    public EstimatedResult() {
        this.building = null;
        this.ssid = null;
        this.positionRealX = 0;
        this.positionRealY = 0;
        this.positionEstimatedX = 0;
        this.positionEstimatedY = 0;
        this.uuid = null;
        this.K = -1;
        this.threshold = -1;
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
        this.K = estimatedResult.getK();
        this.threshold = estimatedResult.getThreshold();
        this.estimateReason = estimatedResult.getEstimateReason();
    }

    public EstimatedResult(String building, String ssid, String uuid) {
        this();

        this.building = building;
        this.ssid = ssid;
        this.uuid = uuid;
    }

    public EstimatedResult(String building, String ssid, String uuid, int K, int threshold) {
        this(building, ssid, uuid);

        this.K = K;
        this.threshold = threshold;
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

    public StringBuilder getEstimateReason() {
        return this.estimateReason;
    }

    public void setEstimateReason(StringBuilder estimateReason) {
        this.estimateReason = estimateReason;
    }
}
