package com.example.mpl_hackathon.helloword;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTopButton();
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
        if (id == R.id.action_settings) {
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
}
