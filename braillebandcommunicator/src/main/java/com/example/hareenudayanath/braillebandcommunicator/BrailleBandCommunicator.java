package com.example.hareenudayanath.braillebandcommunicator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class is use to communicate with the BrailleBand after given the mac address
 *
 * Created by Hareen Udayanath on 10/30/2016.
 */
public class BrailleBandCommunicator {

    /*
   * Bluetooth connection variables.................
   * */
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private final int handlerState = 0;
    private Handler bluetoothIn;
    private StringBuilder recDataString = new StringBuilder();

    private String address;
    private ConnectedThread mConnectedThread;
    private boolean shouldAlive = true;


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    /*
    * ..................................................
    * */

    public BrailleBandCommunicator() {

        bluetoothIn = new Handler() {

            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("~");

                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();


    }

    public BrailleBandCommunicator(String address) {

        bluetoothIn = new Handler() {

            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("~");

                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.address= address;

    }

    /*
    * Bluetooth connection methods...............................
    * */

    public boolean connect(String address) {
        this.address = address;
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
            try {
                btSocket.connect();
                btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
                if (!mConnectedThread.success) {
                    closeSocket();
                    return false;
                }
                return true;
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    //insert code to deal with this
                }
                return false;
            }

        } catch (IOException e) {

            return false;
        }


    }

    public boolean connect() {
        if(this.address == null)
            return false;
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
            try {
                btSocket.connect();
                btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
                if (!mConnectedThread.success) {
                    closeSocket();
                    return false;
                }
                return true;
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    //insert code to deal with this
                }
                return false;
            }

        } catch (IOException e) {
            return false;
        }


    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        //creates secure outgoing connecetion with BT device using UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);

    }

    private boolean closeSocket() {
        try {
            btSocket.close();
            return true;
        } catch (IOException e2) {
            return false;
        }
    }

    public boolean disConnect(){
        if(closeSocket()){
            this.shouldAlive = false;
            mConnectedThread.success = false;
            return true;
        }
        return false;
    }

    public boolean connectWriteDisconnect(String input) {
        if(isConnected()) {
            return mConnectedThread.write(input);
        }else{
            connect();
            boolean result = mConnectedThread.write(input);
            disConnect();
            return result;
        }
    }

    public boolean write(String input){
        if(isConnected())
            return mConnectedThread.write(input);
        else
            return false;
    }

    public boolean isConnected(){
        if(this.btSocket == null)
            return false;
        if(this.btSocket.isConnected()){
            if(mConnectedThread != null)
                return mConnectedThread.success;
            return false;
        }else{
            return false;
        }
    }

    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public boolean success = false;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                success = true;
            } catch (IOException e) {
                success = false;
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (shouldAlive) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }

        }

        //write method
        public boolean write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);
                return true;//write bytes over BT connection via outstream
            } catch (IOException e) {
                return false;
            }
        }
    }

    public int checkBTState() {

        if (btAdapter == null) {
            //Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            return -1;
        } else {
            if (btAdapter.isEnabled()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /*
    * Reading from braille band
    * */

//    void writeReading(String msg){
//        if(CommunicationActivity.isReading){
//            CommunicationActivity inst = CommunicationActivity.instance();
//            inst.textReader.setText(msg);
//        }
//    }

}
