package com.example.myhome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myhome.BluetoothServices.BluetoothService;

import java.util.Arrays;

public class iklimlendirme extends Fragment {

    private static final String TAG = "iklimlendirmeFragment";

    SeekBar seekbarDerece;
    Switch switchIklimlendirme;
    SeekBar seekbar;

    TextView textViewDereceDisplay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_iklimlendirme, container, false);

        try {
            Log.d(TAG, "Initializing page elements.");
            pageInitialize(view);

            Log.d(TAG, "Setting up spinner adapter.");
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.derece_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



            Log.d(TAG, "Setting up spinner listener.");

            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.d("DEGERİM",String.valueOf(progress));
                    textViewDereceDisplay.setText(String.valueOf(progress) + "°C");





                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {


                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    sendFormattedData('T', seekBar.getProgress());

                }
            });


            Log.d(TAG, "Setting up button click listener.");


        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Log.d("hata aldım",e.getMessage());
        }

        return view;
}

    void pageInitialize(View view) {

        switchIklimlendirme = view.findViewById(R.id.switchIklimlendirme);

        textViewDereceDisplay = view.findViewById(R.id.textViewDereceValue);
        seekbar = view.findViewById(R.id.seekBar);
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
        Log.d("sendFormattedData", data); // Debug için gönderilen veriyi loglayın
    }
}
