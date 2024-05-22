package com.example.myhome;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhome.BluetoothServices.BluetoothService;

public class AlarmFragment extends Fragment {

    private static final String TAG = "AlarmFragment";
    private boolean buzzerActive = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_fragmnet, container, false);

        // Buzzer aktif olduğunda kullanıcıya uyarı göster
        if (buzzerActive) {
            showBuzzerAlert();
        } else {
            Log.d(TAG, "Buzzer is not active.");
        }

        return view;
    }

    private void deactivateBuzzer() {
        if (buzzerActive) {
            String command = formatCommand("B", 0);
            Log.d(TAG, "Deactivating buzzer with command: " + command);
            BluetoothService.getInstance(getContext()).sendData(command);
            buzzerActive = false;
        } else {
            Log.d(TAG, "Buzzer is already deactivated.");
        }
    }

    private void showBuzzerAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Buzzer Alert")
                .setMessage("The buzzer is active. Do you want to deactivate it?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deactivateBuzzer();
                    if (getActivity() != null) {
                        getActivity().finish(); // Close the activity
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private String formatCommand(String command, int value) {
        // Komutu ! karakteri ile doldurarak 32 karakter uzunluğa tamamla
        String formattedCommand = String.format("<%s,%d>", command, value);
        int remainingLength = 32 - formattedCommand.length();
        if (remainingLength > 0) {
            for (int i = 0; i < remainingLength - 1; i++) { // Sondaki > karakteri için -1
                formattedCommand += "!";
            }
        }
        formattedCommand += ">";
        return formattedCommand;
    }
}
