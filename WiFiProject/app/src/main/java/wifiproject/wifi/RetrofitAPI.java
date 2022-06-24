package wifilocation.wifi;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    Call<PushResultModel> postData(@Query("pos_x") float pos_x, @Query("pos_y") float pos_y, @Query("est_x") float est_x, @Query("est_y") float est_y, @Query("UUID") String uuid, @Query("k") int K, @Query("threshold") int threshold);
}
