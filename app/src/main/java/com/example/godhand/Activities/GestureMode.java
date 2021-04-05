package com.example.godhand.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.example.godhand.Classes.DBHelper;
import com.example.godhand.Classes.Gesture;
import com.example.godhand.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GestureMode extends AppCompatActivity {

    // Gesture listView
    private ListView listView;
    // Used to store the gesture retrieved from database
    private ArrayList<String> mGestureList;
    // The thread for waiting acknowledgement (command received)
    private ThreadACK threadACK;

    //////////////////////////////////////////////////////////////////////////////////////////
    // onCreate()
    // 1. Setting the back button (going back to MainActivity)
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_mode);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onResume()
    // 1. Checking the Bluetooth connection is connected or not
    // 2. Updating the UI setting
    // 3. Registering the Receiver for bluetooth connection monitoring
    // 4. Initializing the threadACK
    // 5. Adding the click event listener to item in "gesture_list" (for action performing)
    // 6. Adding the long click event listener to item in "gesture_list" (for edit/delete gesture)
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
        if (((BluetoothManager) getApplication()).getDevice() != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(((BluetoothManager) getApplication()).getDevice().ACTION_ACL_DISCONNECTED);
            this.registerReceiver(mReceiver, filter);
        }

        threadACK = new ThreadACK();

        listView = (ListView) findViewById(R.id.gesture_list);
        mGestureList = new ArrayList<String>();

        DBHelper db = new DBHelper(this);

        List<Gesture> gestures = db.getAllGesture();
        for (Gesture g : gestures) {
            Log.d("gesture: ", "ID: " + g.getId() + ", Name: " + g.getName() +
                    ", Content: " + g.getContent());
            mGestureList.add("Name: " + g.getName()
                    + "\n" + "Content: " + g.getContent()
                    + "\n" + "ID: " + g.getId());
        }
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mGestureList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String val = (String)parent.getItemAtPosition(position);
                String[] strings = val.split("\n");
                String[] commands = strings[1].split(": ");

                listView.setEnabled(false);
                threadACK = new ThreadACK(commands[1]);
                threadACK.start();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(GestureMode.this, GestureEditDelete.class);

                String val = (String)parent.getItemAtPosition(position);
                String[] strings = val.split("\n");
                String[] names = strings[0].split(": ");
                String[] commands = strings[1].split(": ");
                String[] ids = strings[2].split(": ");
                Log.d("Long click names: ", names[1]);
                Log.d("Long click commands: ", commands[1]);
                Log.d("Long click ids: ", ids[1]);

                Bundle bundle = new Bundle();
                bundle.putString("id", ids[1]);
                intent.putExtras(bundle);

                GestureMode.this.unregisterReceiver(mReceiver);
                startActivity(intent);

                return true;
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onDestroy()
    // 1. Unregistering the Receiver
    // 2. Killing the threadACK
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
        if (threadACK.isAlive()) {
            threadACK.interrupt();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "add_gesture" button click
    //////////////////////////////////////////////////////////////////////////////////////////
    public void create_gesture (View view) {
        this.unregisterReceiver(mReceiver);
        Intent intent = new Intent(this, GestureCreate.class);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for monitoring the Bluetooth connection
    // If the bluetooth is disconnected, resetting the BluetoothManager
    // and going back to MainActivity
    //////////////////////////////////////////////////////////////////////////////////////////
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((BluetoothManager) getApplication()).getDevice().ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d("Connection loss: ", "Connection loss!");
                AlertDialog alertDialog = new AlertDialog.Builder(GestureMode.this)
                        .setMessage("Connection loss! Please connect again!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ((BluetoothManager)getApplication()).disconnect();
                                    if (threadACK.isAlive()) {
                                        threadACK.interrupt();
                                    }
                                    finish();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
            }
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for waiting the acknowledgement (command received)
    // If the acknowledgement "timeout" is received, the connection is loss.
    // It will reset the BluetoothManager and going back to MainActivity.
    // The user need to connect the Bluetooth device again.

    // After the successful acknowledgement is received, this thread will wait the action completed
    // If the message "timeout" is received, the connection is loss.
    // It will reset the BluetoothManager and going back to MainActivity.
    // The user need to connect the Bluetooth device again.
    // Otherwise, the "gesture_list" will be enabled.
    //////////////////////////////////////////////////////////////////////////////////////////
    public class ThreadACK extends Thread {
        private String command;

        public ThreadACK() {

        }

        public ThreadACK(String c) {
            this.command = c;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String ack = null;
                    try {
                        ack = ((BluetoothManager)getApplication()).sendCommand(command);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d ("ack: ", ack);
                    if (ack.equals("timeout")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(GestureMode.this)
                                .setMessage("ACK Timeout! Please connect again!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            ((BluetoothManager)getApplication()).disconnect();
                                            if (threadACK.isAlive()) {
                                                threadACK.interrupt();
                                            }
                                            finish();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).show();
                    }
                    else if (ack.equals("invalid")) {
                        Toast.makeText(GestureMode.this, "Command valid!",
                                Toast.LENGTH_SHORT).show();
                        listView.setEnabled(true);
                    }
                    else if (ack.equals("valid")){
                        String msg = null;
                        try {
                            msg = ((BluetoothManager)getApplication()).waitFinish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d ("message: ", msg);
                        // The timeout block may not be executed due to there is a Receiver
                        // If want to show this block,
                        // we can reduce the value of "timeout_finish" in BluetoothManager
                        if (msg.equals("timeout")) {
                            AlertDialog alertDialog = new AlertDialog.Builder(GestureMode.this)
                                    .setMessage("MSG Timeout! Please connect again!")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                ((BluetoothManager)getApplication()).disconnect();
                                                if (threadACK.isAlive()) {
                                                    threadACK.interrupt();
                                                }
                                                finish();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).show();
                        }
                        else if (msg.equals("finished")) {
                            Toast.makeText(GestureMode.this, "Action completed!",
                                    Toast.LENGTH_SHORT).show();
                            listView.setEnabled(true);
                        }
                        else {
                            Toast.makeText(GestureMode.this, "Unknown message!",
                                    Toast.LENGTH_SHORT).show();
                            listView.setEnabled(true);
                        }
                    }
                    else {
                        Toast.makeText(GestureMode.this, "Unknown ack!",
                                Toast.LENGTH_SHORT).show();
                        listView.setEnabled(true);
                    }
                }
            });
        }
    }

}
