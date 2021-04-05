////////////////////////////////////////////////////////////////////////////////////////////////////
// Name: Chan Car Lin
// UID: 3035604568
////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////

// References:

// The idea of "global socket" is come from Programmer Sought
// Retrieved from: https://www.programmersought.com/article/86442480064/

// The idea of "DB implementation" is come from
// "Creating simple Database in Android Studio" on YOUTUBE
// Retrieved from: https://www.youtube.com/watch?v=K6cYSNXb9ew

// The idea of update the UI from a background thread
//https://riptutorial.com/android/example/24946/updating-the-ui-from-a-background-thread

////////////////////////////////////////////////////////////////////////////////////////////////////

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.godhand.Classes.BluetoothManager;
import com.example.godhand.R;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //////////////////////////////////////////////////////////////////////////////////////////
    // onCreate()
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onResume()
    // 1. Checking the Bluetooth connection is connected or not
    // 2. Updating the UI setting
    // 3. Registering the Receiver for bluetooth connection monitoring
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
        if (((BluetoothManager) getApplication()).getDevice() != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(((BluetoothManager) getApplication()).getDevice().ACTION_ACL_DISCONNECTED);
            this.registerReceiver(mReceiver, filter);
            this.connectedSetting();
        }
        else {
            this.disconnectedSetting();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "connect" button click
    //////////////////////////////////////////////////////////////////////////////////////////
    public void connectFunction(View view) {
        Intent intent = new Intent(this, BluetoothScan.class);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "disconnect" button click
    //////////////////////////////////////////////////////////////////////////////////////////
    public void disconnectFunction(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage("Would you like to disconnect?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            ((BluetoothManager)getApplication()).disconnect();
                            disconnectedSetting();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this, "Disconnected!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        this.unregisterReceiver(mReceiver);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "gripping" button click
    //////////////////////////////////////////////////////////////////////////////////////////
    public void grippingMode(View view) {
        Intent intent = new Intent(this, GrippingMode.class);
        this.unregisterReceiver(mReceiver);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "gesture" button click
    //////////////////////////////////////////////////////////////////////////////////////////
    public void gestureMode(View view) {
        Intent intent = new Intent(this, GestureMode.class);
        this.unregisterReceiver(mReceiver);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "freedom" button click
    //////////////////////////////////////////////////////////////////////////////////////////
    public void freedomMode(View view) {
        Intent intent = new Intent(this, FreedomMode.class);
        this.unregisterReceiver(mReceiver);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for UI setting in disconnected status
    //////////////////////////////////////////////////////////////////////////////////////////
    public void disconnectedSetting() {
        ((TextView)findViewById(R.id.status_content)).setText("Disconnected");
        ((TextView)findViewById(R.id.device_content)).setText("Non");
        (findViewById(R.id.connect)).setEnabled(true);
        (findViewById(R.id.disconnect)).setEnabled(false);
        (findViewById(R.id.gripping)).setEnabled(false);
        (findViewById(R.id.gesture)).setEnabled(false);
        (findViewById(R.id.freedom)).setEnabled(false);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for UI setting in connected status
    //////////////////////////////////////////////////////////////////////////////////////////
    public void connectedSetting() {
        ((TextView)findViewById(R.id.status_content)).setText("Connected");
        String name = ((BluetoothManager)getApplication()).getname();
        ((TextView)findViewById(R.id.device_content)).setText(name);
        (findViewById(R.id.connect)).setEnabled(false);
        (findViewById(R.id.disconnect)).setEnabled(true);
        (findViewById(R.id.gripping)).setEnabled(true);
        (findViewById(R.id.gesture)).setEnabled(true);
        (findViewById(R.id.freedom)).setEnabled(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for monitoring the Bluetooth connection
    // If the bluetooth is disconnected, resetting the BluetoothManager
    // and changing the UI into disconnected mode
    //////////////////////////////////////////////////////////////////////////////////////////
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((BluetoothManager) getApplication()).getDevice().ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d("Connection loss: ", "Connection loss!");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Connection loss! Please connect again!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ((BluetoothManager)getApplication()).disconnect();
                                    disconnectedSetting();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
            }
        }
    };

}
