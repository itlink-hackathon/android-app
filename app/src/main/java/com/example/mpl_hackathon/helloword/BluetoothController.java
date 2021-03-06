package com.example.mpl_hackathon.helloword;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by mpl-hackathon on 15/01/2016.
 */
public class BluetoothController {

    public static final int REQUEST_ENABLE_BT = 100;

    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;

    public void initialize(Activity activity) {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void scanLeDevice(boolean enable, final BluetoothAdapter.LeScanCallback callback) {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            Handler handler = new Handler();
            if (enable) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(callback);
                    }
                }, SCAN_PERIOD);
                mBluetoothAdapter.startLeScan(callback);
            } else {
                mBluetoothAdapter.stopLeScan(callback);
            }
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }
}
