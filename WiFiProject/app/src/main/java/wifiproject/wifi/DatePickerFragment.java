package wifilocation.wifi;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar calender = Calendar.getInstance();
        int y = calender.get(Calendar.YEAR);
        int m = calender.get(Calendar.MONTH);
        int d = calender.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, y, m, d);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
        MainActivity activity = (MainActivity) getActivity();
        activity.search_fragment.setDateText(String.format("%d%02d%02d", y, m, d));
    }
}