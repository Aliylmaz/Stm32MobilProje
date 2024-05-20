package com.example.myhome.BluetoothServices;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.myhome.R;
import com.example.myhome.WeatherManager;

public class home_Fragment extends Fragment {

    WeatherManager weatherManager;
    ImageView weatherIcon, alarmStatusIcon;
    TextView weatherTemp, weatherCondition, locationText, weatherTime, weatherDay;
    TextView iklimlendirmeMainTemp, iklimlendirmeTargetTemp, humidityValue, kombiDurum, gazDurumu, suBaskiniDurumu,
    klimaDurum;
    SwipeRefreshLayout homeRefresh;

    private BluetoothService bluetoothService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_, container, false);

        // Sayfa bileşenlerini başlatma
        pageInitialize(view);

        // WeatherManager örneği oluşturma
        weatherManager = WeatherManager.getInstance(getContext());
        fetchWeatherData();



        // Hava durumu verilerini alma
        weatherManager.getLocationAndWeather(new WeatherManager.WeatherCallback() {
            @Override
            public void onWeatherDataFetched() {
                String icon = weatherManager.getHavaDurumuIcon();
                String temperature = weatherManager.getSıcaklık();
                String location = weatherManager.getKonum();
                String time = weatherManager.getSaat();
                String dayOfWeek = weatherManager.getHaftanınGünü();

                // Verileri UI bileşenlerine yerleştirme
                if (icon != null) {
                    String iconUrl = "https://openweathermap.org/img/wn/" + icon + ".png";
                    Glide.with(getContext())
                            .load(iconUrl)
                            .into(weatherIcon);
                }

                if (temperature != null) {
                    // Sıcaklık değerinden "°C" kısmını çıkart ve tam sayı olarak göster
                    String tempValue = temperature.replace("°C", "").trim();
                    try {
                        int tempInt = (int) Double.parseDouble(tempValue);
                        weatherTemp.setText(String.format("%d°C", tempInt));
                    } catch (NumberFormatException e) {
                        Log.e("DENEME", "Error parsing temperature: " + e.getMessage());
                    }
                }

                if (location != null) {
                    locationText.setText(location);
                }

                if (time != null) {
                    weatherTime.setText(time);
                }

                if (dayOfWeek != null) {
                    weatherDay.setText(dayOfWeek);
                }

                Log.d("DENEME", icon + " " + temperature + " " + location + " " + time + " " + dayOfWeek);
            }




            @Override
            public void onError(String error) {
                Log.e("DENEME", "Error fetching weather data: " + error);
            }
        });

        // BluetoothService callback ayarı
        bluetoothService = BluetoothService.getInstance(getContext());
        bluetoothService.setDataCallback(new BluetoothService.DataCallback() {
            @Override
            public void onDataReceived() {

                updateUI();
            }
        });

        homeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bluetoothService.sendData("<V,1!!!!!!!!!!!!!!!!!!!!!!!!!!!>");
                fetchWeatherData();
                homeRefresh.setRefreshing(false);
            }
        });

        return view;
    }

    // Sayfa bileşenlerini başlatma
    void pageInitialize(View view) {
        weatherIcon = view.findViewById(R.id.weatherIcon);
        weatherTemp = view.findViewById(R.id.weatherTemp);
        weatherCondition = view.findViewById(R.id.weatherCondition);
        locationText = view.findViewById(R.id.locationText);
        weatherTime = view.findViewById(R.id.weatherTime);
        weatherDay = view.findViewById(R.id.weatherDay);
        iklimlendirmeMainTemp = view.findViewById(R.id.iklimlendirmeMainTemp);
        humidityValue = view.findViewById(R.id.humidityValue);
        kombiDurum = view.findViewById(R.id.kombiDurum);
        gazDurumu = view.findViewById(R.id.gazDurumu);
        suBaskiniDurumu = view.findViewById(R.id.suBaskiniDurumu);
        iklimlendirmeTargetTemp = view.findViewById(R.id.iklimlendirmeTargetTemp);
        homeRefresh= view.findViewById(R.id.homeRefresh);
        klimaDurum= view.findViewById(R.id.klimaDurum);
    }

    private void updateUI() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // UI güncellemeleri
                iklimlendirmeMainTemp.setText(bluetoothService.getTemperature() + "°C");
                humidityValue.setText(bluetoothService.getHumidity() + "%");
                kombiDurum.setText(bluetoothService.getHeaterStatus() == 1 ? "Açık" : "Kapalı");
                klimaDurum.setText(bluetoothService.getAirconditioningStatus() == 1 ? "Açık" : "Kapalı");
                gazDurumu.setText(bluetoothService.getGasStatus() == 1 ? "Gaz Var" : "Gaz Yok");
                suBaskiniDurumu.setText(bluetoothService.getWaterStatus() == 1 ? "Su Baskını" : "Su Baskını Yok");
                iklimlendirmeTargetTemp.setText("  / "+bluetoothService.getTargetHeat() + "°C");
            }
        });
    }

    private void fetchWeatherData() {
        weatherManager.getLocationAndWeather(new WeatherManager.WeatherCallback() {
            @Override
            public void onWeatherDataFetched() {
                String icon = weatherManager.getHavaDurumuIcon();
                String temperature = weatherManager.getSıcaklık();
                String location = weatherManager.getKonum();
                String time = weatherManager.getSaat();
                String dayOfWeek = weatherManager.getHaftanınGünü();

                // Verileri UI bileşenlerine yerleştirme
                if (icon != null) {
                    String iconUrl = "https://openweathermap.org/img/wn/" + icon + ".png";
                    Glide.with(getContext())
                            .load(iconUrl)
                            .into(weatherIcon);
                }

                if (temperature != null) {
                    // Sıcaklık değerinden "°C" kısmını çıkart ve tam sayı olarak göster
                    String tempValue = temperature.replace("°C", "").trim();
                    try {
                        int tempInt = (int) Double.parseDouble(tempValue);
                        weatherTemp.setText(String.format("%d°C", tempInt));
                    } catch (NumberFormatException e) {
                        Log.e("DENEME", "Error parsing temperature: " + e.getMessage());
                    }
                }

                if (location != null) {
                    locationText.setText(location);
                }

                if (time != null) {
                    weatherTime.setText(time);
                }

                if (dayOfWeek != null) {
                    weatherDay.setText(dayOfWeek);
                }

                Log.d("DENEME", icon + " " + temperature + " " + location + " " + time + " " + dayOfWeek);
            }

            @Override
            public void onError(String error) {
                Log.e("DENEME", "Error fetching weather data: " + error);
            }
        });
    }
}
