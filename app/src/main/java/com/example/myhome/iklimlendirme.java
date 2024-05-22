package com.example.myhome;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhome.BluetoothServices.BluetoothService;
import com.seosh817.circularseekbar.CircularSeekBar;
import com.seosh817.circularseekbar.callbacks.OnProgressChangedListener;

import java.util.Arrays;

public class iklimlendirme extends Fragment {

    private static final String TAG = "iklimlendirmeFragment";

    private CircularSeekBar circularSeekBar;
    private Switch switchIklimlendirme;
    private TextView textViewDereceDisplay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iklimlendirme, container, false);

        try {
            Log.d(TAG, "Initializing page elements.");
            pageInitialize(view);

            Log.d(TAG, "Setting up CircularSeekBar listener.");
            circularSeekBar.setOnProgressChangedListener(new OnProgressChangedListener() {
                @Override
                public void onProgressChanged(float progress) {
                    if (switchIklimlendirme.isChecked()) {
                        int displayedProgress = (int) progress + 16;
                        Log.d(TAG, "CircularSeekBar Progress Changed: " + displayedProgress);
                        textViewDereceDisplay.setText(displayedProgress + "°C");
                        sendFormattedData('T', displayedProgress); // Veri göndermeyi burada yapabiliriz
                    }
                }
            });

            circularSeekBar.setProgress(0); // Başlangıç değerini 16 olarak ayarlayın

            Log.d(TAG, "Setting up switch listener.");
            switchIklimlendirme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "Switch State Changed: " + (isChecked ? "ON" : "OFF"));
                updateUIBasedOnSwitchState(isChecked);
                sendFormattedData('I', isChecked ? 1 : 0);
            });

            // Başlangıçta UI'yi Switch durumuna göre ayarlayın
            updateUIBasedOnSwitchState(switchIklimlendirme.isChecked());

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
        }

        return view;
    }

    private void pageInitialize(View view) {
        try {
            Log.d(TAG, "Initializing components.");
            textViewDereceDisplay = view.findViewById(R.id.textViewDereceValue);
            circularSeekBar = view.findViewById(R.id.circular_seek_bar);
            circularSeekBar.setMin(0); // Min değeri 0 olarak ayarlayın, 16 eklenmiş hali gösterilecek
            circularSeekBar.setMax(20 ); // Maksimum değeri 19 olarak ayarlayın, 35 eklenmiş hali gösterilecek
            switchIklimlendirme = view.findViewById(R.id.switchIklimlendirme);
            Log.d(TAG, "Components initialized successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error in pageInitialize", e);
        }
    }

    private void sendFormattedData(char prefix, int value) {
        String data = String.format("<%c,%d", prefix, value);
        int remainingLength = 31 - data.length(); // 31 because the closing '>' will be added
        if (remainingLength > 0) {
            char[] fillChars = new char[remainingLength];
            Arrays.fill(fillChars, '!');
            data += new String(fillChars);
        }
        data += '>'; // Add closing '>'
        BluetoothService.getInstance(getContext()).sendData(data);
        Log.d(TAG, "sendFormattedData: " + data); // Debug için gönderilen veriyi loglayın
    }

    private void updateUIBasedOnSwitchState(boolean isOn) {
        if (isOn) {
            // Assuming setBarColor is not available, setting gradient might not be supported directly
            // Instead, we can set progress color based on the current progress value.
            circularSeekBar.setEnabled(true);
            int displayedProgress = (int) circularSeekBar.getProgress() + 16;
            textViewDereceDisplay.setText(displayedProgress + "°C");
            // You can adjust the color dynamically here based on your logic
        } else {
            circularSeekBar.setEnabled(false);
            textViewDereceDisplay.setText("KAPALI");
            circularSeekBar.setProgressColor(Color.GRAY);
        }
    }
}
