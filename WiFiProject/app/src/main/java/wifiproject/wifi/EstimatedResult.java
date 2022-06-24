package wifilocation.wifi;

public class EstimatedResult {
    String building;
    String ssid;
    double[] positionReal;
    double[] positionEstimated;
    String uuid;
    int K;
    int threshold;
    StringBuilder estimateReason;

    public EstimatedResult() {
        this.building = null;
        this.ssid = null;
        this.positionReal = new double[2];
        this.positionEstimated = new double[2];
        this.uuid = null;
        this.K = -1;
        this.threshold = -1;
        this.estimateReason = new StringBuilder();
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

    public double[] getPositionReal() {
        return this.positionReal;
    }

    public void setPositionReal(double[] positionReal) {
        this.positionReal = positionReal;
    }

    public double[] getPositionEstimated() {
        return this.positionEstimated;
    }

    public void setPositionEstimated(double[] positionEstimated) {
        this.positionEstimated = positionEstimated;
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
