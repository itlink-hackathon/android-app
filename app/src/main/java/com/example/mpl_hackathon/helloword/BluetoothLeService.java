package com.example.mpl_hackathon.helloword;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by mpl-hackathon on 16/01/2016.
 */
// A service that interacts with the BLE device via the Android BLE API.
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    public static final String DEVICE_EXTRA = "BluetoothLeService.device.extra";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_VSN_GATT_SERVICE =
            UUID.fromString("FFFFFFF0-00F7-4000-B000-000000000000");

    public final static UUID UUID_VSN_GATT_SERVICE_CHARACTERISTIC =
            UUID.fromString("FFFFFFF4-00F7-4000-B000-000000000000");

    public final static UUID UUID_ENABLE_NOTIFICATION =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initConnection(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        closeConnection();
        super.onDestroy();
    }

    private void initConnection(Intent intent) {
        if (intent != null) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(DEVICE_EXTRA);
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }
    }

    private void closeConnection() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.i(TAG,"test");
                    BluetoothGattCharacteristic characteristic = gatt.getService(UUID_VSN_GATT_SERVICE).getCharacteristic(UUID_VSN_GATT_SERVICE_CHARACTERISTIC);
                    gatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_ENABLE_NOTIFICATION);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    //return
                    Log.i(TAG, ""+ (gatt.writeDescriptor(descriptor) ? "vrai" : "faux")); //descriptor write operation successfully started?
                }
                // New services discovered
                public void onServicesDiscovered_old(BluetoothGatt gatt, int status) {
                    Log.i(TAG, "onServicesDiscovered");
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        List<BluetoothGattService> services = gatt.getServices();
                        Log.i(TAG, "onServicesDiscovered" + services.toString());
                        for (BluetoothGattService serv : services) {
                            Log.i(TAG, "uuid : " + serv.getUuid());
                            if (UUID_VSN_GATT_SERVICE.equals(serv.getUuid())) {
                                Log.d(TAG, "VSN GATT service detected !");
                                List<BluetoothGattCharacteristic> characteristics
                                        = serv.getCharacteristics();
                                for (BluetoothGattCharacteristic charac : characteristics) {
                                    if (UUID_VSN_GATT_SERVICE_CHARACTERISTIC.equals(charac.getUuid())) {
                                        Log.d(TAG, "VSN GATT characteristic detected !");
                                        List<BluetoothGattDescriptor> descriptors = charac.getDescriptors();
                                        gatt.setCharacteristicNotification(charac, true);

                                        for(BluetoothGattDescriptor descriptor : descriptors){
                                            if(UUID_ENABLE_NOTIFICATION.equals(descriptor.getUuid())) {
                                                Log.d(TAG, "enable notification found !");
                                                if(descriptor.getValue() != null) {
                                                    Log.d(TAG, "enable value : " + descriptor.getValue());
                                                } else {
                                                    Log.d(TAG, "enable value : null");
                                                }
                                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                                try{
                                                    wait(1000);
                                                } catch (Exception e) {

                                                }
                                                gatt.writeDescriptor(descriptor);
                                            }
                                        }

                                    }
                                }
                            }
                        }
//                        gatt.readCharacteristic(services.get(0).getCharacteristics().get
//                                (0));
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.i(TAG, "onCharacteristicRead" + characteristic.toString());
                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.i(TAG, "onCharacteristicChanged" + characteristic.getValue());
                }
            };

    private void broadcastUpdate(String intentAction) {
        Intent intent = new Intent(intentAction);
        sendBroadcast(intent);
    }
}