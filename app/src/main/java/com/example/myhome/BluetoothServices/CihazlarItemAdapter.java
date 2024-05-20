package com.example.myhome.BluetoothServices;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.myhome.R;
import com.example.myhome.mainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CihazlarItemAdapter extends RecyclerView.Adapter<CihazlarItemAdapter.CihazlarViewHolder> {

    private ArrayList<BluetoothDevice> devices;
    private Activity context;
    String uniqueId = UUID.randomUUID().toString();
    BluetoothDevice device;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String device_address;
    BluetoothSocket bluetoothSocket;

    public CihazlarItemAdapter(Activity context, ArrayList<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
    }

    public void updateData(ArrayList<BluetoothDevice> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CihazlarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.cihazlar_item, parent, false);
        return new CihazlarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CihazlarViewHolder holder, int position) {
        device = devices.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());

        holder.cihazConstrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device = devices.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false); // Prevent closing the dialog by clicking outside
                builder.setView(R.layout.loading_dialog); // Custom layout for loading
                AlertDialog loadingDialog = builder.create();
                loadingDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(context,
                                        new String[]{
                                                Manifest.permission.BLUETOOTH_SCAN,
                                                Manifest.permission.BLUETOOTH_CONNECT,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                        },
                                        1);
                                return;
                            }
                            BluetoothService.getInstance(context).getBluetoothAdapter().cancelDiscovery();
                            BluetoothService.getInstance(context).connectToDevice(devices.get(position));

                            // Bağlantı başarılı olduğunda yapılacak işlemler
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismiss();
                                    Snackbar.make(v, device.getName() + " 'e bağlanıldı.", Snackbar.LENGTH_LONG).show();

                                    // MainActivity'e geçiş
                                    Intent intent = new Intent(context, mainActivity.class);
                                    context.startActivity(intent);
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismiss();
                                    Snackbar.make(v, "Bağlantı hatası: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class CihazlarViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, deviceAddress;
        CardView cihazCardView;
        ConstraintLayout cihazConstrain;

        public CihazlarViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.cihadAdi);
            cihazCardView = itemView.findViewById(R.id.cihazCardView);
            deviceAddress = itemView.findViewById(R.id.cihazId);
            cihazConstrain = itemView.findViewById(R.id.cihazConstrain);
        }
    }
}
