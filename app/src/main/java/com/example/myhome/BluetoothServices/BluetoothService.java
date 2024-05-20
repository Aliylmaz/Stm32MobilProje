package com.example.myhome.BluetoothServices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {

    private static final String TAG = "BluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice device;
    private static BluetoothService instance;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Seri port profili için yaygın UUID
    private InputStream inputStream;
    private OutputStream outputStream;
    private List<String> receivedDataList = new ArrayList<>();
    private StringBuilder dataBuffer = new StringBuilder();

    // Değişken tanımları
    private int RGB_LED_red = 0;
    private int RGB_LED_green = 0;
    private int RGB_LED_blue = 0;
    private int RGB_LED_brightness = 0;
    private int DOOR_status = 0;
    private int PARK_status = 0;
    private int BUZZER_status = 0;
    private int GARDEN_LIGHT_status = 0;
    private int heaterStatus = 0;
    private int airconditioningStatus = 0;
    private int targetHeat = 0;
    private int gasStatus = 0;
    private int waterStatus = 0;
    private int temperature = 0;
    private int humidity = 0;

    private DataCallback dataCallback;

    public static BluetoothService getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothService(context);
        }
        return instance;
    }

    private BluetoothService(Context context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void connectToDevice(BluetoothDevice device) throws IOException {
        this.device = device;
        bluetoothAdapter.cancelDiscovery();

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable connectionTimeout = new Runnable() {
            @Override
            public void run() {
                try {
                    if (bluetoothSocket != null && !bluetoothSocket.isConnected()) {
                        bluetoothSocket.close();
                        throw new IOException("Connection timeout.");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Connection timeout: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        handler.postDelayed(connectionTimeout, 10000); // 10 saniye sonra zaman aşımı

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d(TAG, "Connecting to device: " + device.getName() + " - " + device.getAddress());
            bluetoothSocket.connect();
            handler.removeCallbacks(connectionTimeout); // Bağlantı başarılıysa zaman aşımını iptal et
            Log.d(TAG, "Connected to device: " + device.getName());

            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            startListeningForData();

        } catch (IOException e) {
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            Log.e(TAG, "Error connecting to device: " + e.getMessage(), e);
            throw new IOException("Error connecting to device: " + e.getMessage(), e);
        }
    }

    private void startListeningForData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    try {
                        if (inputStream != null && (bytes = inputStream.read(buffer)) > 0) {
                            String data = new String(buffer, 0, bytes);
                            Log.d(TAG, "Received data: " + data);
                            processReceivedData(data);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading data: " + e.getMessage());
                        break;
                    }
                }
            }
        }).start();
    }

    private void processReceivedData(String data) {
        // Gelen verileri buffer'a ekle
        dataBuffer.append(data);

        // '*' karakteri ile biten tam veri bloğu var mı kontrol et
        int endIndex = dataBuffer.indexOf("*");
        while (endIndex != -1) {
            String completeData = dataBuffer.substring(0, endIndex);
            dataBuffer.delete(0, endIndex + 1);

            // Tam veri bloğunu işlemeye gönder
            updateVariables(completeData);

            endIndex = dataBuffer.indexOf("*");
        }
    }

    public void setDataCallback(DataCallback callback) {
        this.dataCallback = callback;
    }

    public void disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                Log.d(TAG, "Disconnected from device: " + device.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Bluetooth cihazlarını getiren metod
    public ArrayList<BluetoothDevice> getBluetoothDevices() {
        ArrayList<BluetoothDevice> devices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            devices.addAll(pairedDevices);
        }
        Log.d(TAG, "getBluetoothDevices: Found " + devices.size() + " devices.");
        return devices;
    }

    // Getter metodları
    public int getRGB_LED_red() { return RGB_LED_red; }
    public int getRGB_LED_green() { return RGB_LED_green; }
    public int getRGB_LED_blue() { return RGB_LED_blue; }
    public int getRGB_LED_brightness() { return RGB_LED_brightness; }
    public int getDOOR_status() { return DOOR_status; }
    public int getPARK_status() { return PARK_status; }
    public int getBUZZER_status() { return BUZZER_status; }
    public int getGARDEN_LIGHT_status() { return GARDEN_LIGHT_status; }
    public int getHeaterStatus() { return heaterStatus; }
    public int getAirconditioningStatus() { return airconditioningStatus; }
    public int getTargetHeat() { return targetHeat; }
    public int getGasStatus() { return gasStatus; }
    public int getWaterStatus() { return waterStatus; }
    public int getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }

    public interface DataCallback {
        void onDataReceived();
    }

    public void updateVariables(String data) {
        try {
            String[] dataParts = data.split(",");
            for (String part : dataParts) {
                if (part.startsWith("C")) {
                    String value = part.substring(1); // "C27" -> "27"
                    temperature = Integer.parseInt(value);
                } else if (part.startsWith("N")) {
                    String value = part.substring(1); // "H45" -> "45"
                    humidity = Integer.parseInt(value);
                } else if (part.startsWith("r")) {
                    String value = part.substring(1); // "r255" -> "255"
                    RGB_LED_red = Integer.parseInt(value);
                } else if (part.startsWith("g")) {
                    String value = part.substring(1); // "g255" -> "255"
                    RGB_LED_green = Integer.parseInt(value);
                } else if (part.startsWith("b")) {
                    String value = part.substring(1); // "b255" -> "255"
                    RGB_LED_blue = Integer.parseInt(value);
                } else if (part.startsWith("B")) {
                    String value = part.substring(1); // "B100" -> "100"
                    RGB_LED_brightness = Integer.parseInt(value);
                } else if (part.startsWith("D")) {
                    String value = part.substring(1); // "D1" -> "1"
                    DOOR_status = Integer.parseInt(value);
                } else if (part.startsWith("P")) {
                    String value = part.substring(1); // "P1" -> "1"
                    PARK_status = Integer.parseInt(value);
                } else if (part.startsWith("A")) {
                    String value = part.substring(1); // "A1" -> "1"
                    BUZZER_status = Integer.parseInt(value);
                } else if (part.startsWith("G")) {
                    String value = part.substring(1); // "G1" -> "1"
                    GARDEN_LIGHT_status = Integer.parseInt(value);
                } else if (part.startsWith("H")) {
                    String value = part.substring(1); // "H1" -> "1"
                    heaterStatus = Integer.parseInt(value);
                } else if (part.startsWith("I")) {
                    String value = part.substring(1); // "I1" -> "1"
                    airconditioningStatus = Integer.parseInt(value);
                } else if (part.startsWith("T")) {
                    String value = part.substring(1); // "T25" -> "25"
                    targetHeat = Integer.parseInt(value);
                } else if (part.startsWith("Z")) {
                    String value = part.substring(1); // "Z1" -> "1"
                    gasStatus = Integer.parseInt(value);
                } else if (part.startsWith("W")) {
                    String value = part.substring(1); // "W1" -> "1"
                    waterStatus = Integer.parseInt(value);
                }
            }
            if (dataCallback != null) {
                dataCallback.onDataReceived();
            }
        } catch (NumberFormatException e) {
            Log.e("BluetoothService", "Error parsing data: " + e.getMessage());
        }
    }

    public void sendData(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                Log.d(TAG, "Sent data: " + data);
            } catch (IOException e) {
                Log.e(TAG, "Error sending data: " + e.getMessage());
            }
        }
    }
}
