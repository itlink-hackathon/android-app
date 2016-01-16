package com.example.mpl_hackathon.helloword;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String V_ALERT_BLE_NAME = "V.ALRT A2:FE:C1";

    private ArrayAdapter<String> mArrayAdapter;
    private BluetoothDevice mValertDevice;

    private BluetoothController mBluetoothController;

    private LocationManager mLocationManager;

    private boolean mAlertDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.content_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        setTitle(getString(R.string.app_name));

        initTopButton();

        // bluetooth
        mBluetoothController = new BluetoothController();
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);

        registerReceiver(mBluetoothReceiver, filter);
        connectToDevice();

        // position
        mLocationManager = new LocationManager(this);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBluetoothReceiver);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        disconnectFromDevice();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case LocationManager.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        mLocationManager.tryStartingLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
            case BluetoothController.REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agree to access the bluetooth");
                        if (mBluetoothController != null) {
                            mBluetoothController.scanLeDevice(true, mLeScanCallback);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User disagree to access the bluetooth");
                        break;
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect_to_device) {
            connectToDevice();
            return true;
        } else if (id == R.id.action_disconnect_from_device) {
            disconnectFromDevice();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTopButton() {
        final ImageView btnTop = (ImageView) findViewById(R.id.btn_top);
        if (btnTop != null) {
            btnTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAlertDetected();
                }
            });
        }
    }

    private void changeLedColor() {
        final ImageView btnBottom = (ImageView) findViewById(R.id.btn_bottom);
        if (btnBottom != null && !mAlertDetected) {
            mAlertDetected = true;
            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnBottom.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.green_led));
                    mAlertDetected = false;
                }
            }, 2000);

            btnBottom.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.red_led));
        }
    }

    private void connectToDevice() {
        Log.d(TAG, "Démarrage du scan");
        if (mBluetoothController != null) {
            mBluetoothController.initialize(this);
            mBluetoothController.scanLeDevice(true, mLeScanCallback);
        }
    }

    private void disconnectFromDevice() {
        stopService(new Intent(MainActivity.this, BluetoothLeService.class));
        updateConnectionStatus(false);
    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Finding devices
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + " " + device.getAddress());
                Log.d(TAG, "new device detected : " + device.getName() + " " + device.getAddress());
                mArrayAdapter.notifyDataSetChanged();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "data received from device : " + action);
                updateConnectionStatus(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "data received from device : " + action);
                updateConnectionStatus(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "data received from device : " + action);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "data received from device : " + action);
                onAlertDetected();
            }
        }
    };

    private void updateConnectionStatus(boolean connected) {
        TextView status = (TextView) findViewById(R.id.status_connection);
        if (status != null) {
            if(connected) {
                status.setText("Connecté");
            } else {
                status.setText("Déconnecté");
            }
        }
    }

    private void sendAlertData() {
        try {
            final JSONObject jsonObject = getCurrentInformation();

            if (jsonObject != null) {
                // création de la requête
                JsonObjectRequest testRequestPost =
                        new JsonObjectRequest(Request.Method.POST,
                                "http://" + NetworkManager.HOSTNAME + "app-urgence/web/app.php/api/new-alerte",
                                jsonObject,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Toast.makeText(MainActivity.this, "Response received : " + response, Toast
                                                .LENGTH_LONG).show();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(MainActivity.this, "Response Error", Toast.LENGTH_LONG)
                                                .show();
                                        error.printStackTrace();
                                    }
                                });

                // envoi de requête
                NetworkManager.getInstance(getApplicationContext()).addToRequestQueue(testRequestPost);
            }
        } catch (JSONException e) {

        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    if (V_ALERT_BLE_NAME.equals(device.getName())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", device.toString());
                                connectToDevice(device);
                            }
                        });
                    }
                }
            };


    private void connectToDevice(BluetoothDevice device) {
        mValertDevice = device;
        Intent startIntent = new Intent(MainActivity.this, BluetoothLeService.class);
        startIntent.putExtra(BluetoothLeService.DEVICE_EXTRA, mValertDevice);
        startService(startIntent);
        mBluetoothController.scanLeDevice(false, mLeScanCallback);
    }

    private JSONObject getCurrentInformation() throws JSONException {
        JSONObject jsonBody = null;
        Location location = mLocationManager.getCurrentLocation();

        if (location != null) {
            jsonBody = new JSONObject();
            jsonBody.put("timestamp_current", new Date().getTime());
            jsonBody.put("latitude", location.getLatitude());
            jsonBody.put("longitude", location.getLongitude());
            jsonBody.put("timestamp_position", location.getTime());
        }

        return jsonBody;
    }

    private void onAlertDetected() {
        changeLedColor();
        sendAlertData();
    }
}
