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

public class SearchFragment extends Fragment {
    RecyclerView recyclerview_searched;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        Context context = getActivity().getApplicationContext();

        recyclerview_searched = rootview.findViewById(R.id.RecyclerViewSearched);
        recyclerview_searched.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        SearchView searchview= rootview.findViewById(R.id.searchView);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast.makeText(context, "구현되지 않음", Toast.LENGTH_SHORT).show();
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