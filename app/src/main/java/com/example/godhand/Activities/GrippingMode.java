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
import android.widget.Button;
import android.widget.Toast;

import com.example.godhand.Classes.BluetoothManager;
import com.example.godhand.R;

import java.io.IOException;

public class GrippingMode extends AppCompatActivity {

    // The thread for waiting acknowledgement
    private ThreadACK threadACK;

    //////////////////////////////////////////////////////////////////////////////////////////
    // onCreate()
    // 1. Setting the back button (going back to MainActivity)
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gripping_mode);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onResume()
    // 1. Checking the Bluetooth connection is connected or not
    // 2. Registering the Receiver for bluetooth connection monitoring
    // 3. Initializing the threadACK
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
    // The action of buttons' click
    // When the button is clicked, the button's content description will be retrieved,
    // then disabling all buttons
    // and running the threadACK to wait the acknowledgement (command received)
    //////////////////////////////////////////////////////////////////////////////////////////
    public void actionPerform(View view) throws InterruptedException {
        Log.d("Content Description: ", ((Button) view).getContentDescription().toString());
        final String command = ((Button) view).getContentDescription().toString();

        toggleButton();

        threadACK = new ThreadACK(command);
        threadACK.start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // User for monitoring the Bluetooth connection
    // If the bluetooth is disconnected, resetting the BluetoothManager
    // and going back to MainActivity
    //////////////////////////////////////////////////////////////////////////////////////////
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((BluetoothManager) getApplication()).getDevice().ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d("Connection loss: ", "Connection loss!");
                AlertDialog alertDialog = new AlertDialog.Builder(GrippingMode.this)
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
    // Otherwise, the other buttons will be enabled.
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
                        AlertDialog alertDialog = new AlertDialog.Builder(GrippingMode.this)
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
                        Toast.makeText(GrippingMode.this, "Command valid!",
                                Toast.LENGTH_SHORT).show();
                        toggleButton();
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
                            AlertDialog alertDialog = new AlertDialog.Builder(GrippingMode.this)
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
                            Toast.makeText(GrippingMode.this, "Action completed!",
                                    Toast.LENGTH_SHORT).show();
                            toggleButton();
                        }
                        else {
                            Toast.makeText(GrippingMode.this, "Unknown message!",
                                    Toast.LENGTH_SHORT).show();
                            toggleButton();
                        }
                    }
                    else {
                        Toast.makeText(GrippingMode.this, "Unknown ack!",
                                Toast.LENGTH_SHORT).show();
                        toggleButton();

                    }
                }
            });
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for UI toggling the button
    //////////////////////////////////////////////////////////////////////////////////////////
    public void toggleButton() {
        findViewById(R.id.gripping).setEnabled(!findViewById(R.id.gripping).isEnabled());
        findViewById(R.id.straight).setEnabled(!findViewById(R.id.straight).isEnabled());
        findViewById(R.id.relax).setEnabled(!findViewById(R.id.relax).isEnabled());
        findViewById(R.id.gripping).setClickable(!findViewById(R.id.gripping).isClickable());
        findViewById(R.id.straight).setClickable(!findViewById(R.id.straight).isClickable());
        findViewById(R.id.relax).setClickable(!findViewById(R.id.relax).isClickable());
    }

}
