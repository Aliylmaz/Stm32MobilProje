package com.example.myhome;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.myhome.BluetoothServices.BluetoothService;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.LightnessSlider;

public class Kontrol extends Fragment {

    Button buttonColorPicker;
    Button buttonOpenCloseDoor;
    Button buttonRotatePark;
    Button buttonOpenCloseGardenLight;

    private boolean isDoorOpen = false;
    private boolean isParkRotated = false;
    private boolean isGardenLightOn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kontrol, container, false);

        buttonColorPicker = view.findViewById(R.id.button_color_picker);
        buttonOpenCloseDoor = view.findViewById(R.id.button_open_close_door);
        buttonRotatePark = view.findViewById(R.id.button_rotate_park);
        buttonOpenCloseGardenLight = view.findViewById(R.id.button_open_close_garden_light);

        buttonColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog();
            }
        });

        buttonOpenCloseDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDoor();
            }
        });

        buttonRotatePark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotatePark();
            }
        });

        buttonOpenCloseGardenLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGardenLight();
            }
        });

        return view;
    }

    private void showColorPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
        builder.setView(dialogView);

        ColorPickerView colorPickerView = dialogView.findViewById(R.id.color_picker_view);
        LightnessSlider v_lightness_slider = dialogView.findViewById(R.id.v_lightness_slider);

        colorPickerView.setLightnessSlider(v_lightness_slider);
        colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int selectedColor) {
                String hexColor = Integer.toHexString(selectedColor);
                int[] rgb = hexToRgb(hexColor);
                int brightness = calculateBrightness(rgb[0], rgb[1], rgb[2]);
                Log.d("ColorPicker", "onColorChanged: " + "RGB: " + rgb[0] + ", " + rgb[1] + ", " + rgb[2] + ", Brightness: " + brightness);
                sendFormattedRGBValues(rgb[0], rgb[1], rgb[2], brightness);
            }
        });

        builder.setPositiveButton("Tamam", (dialog, which) -> {
            // Tamam butonuna basıldığında yapılacak işlemler
        });

        builder.setNegativeButton("İptal", (dialog, which) -> {
            // İptal butonuna basıldığında yapılacak işlemler
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void toggleDoor() {
        // Kapı açma/kapatma işlevi
        if (isDoorOpen){
            isDoorOpen=false;
            BluetoothService.getInstance(getContext()).sendData(formatCommand("D", 0));


        }else {
            isDoorOpen=true;
            BluetoothService.getInstance(getContext()).sendData(formatCommand("D", 1));
        }


    }

    private void rotatePark() {
        // Otopark döndürme işlevi
        if (isParkRotated){
            isParkRotated=false;
            BluetoothService.getInstance(getContext()).sendData(formatCommand("P", 0));

        }else {
            isParkRotated=true;
            BluetoothService.getInstance(getContext()).sendData(formatCommand("P", 1));
        }

    }

    private void toggleGardenLight() {
        // Bahçe ışığı açma/kapatma işlevi
        if (isGardenLightOn){
            isGardenLightOn=false;
            BluetoothService.getInstance(getContext()).sendData(formatCommand("G", 0));

        }else {
            isGardenLightOn=true;
            BluetoothService.getInstance(getContext()).sendData(formatCommand("G", 1));
        }
    }
    public static Kontrol newInstance() {
        return new Kontrol(); // Fragment oluşturuldu, fragment:bir sayfanın içindeki bir parça
    }

    public void sendFormattedRGBValues(int red, int green, int blue, int brightness) {
        // <R,r=138,g=112,b=23,B=55!!!!!!!> şeklinde veri gidecek, giden veriler her seferinde değişebilir ona göre format
        String formattedValues = String.format("<R,r=%d,g=%d,b=%d,B=%d>", red, green, blue, brightness);
        int remainingLength = 31 - formattedValues.length();
        if (remainingLength > 0) {
            for (int i = 0; i < remainingLength; i++) {
                formattedValues += "!";
            }
        }
        formattedValues += ">";
        BluetoothService.getInstance(getContext()).sendData(formattedValues);
        Log.d("RGB", formattedValues);
    }





    public static int[] hexToRgb(String hex) {
        // Hex kodunu '#' karakterinden temizle
        hex = hex.replace("#", "");

        // Hex kodunu RGB bileşenlerine böl
        int r = Integer.parseInt(hex.substring(2, 4), 16);
        int g = Integer.parseInt(hex.substring(4, 6), 16);
        int b = Integer.parseInt(hex.substring(6, 8), 16);

        return new int[]{r, g, b};
    }

    private int calculateBrightness(int red, int green, int blue) {
        // Brightness hesaplama formülü (0-255 aralığında)
        int brightness = (int) Math.sqrt(
                red * red * .241 +
                        green * green * .691 +
                        blue * blue * .068);

        // 0-255 aralığındaki parlaklığı 0-100 aralığına normalize et
        return (brightness * 100) / 255;
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

