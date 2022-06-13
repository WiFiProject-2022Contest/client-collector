package wifilocation.wifi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WiFiItemAdapter extends RecyclerView.Adapter<WiFiItemAdapter.ViewHolder>{
    ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.wifi_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WiFiItem item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(WiFiItem item) {
        items.add(item);
    }

    public void setItems(ArrayList<WiFiItem> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textview_BSSID, textview_RSSI;

        public ViewHolder(View itemView) {
            super(itemView);
            textview_BSSID = itemView.findViewById(R.id.textViewBSSID);
            textview_RSSI = itemView.findViewById(R.id.textViewRSSI);
        }

        public void setItem(WiFiItem item) {
            textview_BSSID.setText("BSSID: " + item.getBSSID());
            textview_RSSI.setText("RSSI: " + String.valueOf(item.getRSSI()));
        }
    }
}
