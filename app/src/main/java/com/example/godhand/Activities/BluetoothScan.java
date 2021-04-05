package com.example.godhand.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.godhand.Classes.BluetoothManager;
import com.example.godhand.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothScan extends AppCompatActivity {

    // Gesture listView
    private ListView listView;
    // Used to store the gesture retrieved from database
    private ArrayList<String> mDeviceList;
    // Object of BluetoothAdapter
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    // Object of BluetoothSocket
    private BluetoothSocket mSocket;

    //////////////////////////////////////////////////////////////////////////////////////////
    // onCreate()
    // 1. Setting the back button (going back to MainActivity)
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onResume()
    // 1. Checking the Bluetooth adapter whether support or not
    // 2. Checking the Bluetooth whether is on or off. If off, asking to turn on
    // 3. Discovering the Bluetooth devices and showing them on list
    // 4. Adding the click event listener to item in "list_view" (for connecting)
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
        listView = (ListView) findViewById(R.id.list_view);
        mDeviceList = new ArrayList<String>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter==null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .show();
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            mBluetoothAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(BluetoothScan.this, "Connecting...",
                        Toast.LENGTH_SHORT).show();
                String val = (String)parent.getItemAtPosition(position);
                String[] strings = val.split("\n");
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(strings[1]);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                try {
                    mBluetoothAdapter.cancelDiscovery();
                    mSocket = device.createRfcommSocketToServiceRecord(uuid);
                    mSocket.connect();
                    ((BluetoothManager) getApplication()).setDevice(device);
                    ((BluetoothManager) getApplication()).setSocket(mSocket);
                    ((BluetoothManager) getApplication()).setName(strings[0]);
                    ((BluetoothManager) getApplication()).setInputStream(mSocket.getInputStream());
                    ((BluetoothManager) getApplication()).setOutputStream(mSocket.getOutputStream());
                    Toast.makeText(BluetoothScan.this, "Connected!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } catch (IOException e) {
                    Toast.makeText(BluetoothScan.this, "Connection fails!",
                            Toast.LENGTH_SHORT).show();
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onDestroy()
    // 1. Unregistering the Receiver
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for finding the Bluetooth devices
    // Putting the finding into "list_view"
    //////////////////////////////////////////////////////////////////////////////////////////
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                listView.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mDeviceList));
            }
        }
    };

}
