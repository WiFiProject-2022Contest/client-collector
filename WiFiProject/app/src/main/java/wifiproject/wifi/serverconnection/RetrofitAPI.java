package wifilocation.wifi.serverconnection;

import java.util.List;

import wifilocation.wifi.barcode.Barcode;
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
    Call<List<WiFiItem>> getDataWiFiItem(@Query("building") String building, @Query("SSID") String ssid, @Query("pos_x") Float x, @Query("pos_y") Float y, @Query("from") String from, @Query("to") String to);

    @POST("/rssi")
    Call<PushResultModel> postDataWiFiItem(@Body List<WiFiItem> data);

    @GET("/fingerprint")
    Call<List<EstimatedResult>> getDataEstimateResult(@Query("from") String from, @Query("to") String to);

    @POST("/fingerprint")
    Call<PushResultModel> postDataEstimatedResult(@Body List<EstimatedResult> data);

    @GET("/barcode")
    Call<List<Barcode>> getDataBarcode();

    @POST("/barcode")
    Call<PushResultModel> postDataBarcode(@Body List<Barcode> data);
}
