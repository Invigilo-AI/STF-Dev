package com.example.androidapp;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button buttonON, buttonOFF;
    BluetoothAdapter myBluetoothAdapter;
    Intent btEnablingIntent;
    ListView listView;
    int requestCodeForEnable;
    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonON = findViewById(R.id.btON);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;

        bluetoothONMethod();
    }

    ActivityResultLauncher<Intent> activityLauncherForBluetooth = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth is Enabled", Toast.LENGTH_LONG).show();
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Bluetooth Enabling Cancelled", Toast.LENGTH_LONG).show();
                }
            });


    public void requestBlePermissions(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
        }
    }

    // fired when on is pressed
    private void bluetoothONMethod() {
        buttonON.setOnClickListener(v -> {
            if (myBluetoothAdapter == null) {
                Toast.makeText(getApplicationContext(), "bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            } else {
                if (!myBluetoothAdapter.isEnabled()) {
                    // asks permission to switch on bluetooth
                    requestBlePermissions(this, 1);
                    // switches on bluetooth
                    activityLauncherForBluetooth.launch(btEnablingIntent);
                    Log.d("CHECK0", "doing");
                    // permission to show list of devices that our device detects
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestBlePermissions(this,1);
                    }
                    Log.d("CHECK1", "FINISHED on");
                    // checks for paired devices
                    // can use for finding the stick c im paired with
                    Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
                    String[] strings = new String[bt.size()];
                    int index = 0 ;
                    if (bt.size() > 0){
                        for(BluetoothDevice device:bt){
                            strings[index] = device.getName();
                            index++;

                        }


                    }



                }else{
                    // if blue tooth is already enabled
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestBlePermissions(this,1);
                    }
                    Log.d("CHECK1", "FINISHED on");

                    Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
                    String[] strings = new String[bt.size()];
                    int index = 0 ;
                    Log.d("CHECK2", "FINISHED chunk");
                    if (bt.size() > 0){
                        for(BluetoothDevice device:bt){
                            strings[index] = device.getName();
                            index++;

                        }
                        Log.d("CHECK3", "FINISHED for");
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,strings);
                        Log.d("CHECK4", "FINISHED for");
                        listView.setAdapter(arrayAdapter);
                        Log.d("CHECK5", "FINISHED for");

                    }


                }
            }

        });
    }
}