package com.example.mpl_hackathon.helloword;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String VAlertBleName = "V.ALRT A2:FE:C1";

    private ArrayAdapter<String> mArrayAdapter;
    private BluetoothDevice mValertDevice;

    private BluetoothController mBluetoothController;

    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.content_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        initTopButton();

        // bluetooth
        mBluetoothController = new BluetoothController();
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        IntentFilter filter = new IntentFilter(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        registerReceiver(mBluetoothReceiver, filter);
        startScanBluetoothDevices();

        // position
        mLocationManager = new LocationManager(this);

        // connection avec serveur
        sendTestRequest();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBluetoothReceiver);
        stopService(new Intent(MainActivity.this, BluetoothLeService.class));
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
        if (id == R.id.action_scan_bluetooth) {
            startScanBluetoothDevices();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTopButton() {
        final ImageView btnTop = (ImageView) findViewById(R.id.btn_top);
        if (btnTop != null) {
            btnTop.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            btnTop.setImageDrawable(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.alert_btn_pressed));
                            btnTop.getLayoutParams().height = 160;
                            btnTop.getLayoutParams().width = 160;
                            changeLedColor(true);
                            return true;
                        case MotionEvent.ACTION_UP:
                            btnTop.setImageDrawable(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.alert_btn_unpressed));
                            btnTop.getLayoutParams().height = 180;
                            btnTop.getLayoutParams().width = 180;
                            changeLedColor(false);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
    }

    private void changeLedColor(boolean green) {
        ImageView btnBottom = (ImageView) findViewById(R.id.btn_bottom);
        if (btnBottom != null) {
            if (green) {
                btnBottom.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.green_led));
            } else {
                btnBottom.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.red_led));
            }
        }

    }

    private void startScanBluetoothDevices() {
        Log.d(TAG, "Démarrage du scan");
        if (mBluetoothController != null) {
            mBluetoothController.initialize(this);
            mBluetoothController.scanLeDevice(true, mLeScanCallback);
        }
    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.getAction())) {
                Log.d(TAG, "data received from device : " + intent.getAction());
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {
                Log.d(TAG, "data received from device : " + intent.getAction());
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {
                Log.d(TAG, "data received from device : " + intent.getAction());
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    if (VAlertBleName.equals(device.getName())) {
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

    private void sendTestRequest() {
        // création de la requête
        StringRequest testRequestPost = new StringRequest(Request.Method.GET,
                "http://" + NetworkManager.HOSTNAME + "",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Response received : success !", Toast
                                .LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Response received : Error !", Toast.LENGTH_LONG)
                                .show();
                        error.printStackTrace();
                    }
                });

        // envoi de requête
        NetworkManager.getInstance(getApplicationContext()).addToRequestQueue(testRequestPost);
    }


}
