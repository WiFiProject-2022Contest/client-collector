package wifilocation.wifi.barcode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wifilocation.wifi.R;
import wifilocation.wifi.model.WiFiItem;
import wifilocation.wifi.model.WiFiItemAdapter;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.ViewHolder> {

    List<Barcode> items = new ArrayList<>();

    @NonNull
    @Override
    public BarcodeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.barcode, parent, false);

        return new BarcodeAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BarcodeAdapter.ViewHolder holder, int position) {
        Barcode item = items.get(position);
        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Barcode item) {
        items.add(item);
    }

    public List<Barcode> getItems() {
        return items;
    }

    public void setItems(List<Barcode> items) {
        this.items = items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBarcodeSerial;
        TextView textViewBarcodePos;
        TextView textViewBarcodeDate;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewBarcodeSerial = itemView.findViewById(R.id.textViewBarcodeSerial);
            textViewBarcodePos = itemView.findViewById(R.id.textViewBarcodePos);
            textViewBarcodeDate = itemView.findViewById(R.id.textViewBarcodeDate);
        }

        public void setItem(Barcode item) {
            textViewBarcodeSerial.setText("Serial: " + item.getSerial());
            textViewBarcodePos.setText(String.format("x: %.5f, y: %.5f", item.getPosX(), item.getPosY()));
            textViewBarcodeDate.setText("Date: " + item.getDate().toString());
        }
    }
}
