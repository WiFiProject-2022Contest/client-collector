package wifilocation.wifi;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstimateFragment extends Fragment {
    Button buttonEstimate;
    TextView textResultEstimate;
    List<WiFiItem> databaseData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_estimate, container, false);
        buttonEstimate = rootView.findViewById(R.id.buttonEstimate);
        textResultEstimate = rootView.findViewById(R.id.textResultEstimate);

        // DB 전체 다 받아오기
        RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
        retrofit_api.getData(-1, -1).enqueue(new Callback<List<WiFiItem>>() {
            @Override
            public void onResponse(Call<List<WiFiItem>> call, Response<List<WiFiItem>> response) {
                databaseData = response.body();
            }

            @Override
            public void onFailure(Call<List<WiFiItem>> call, Throwable t) {

            }
        });

        buttonEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });

        return rootView;
    }
}