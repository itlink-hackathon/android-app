package com.example.mpl_hackathon.helloword;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


/**
 * Created by mpl-hackathon on 16/01/2016.
 */
public class BluetoothConnectionThread {

    private static final String NAME = BluetoothConnectionThread.class.getSimpleName();
    private static final String TAG = BluetoothConnectionThread.class.getSimpleName();

    private BluetoothServerSocket mServerSocket;

    public BluetoothConnectionThread(BluetoothController controller, UUID uuid) {
        if(controller != null && uuid != null) {
            // Use a temporary object that is later assigned to mServerSocket,
            // because mServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = controller.getBluetoothAdapter().listenUsingRfcommWithServiceRecord(NAME, uuid);
            } catch (IOException e) { }
            mServerSocket = tmp;
        }
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mServerSocket.accept();
            } catch (IOException e) {
                Log.d(TAG, "Connection failed !");
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
//                manageConnectedSocket(socket);
                Log.d(TAG, "Connection success !");
                try {
                    mServerSocket.close();
                } catch (IOException e) {

                } finally {
                    break;
                }
            } else {
                Log.d(TAG, "Connection failed !");
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException e) { }
    }
}
