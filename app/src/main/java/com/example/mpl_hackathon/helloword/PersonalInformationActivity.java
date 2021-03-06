package com.example.mpl_hackathon.helloword;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

/**
 * Created by mnaHackathon on 16/01/2016.
 */
public class PersonalInformationActivity extends AppCompatActivity {

    public static final String LASTNAME = "lastname";
    public static final String FIRSTNAME = "firstname";
    public static final String PHONE_NUMBER = "phone_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.personal_info_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        setTitle("Mes informations");
    }

    @Override
    protected void onResume() {
        initLastName();
        initFirstName();
        initPhoneNumber();
        super.onResume();
    }

    @Override
    protected void onPause() {
        saveLastName();
        saveFirstName();
        savePhoneNumber();
        super.onPause();
    }

    private void initLastName() {
        EditText name = (EditText) findViewById(R.id.lastname);
        if (name != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            name.setText(settings.getString(LASTNAME, ""));
        }
    }

    private void initFirstName() {
        EditText name = (EditText) findViewById(R.id.firstname);
        if (name != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            name.setText(settings.getString(FIRSTNAME, ""));
        }
    }

    private void initPhoneNumber() {
        EditText name = (EditText) findViewById(R.id.phone_number);
        if (name != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            name.setText(settings.getString(PHONE_NUMBER, ""));
        }
    }

    private void saveLastName() {
        EditText name = (EditText) findViewById(R.id.lastname);
        if (name != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            settings.edit().putString(LASTNAME, name.getText().toString()).commit();
        }
    }

    private void saveFirstName() {
        EditText name = (EditText) findViewById(R.id.firstname);
        if (name != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            settings.edit().putString(FIRSTNAME, name.getText().toString()).commit();
        }
    }

    private void savePhoneNumber() {
        EditText name = (EditText) findViewById(R.id.phone_number);
        if (name != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            settings.edit().putString(PHONE_NUMBER, name.getText().toString()).commit();
        }
    }
}
