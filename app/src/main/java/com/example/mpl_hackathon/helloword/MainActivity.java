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

    private LocationManager mLocationManager;

    private NetworkManager mNetworkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.content_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Log.d(TAG, "1");
        initTopButton();

        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBluetoothReceiver, filter);

        Log.d(TAG, "2");

        startScanBluetoothDevices();

        mLocationManager = new LocationManager(this);

        sendTestRequest();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBluetoothReceiver);
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
        BluetoothController controller = new BluetoothController();
        controller.initialize(this);
        controller.scan();
    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    Log.d(TAG, "new device detected : " + device.getName() + " " + device.getAddress());
                    if(device.getName().equals(VAlertBleName))
                    {
                        mValertDevice = device;
                        Log.d(TAG, "VAlert Detected! ");
                    }

                    mArrayAdapter.add(device.getName() + " " + device.getAddress());
                    mArrayAdapter.notifyDataSetChanged();
                }
            }
        };

    private void sendTestRequest() {
        // création de la requête
        StringRequest testRequestPost = new StringRequest(Request.Method.GET,
                "http://" + NetworkManager.HOSTNAME + "",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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


}
