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
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.godhand.Classes.BluetoothManager;
import com.example.godhand.Classes.DBHelper;
import com.example.godhand.Classes.Gesture;
import com.example.godhand.R;

import java.io.IOException;

public class GestureEditDelete extends AppCompatActivity {

    // The object of DBHelper class
    DBHelper db;
    // The object of Gesture class
    Gesture gesture;

    //////////////////////////////////////////////////////////////////////////////////////////
    // onCreate()
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_edit_delete);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onResume()
    // 1. Checking the Bluetooth connection is connected or not
    // 2. Registering the Receiver for bluetooth connection monitoring
    // 3. Getting the gesture information from database
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();
        if (((BluetoothManager) getApplication()).getDevice() != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(((BluetoothManager) getApplication()).getDevice().ACTION_ACL_DISCONNECTED);
            this.registerReceiver(mReceiver, filter);
        }

        Bundle bundle = getIntent().getExtras();
        String passedID = bundle.getString("id");
        Log.d("passed id: ", passedID);

        db = new DBHelper(this);
        gesture = db.getGesture(Integer.parseInt(passedID));

//        ((EditText)findViewById(R.id.edit_name)).setHint(gesture.getName());
        ((EditText)findViewById(R.id.edit_name)).setText(gesture.getName());

        String temp_command = gesture.getContent();
        if (temp_command.charAt(0) == 'C') {
            ((Switch) findViewById(R.id.thumb_switch)).setChecked(true);
        }
        if (temp_command.charAt(1) == 'C') {
            ((Switch) findViewById(R.id.index_switch)).setChecked(true);
        }
        if (temp_command.charAt(2) == 'C') {
            ((Switch) findViewById(R.id.middle_switch)).setChecked(true);
        }
        if (temp_command.charAt(3) == 'C') {
            ((Switch) findViewById(R.id.ring_switch)).setChecked(true);
        }
        if (temp_command.charAt(4) == 'C') {
            ((Switch) findViewById(R.id.pinky_switch)).setChecked(true);
        }
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
    // The action of "cancel" button click
    // Discarding the update and going back to GestureMode
    //////////////////////////////////////////////////////////////////////////////////////////
    public void cancel_click (View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(GestureEditDelete.this)
                .setMessage("Would you like to discard the changes?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(GestureEditDelete.this, "Remain unchanged!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "delete" button click
    // Deleting the gesture and going back to GestureMode
    //////////////////////////////////////////////////////////////////////////////////////////
    public void delete_click (View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(GestureEditDelete.this)
                .setMessage("Would you like to delete this gesture?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteGesture(gesture);
                        Toast.makeText(GestureEditDelete.this, "Deleted!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // The action of "update" button click
    // Updating the gesture and going back to GestureMode
    //////////////////////////////////////////////////////////////////////////////////////////
    public void update_click (View view) {
        boolean[] onOff = {false, false, false, false, false};

        onOff[0] = ((Switch) findViewById(R.id.thumb_switch)).isChecked();
        onOff[1] = ((Switch) findViewById(R.id.index_switch)).isChecked();
        onOff[2] = ((Switch) findViewById(R.id.middle_switch)).isChecked();
        onOff[3] = ((Switch) findViewById(R.id.ring_switch)).isChecked();
        onOff[4] = ((Switch) findViewById(R.id.pinky_switch)).isChecked();

        char[] temp = {'R', 'R', 'R', 'R', 'R'};
        for (int i = 0; i < 5; i++){
            if (onOff[i] == false) {
                temp[i] = 'S';
            }
            else {
                temp[i] = 'C';
            }
        }
        String command = String.valueOf(temp);
        Log.d("Edit gesture result: ", command);

        String name = ((EditText)findViewById(R.id.edit_name)).getText().toString();
        Log.d("Edit gesture name: ", name);

        if (name.trim().equals("")) {
            AlertDialog alertDialog = new AlertDialog.Builder(GestureEditDelete.this)
                    .setMessage("The Name field is required!")
                    .setPositiveButton("OK", null)
                    .show();
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(GestureEditDelete.this)
                    .setMessage("Would you like to update this gesture?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Gesture edited = new Gesture(gesture.getId(), name, command);
                            int value = db.undateGesture(edited);

                            Toast.makeText(GestureEditDelete.this, "Updated!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
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
                AlertDialog alertDialog = new AlertDialog.Builder(GestureEditDelete.this)
                        .setMessage("Connection loss! Please connect again!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ((BluetoothManager)getApplication()).disconnect();
                                    Intent main = new Intent(GestureEditDelete.this,
                                            MainActivity.class);
                                    finish();
                                    startActivity(main);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
            }
        }
    };

}
