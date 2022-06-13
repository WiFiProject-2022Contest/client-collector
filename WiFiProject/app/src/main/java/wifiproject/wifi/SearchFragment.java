package wifilocation.wifi;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    RecyclerView recyclerview_searched;
    WiFiItemAdapter wifiitem_adapter = new WiFiItemAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        Context context = getActivity().getApplicationContext();

        recyclerview_searched = rootview.findViewById(R.id.RecyclerViewSearched);
        recyclerview_searched.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        SearchView searchview = rootview.findViewById(R.id.searchView);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
                String[] pos = s.split(",");
                retrofit_api.getData(Integer.parseInt(pos[0]), Integer.parseInt(pos[1])).enqueue(new Callback<List<WiFiItem>>() {
                    @Override
                    public void onResponse(Call<List<WiFiItem>> call, Response<List<WiFiItem>> response) {
                        ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();
                        items.addAll(response.body());
                        wifiitem_adapter.setItems(items);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerview_searched.setAdapter(wifiitem_adapter);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<WiFiItem>> call, Throwable t) {

                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return rootview;
    }
}