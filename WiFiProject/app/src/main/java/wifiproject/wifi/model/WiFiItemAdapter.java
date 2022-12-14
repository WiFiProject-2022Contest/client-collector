package wifilocation.wifi.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import wifilocation.wifi.R;

public class WiFiItemAdapter extends RecyclerView.Adapter<WiFiItemAdapter.ViewHolder> {
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
        TextView textview_SSID, textview_BSSID, textview_RSSI, textview_frequency, textview_uuid, textview_pos, textview_method;

        public ViewHolder(View itemView) {
            super(itemView);
            textview_SSID = itemView.findViewById(R.id.textViewSSID);
            textview_BSSID = itemView.findViewById(R.id.textViewBSSID);
            textview_RSSI = itemView.findViewById(R.id.textViewRSSI);
            textview_frequency = itemView.findViewById(R.id.textViewFrequency);
            textview_uuid = itemView.findViewById(R.id.textViewUuid);
            textview_pos = itemView.findViewById(R.id.textViewPos);
            textview_method = itemView.findViewById(R.id.textViewMethod);
        }

        public void setItem(WiFiItem item) {
            textview_SSID.setText("SSID: " + item.getSSID());
            textview_BSSID.setText("BSSID: " + item.getBSSID());
            textview_RSSI.setText("RSSI: " + String.valueOf(item.getRSSI()));
            textview_frequency.setText(String.format("Frequency: %d", item.getFrequency()));
            textview_uuid.setText(String.format("UUID: %s", item.getUuid()));
            textview_pos.setText(String.format("x: %.5f, y: %.5f", item.getX(), item.getY()));
            textview_method.setText("(" + item.getMethod() + ")");
        }
    }
}
