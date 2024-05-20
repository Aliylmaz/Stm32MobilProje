package com.example.myhome;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myhome.BluetoothServices.BluetoothService;
import com.example.myhome.BluetoothServices.CihazlarItemAdapter;

import java.util.ArrayList;

public class EslesmisCihazlar extends AppCompatActivity {

    private static final String TAG = "EslesmisCihazlar";
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    ArrayList<BluetoothDevice> devices;
    BluetoothAdapter bluetoothAdapter;
    RecyclerView cihazlarRecyclerView;
    CihazlarItemAdapter cihazlarItemAdapter;
    LinearLayoutManager layoutManager;
    TextView cihazDurum;
    SwipeRefreshLayout refreshLayout;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eslesmis_cihazlar);

        Log.d(TAG, "onCreate: Checking permissions...");
        // İzinleri kontrol edin ve gerekiyorsa isteyin
        if (!hasPermissions()) {
            Log.d(TAG, "onCreate: Permissions not granted, requesting...");
            requestPermissions();
        } else {
            Log.d(TAG, "onCreate: Permissions granted, initializing Bluetooth...");
            initializeBluetooth();
        }
    }

    private void initializeBluetooth() {
        devices = BluetoothService.getInstance(this).getBluetoothDevices();
        Log.d(TAG, "initializeBluetooth: Found " + devices.size() + " devices.");
        cihazlarItemAdapter = new CihazlarItemAdapter(this, devices);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cihazlarRecyclerView = findViewById(R.id.cihazlarRecyclerView);
        refreshLayout = findViewById(R.id.cihazlarRefresh);
        cihazlarRecyclerView.setAdapter(cihazlarItemAdapter);
        cihazlarRecyclerView.setLayoutManager(layoutManager);
        cihazDurum = findViewById(R.id.cihazlarDurum);

        if (devices.size() == 0) {
            cihazDurum.setVisibility(View.VISIBLE);
        } else {
            cihazDurum.setVisibility(View.GONE);
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                devices = BluetoothService.getInstance(EslesmisCihazlar.this).getBluetoothDevices();
                cihazlarItemAdapter.updateData(devices);
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private boolean hasPermissions() {
        boolean permissionsGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            permissionsGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        Log.d(TAG, "hasPermissions: " + permissionsGranted);
        return permissionsGranted;
    }

    private void requestPermissions() {
        Log.d(TAG, "requestPermissions: Requesting permissions...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_CODE_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "onRequestPermissionsResult: Permissions granted: " + granted);
            if (granted) {
                // İzin verildi, Bluetooth işlemleri burada yapılabilir
                initializeBluetooth();
            } else {
                // İzin reddedildi
                Toast.makeText(this, "Bluetooth ve konum izinleri gereklidir", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
