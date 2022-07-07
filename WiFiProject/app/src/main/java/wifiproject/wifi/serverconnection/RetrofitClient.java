package wifilocation.wifi.serverconnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private final static String BASE_URL = "http://test.devsh.kro.kr:8080";
    private static RetrofitAPI retrofit_api;

    public static RetrofitAPI getRetrofitAPI() {
        if (retrofit_api == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new GsonDateFormatAdapter());
            Gson gson = gsonBuilder.create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            retrofit_api = retrofit.create(RetrofitAPI.class);
        }
        return retrofit_api;
    }
}