package com.example.myhome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhome.BluetoothServices.BluetoothService;

import java.util.Arrays;

public class iklimlendirme extends Fragment {

    private static final String TAG = "iklimlendirmeFragment";

    private SeekBar seekbar;
    private Switch switchIklimlendirme;
    private TextView textViewDereceDisplay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iklimlendirme, container, false);

        try {
            Log.d(TAG, "Initializing page elements.");
            pageInitialize(view);

            Log.d(TAG, "Setting up seekbar listener.");
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.d(TAG, "SeekBar Progress Changed: " + progress);
                    textViewDereceDisplay.setText(String.valueOf(progress) + "°C");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Log.d(TAG, "SeekBar Start Tracking");
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.d(TAG, "SeekBar Stop Tracking at: " + seekBar.getProgress());
                    sendFormattedData('T', seekBar.getProgress());
                }
            });

            Log.d(TAG, "Setting up switch listener.");
            switchIklimlendirme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "Switch State Changed: " + (isChecked ? "ON" : "OFF"));
                sendFormattedData('I', isChecked ? 1 : 0);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
        }

        return view;
    }

    private void pageInitialize(View view) {
        try {
            Log.d(TAG, "Initializing components.");
            switchIklimlendirme = view.findViewById(R.id.switchIklimlendirme);
            textViewDereceDisplay = view.findViewById(R.id.textViewDereceValue);
            seekbar = view.findViewById(R.id.circular_seek_bar);
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
}
