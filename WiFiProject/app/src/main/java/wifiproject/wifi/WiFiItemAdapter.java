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

    public ArrayList<WiFiItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<WiFiItem> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textview_SSID, textview_BSSID, textview_RSSI, textview_bandwidth, textview_pos;

        public ViewHolder(View itemView) {
            super(itemView);
            textview_SSID = itemView.findViewById(R.id.textViewSSID);
            textview_BSSID = itemView.findViewById(R.id.textViewBSSID);
            textview_RSSI = itemView.findViewById(R.id.textViewRSSI);
            textview_bandwidth = itemView.findViewById(R.id.textViewBandwidth);
            textview_pos = itemView.findViewById(R.id.textViewPos);
        }

        public void setItem(WiFiItem item) {
            textview_SSID.setText("SSID: " + item.getSSID());
            textview_BSSID.setText("BSSID: " + item.getBSSID());
            textview_RSSI.setText("RSSI: " + String.valueOf(item.getRSSI()));
            textview_bandwidth.setText(String.format("Bandwidth: %d", item.getBandwidth()));
            textview_pos.setText(String.format("x: %.5f, y: %.5f", item.getX(), item.getY()));
        }
    }
}
