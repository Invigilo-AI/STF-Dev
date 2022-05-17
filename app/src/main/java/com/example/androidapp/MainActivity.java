package com.example.androidapp;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button buttonON, buttonOFF;
    BluetoothAdapter myBluetoothAdapter;
    Intent btEnablingIntent;
    int requestCodeForEnable;
    ListView scanListView;
    ArrayList<String> stringArrayList = new ArrayList <String>();
    ArrayAdapter<String> arrayAdapter;
    TextView status;
    ArrayList<BluetoothDevice> btArrayList = new ArrayList<>();
    private static final UUID MY_UUID = UUID.fromString("9e0600a2-d5a9-11ec-9d64-0242ac120002");
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED= 5;

    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
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

    // GPS tracking variables
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    Switch sw_locationupdates, sw_gps;
    boolean updateOn = false;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonON = findViewById(R.id.btON);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;
        scanListView = findViewById(R.id.scannedListView);
        bluetoothONMethod();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver,intentFilter);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,stringArrayList);
        scanListView.setAdapter(arrayAdapter);
        status = findViewById(R.id.statuses);

        //for gps
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000*30);
        locationRequest.setFastestInterval(1000);

        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()){
                    startLocationUpdates();
                }
                else{
                    //turn off tracking fragment [078-MAJ]
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();




        // on click listener for the list view
        scanListView.setOnItemClickListener((parent, view, position, id) -> {
            ClientClass clientClass = new ClientClass(btArrayList.get(position));
            clientClass.start();
            status.setText("Connecting");
        });

    }

    private void startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestBlePermissions(this,1);
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates(){
        tv_lat.setText("Not tracking Location");
        tv_lon.setText("Not tracking Location");
        tv_altitude.setText("Not tracking Location");
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    private void sendpostreq(){
        String url = "http://ec2-54-169-121-229.ap-southeast-1.compute.amazonaws.com:11010/main";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response).getJSONObject("form");
                        String site = jsonResponse.getString("site"),
                                network = jsonResponse.getString("network");
                        System.out.println("Site: "+site+"\nNetwork: "+network);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("site", "code");
                params.put("network", "tutsplus");
                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> updateUIValues(location));
        }
        else {
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location){
        //update all the text view objects with a new location
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));

        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tv_altitude.setText("Not available");
        }
        sendpostreq();
    }


    Handler handler = new Handler(new Handler.Callback(){

        @Override
        public boolean handleMessage(Message msg){
            switch(msg.what){
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("connection failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    //write it later
                    break;
            }
            return true;

        }

    });

    // when available device is discovered by app
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestBlePermissions(((MyApp)
                            getApplicationContext()).getCurrentActivity(),1);
                }
                btArrayList.add(device);
                stringArrayList.add(device.getName());
                arrayAdapter.notifyDataSetChanged();
            }

        }
    };


// method to switch on bluetooth
    ActivityResultLauncher<Intent> activityLauncherForBluetooth = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth is Enabled", Toast.LENGTH_LONG).show();
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Bluetooth Enabling Cancelled", Toast.LENGTH_LONG).show();
                }
            });


// method to request permission to switch on bluetooth, make device discoverable
    public void requestBlePermissions(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
        }
    }
    public String[] getDevicesPaired(){
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
        return strings;
    }

    // fired when on is pressed
    private void bluetoothONMethod() {
        buttonON.setOnClickListener(v -> {
            if (myBluetoothAdapter == null) {
                Toast.makeText(getApplicationContext(), "bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
            } else {
                // if bluetooth no enabled
                if (!myBluetoothAdapter.isEnabled()) {
//                    // asks permission to switch on bluetooth
//                    requestBlePermissions(this, 1);
//                    // switches on bluetooth
//                    activityLauncherForBluetooth.launch(btEnablingIntent);
//                    Log.d("CHECK0", "doing");
                    // permission to make this device discoverable
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestBlePermissions(this,1);
                    }
                    // make device discoverable
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,10);
                    startActivity(intent);

                }else{
                    // if blue tooth is already enabled
                    // make device discoverable
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,10);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestBlePermissions(this,1);
                        startActivity(intent);
                    }


                }
                // start discovery of other available devices

                Toast.makeText(getApplicationContext(), "make discovery of other apps", Toast.LENGTH_LONG).show();
                // get permission to scan for available devices
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        ActivityCompat.requestPermissions(this, ANDROID_12_BLE_PERMISSIONS, 1);
                    else
                        ActivityCompat.requestPermissions(this, BLE_PERMISSIONS, 1);

                }
                myBluetoothAdapter.startDiscovery();





                // get paired devices
                String[] pairedDevices = getDevicesPaired();
            }

        });
    }



    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1){
            device = device1;

            try{
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        ActivityCompat.requestPermissions(((MyApp)
                                getApplicationContext()).getCurrentActivity(), ANDROID_12_BLE_PERMISSIONS, 1);
                    else
                        ActivityCompat.requestPermissions(((MyApp)
                                getApplicationContext()).getCurrentActivity(), BLE_PERMISSIONS, 1);

                }

                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }catch( IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            try{
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        ActivityCompat.requestPermissions(((MyApp)
                                getApplicationContext()).getCurrentActivity(), ANDROID_12_BLE_PERMISSIONS, 1);
                    else
                        ActivityCompat.requestPermissions(((MyApp)
                                getApplicationContext()).getCurrentActivity(), BLE_PERMISSIONS, 1);

                }
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
            }catch(IOException e){
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

















}