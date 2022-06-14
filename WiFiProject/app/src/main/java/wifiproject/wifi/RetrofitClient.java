package wifilocation.wifi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private final static String BASE_URL = "http://test.devsh.kro.kr:8080";
    private static RetrofitAPI retrofit_api;

    public static RetrofitAPI getRetrofitAPI() {
        if (retrofit_api == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            retrofit_api = retrofit.create(RetrofitAPI.class);
        }
        return retrofit_api;
    }
}
