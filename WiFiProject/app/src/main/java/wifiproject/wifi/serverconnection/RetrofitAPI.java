package wifilocation.wifi.serverconnection;

import java.util.List;

import wifilocation.wifi.estimate.EstimatedResult;
import wifilocation.wifi.model.PushResultModel;
import wifilocation.wifi.model.WiFiItem;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitAPI {
    @GET("/rssi")
    Call<List<WiFiItem>> getData(@Query("building") String building, @Query("SSID") String ssid, @Query("pos_x") Float x, @Query("pos_y") Float y, @Query("from") String from, @Query("to") String to);

    @POST("/rssi")
    Call<PushResultModel> postData(@Query("pos_x") float x, @Query("pos_y") float y, @Body List<WiFiItem> data);

    @POST("/fingerprint")
    Call<PushResultModel> postData(@Body EstimatedResult estimatedResult);
}
