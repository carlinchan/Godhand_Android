package com.example.godhand.Classes;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//////////////////////////////////////////////////////////////////////////////////////////
// BluetoothManger Class
// Used for managing the Bluetooth connection
//////////////////////////////////////////////////////////////////////////////////////////
public class BluetoothManager extends Application {
    // BluetoothDevice object
    private BluetoothDevice mDevice = null;
    // BluetoothSocket object
    private BluetoothSocket mSocket = null;
    // Bluetooth device's name
    private String name = null;
    // OutputStream object
    private OutputStream outputStream = null;
    // InputStream object
    private InputStream inputStream = null;
    // The timeout parameter for acknowledgement (unit: millisecond)
    private long timeout_ack = 1000;
    // The timeout parameter for "finished" message (unit: millisecond)
    private long timeout_finish = 8000;

    //////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    //////////////////////////////////////////////////////////////////////////////////////////
    public BluetoothDevice getDevice() {
        return mDevice;
    }
    public BluetoothSocket getSocket() {
        return mSocket;
    }
    public String getname() {
        return name;
    }
    public OutputStream getOutputStream() {
        return outputStream;
    }
    public InputStream getInputStream() {
        return inputStream;
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    // Setters
    //////////////////////////////////////////////////////////////////////////////////////////
    public void setDevice(BluetoothDevice bd) {
        this.mDevice = bd;
    }
    public void setSocket(BluetoothSocket bs) {
        this.mSocket = bs;
    }
    public void setName(String n) {
        this.name = n;
    }
    public void setOutputStream(OutputStream o) {
        this.outputStream = o;
    }
    public void setInputStream(InputStream i) {
        this.inputStream = i;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for sending the command
    // Once the command is sent out, it will call the waitACK() and wait for acknowledgement
    // Para: command - command string
    // Return: ack - acknowledgement
    //////////////////////////////////////////////////////////////////////////////////////////
    public String sendCommand(String command) throws IOException {
        outputStream.write(command.getBytes());
        outputStream.flush();
        String ack = "";
        ack = waitACK();
        return ack;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for waiting the acknowledgement
    // If the time between current and sending time exceeds the timeout_ack,
    // the while loop will stop, and send acknowledgement back
    // Return: ack - acknowledgement
    //////////////////////////////////////////////////////////////////////////////////////////
    public String waitACK() throws IOException {
        long sendingTime = System.currentTimeMillis();
        String ack = "";
        while(System.currentTimeMillis() - sendingTime < timeout_ack) {
            if (inputStream.available() > 0) {
                int byteRead = inputStream.read();
                char temp = (char)byteRead;
                ack += temp;
                if (ack.equals("valid") || ack.equals("invalid")) {
                    break;
                }
            }
        }
        if (ack.equals("")) {
            ack = "timeout";
        }
        return ack;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for waiting the acknowledgement
    // If the time between current and sending time exceeds the timeout_ack,
    // the while loop will stop, and send acknowledgement back
    // Return: ack - acknowledgement
    //////////////////////////////////////////////////////////////////////////////////////////
    public String waitFinish() throws IOException {
        Long sendingTime = System.currentTimeMillis();
        String msg = "";
        while(System.currentTimeMillis() - sendingTime < timeout_finish) {
            if (inputStream.available() > 0) {
                int byteRead = inputStream.read();
                char temp = (char)byteRead;
                msg += temp;
                if (msg.equals("finished")) {
                    break;
                }
            }
        }
        if (msg.equals("")) {
            msg = "timeout";
        }
        return msg;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for clear all data in the object (reset)
    //////////////////////////////////////////////////////////////////////////////////////////
    public void disconnect() throws IOException {
        this.inputStream.close();
        this.outputStream.close();
        this.mSocket.close();
        this.inputStream = null;
        this.outputStream = null;
        this.mSocket = null;
        this.mDevice = null;
    }

}
