package wifilocation.wifi.barcode;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import wifilocation.wifi.MainActivity;
import wifilocation.wifi.R;
import wifilocation.wifi.customviews.SpotImageView;
import wifilocation.wifi.database.DatabaseHelper;

public class BarcodeFragment extends Fragment {

    Context context;
    SpotImageView imageViewMapBarcode;
    EditText editTextBarcodeSerial;
    EditText editTextPosX, editTextPosY;
    Button buttonPush;
    Button buttonSearch;
    RecyclerView recyclerViewBarcode;
    BarcodeAdapter barcodeAdapter = new BarcodeAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_barcode, container, false);

        context = getActivity().getApplicationContext();
        initView(rootView);

        return rootView;
    }

    private void initView(ViewGroup rootView) {
        imageViewMapBarcode = rootView.findViewById(R.id.imageViewMapBarcode);
        editTextBarcodeSerial = rootView.findViewById(R.id.editTextBarcodeSerial);
        editTextPosX = rootView.findViewById(R.id.editTextPosX);
        editTextPosY = rootView.findViewById(R.id.editTextPosY);
        buttonPush = rootView.findViewById(R.id.buttonPush2);
        buttonSearch = rootView.findViewById(R.id.buttonSearch2);
        recyclerViewBarcode = rootView.findViewById(R.id.RecyclerViewBarcode);

        switch (MainActivity.building) {
            case "Library5F":
                imageViewMapBarcode.setImage(ImageSource.resource(R.drawable.skku_library_5f));
                break;
            case "Library3F":
                imageViewMapBarcode.setImage(ImageSource.resource(R.drawable.skku_library_3f));
                break;
            case "WiFiLocation3F":
                imageViewMapBarcode.setImage(ImageSource.resource(R.drawable.wifilocation_gimpo_3f_room_temp_mezzanine_bottom));
                break;
            default:
                break;
        }

        imageViewMapBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (imageViewMapBarcode.isReady()) {
                    PointF s_coord = imageViewMapBarcode.getCenter();
                    PointF meter_coord = imageViewMapBarcode.sourceToMeter(s_coord);

                    editTextPosX.setText(String.valueOf(meter_coord.x));
                    editTextPosY.setText(String.valueOf(meter_coord.y));
                }
                return false;
            }
        });

        buttonPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Barcode> items = new ArrayList<>();
                try {
                    items.add(new Barcode(editTextBarcodeSerial.getText().toString(),
                            Float.parseFloat(editTextPosX.getText().toString()),
                            Float.parseFloat(editTextPosY.getText().toString()),
                            new Date()));
                } catch (Exception e) {
                    Toast.makeText(context, "올바른 데이터 입력 필요", Toast.LENGTH_SHORT).show();
                    return;
                }
                pushLocal(items);
            }
        });

        recyclerViewBarcode.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerViewBarcode.setAdapter(barcodeAdapter);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Barcode> items = searchLocal();
                barcodeAdapter.setItems(items);
                recyclerViewBarcode.setAdapter(barcodeAdapter);
                imageViewMapBarcode.setSpot(items);

                Toast.makeText(context, "로컬 서치 완료", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pushLocal(List<Barcode> items) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.insertIntoBarcode(items, 1);
        Toast.makeText(context, "로컬 푸시 완료", Toast.LENGTH_SHORT).show();
    }

    private List<Barcode> searchLocal() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<Barcode> items = dbHelper.searchFromBarcode(null);
        return items;
    }
}