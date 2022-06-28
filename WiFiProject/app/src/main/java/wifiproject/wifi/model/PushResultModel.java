package wifilocation.wifi.model;

import com.google.gson.annotations.SerializedName;

public class PushResultModel {
    @SerializedName("success")
    String success;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
}
