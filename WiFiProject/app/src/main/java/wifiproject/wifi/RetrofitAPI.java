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
    Call<List<WiFiItem>> getData(@Query("pos_x") float x, @Query("pos_y") float  y);

    @POST("/rssi")
    Call<PushResultModel> postData(@Query("pos_x") float x, @Query("pos_y") float  y, @Body List<WiFiItem> data);
}
