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
    @GET("/ap")
    Call<List<WiFiItem>> getData(@Query("pos_x") int x, @Query("pos_y") int  y);

    @POST("/rssi")
    Call<JSONObject> postData(@Query("pos_x") int x, @Query("pos_y") int  y, @Body List<WiFiItem> data);
}
