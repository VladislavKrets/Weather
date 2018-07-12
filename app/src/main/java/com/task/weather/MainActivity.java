package com.task.weather;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button getSettingsButton;
    private TextView typeTextView, temperatureTextView;
    private Spinner citiesSpinner, seasonsSpinner, temperatureSpinner;
    private SQLiteDB sqLiteDB;
    private int currentCityId; // allows us not to do more request to db
    private double currentTemperature; //saving temperature before conversion allows us come it back
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initializeVariables(); //widget's initializing

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final SQLiteDatabase sqLiteDatabase = sqLiteDB.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select city from WeatherSettings", new String[]{});
        if (cursor.moveToFirst()) {
            do {
                arrayAdapter.add(cursor.getString(cursor.getColumnIndex("city"))); //cities spinner gets data from db
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        citiesSpinner.setAdapter(arrayAdapter);

        settingOnItemSelectedListeners(); //sets itemSelectedListeners

        getSettingsButton.setOnClickListener(this);
    }

    private void settingOnItemSelectedListeners() {
        citiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item);
                String[] seasons = getResources().getStringArray(R.array.seasons);
                String[] types = getResources().getStringArray(R.array.type);
                Cursor cursor = sqLiteDB.getWritableDatabase().rawQuery("select * from WeatherSettings where city=?",
                        new String[]{(String)parent.getAdapter().getItem(position)}); //searching by city name to get ids
                if (cursor.moveToFirst()) {
                    typeTextView.setText(types[cursor.getInt(cursor.getColumnIndex("type"))]); //get type by index from db
                    int cityId = cursor.getInt(cursor.getColumnIndex("id"));
                    currentCityId = cityId;
                    cursor.close();
                    cursor = sqLiteDB.getWritableDatabase()
                            .rawQuery("select * from SeasonSettings where cityId=?",
                                    new String[]{String.valueOf(cityId)});
                    //searching what seasons associated with this city were written
                    if (cursor.moveToFirst()) {
                        do {
                            arrayAdapter.add(seasons[cursor.getInt(cursor.getColumnIndex("season"))]);
                            //getting seasons's names by index
                        }
                        while (cursor.moveToNext());
                    }
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    seasonsSpinner.setAdapter(arrayAdapter);

                }
                cursor.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        seasonsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = sqLiteDB.getWritableDatabase()
                        .rawQuery("select * from SeasonSettings where cityId=? and season=?",
                                new String[]{String.valueOf(currentCityId), String.valueOf(position)});
                if (cursor.moveToFirst()) {
                    currentTemperature = cursor.getDouble(cursor.getColumnIndex("averageTemperature"));

                    double temperature = currentTemperature;
                    switch (temperatureSpinner.getSelectedItemPosition()) {
                        case 0:{
                            showSnackBar(currentTemperature, getResources().getStringArray(R.array.temperature)[0]);
                            break;
                        }
                        case 1:{
                            temperature = temperature * 1.8 + 32;
                            showSnackBar(temperature, getResources().getStringArray(R.array.temperature)[1]);
                            break;
                        } //fahrenheit
                        case 2: {
                            temperature += 273.15;
                            showSnackBar(temperature, getResources().getStringArray(R.array.temperature)[2]);
                            break;
                        }  //kelvin
                    }
//ToDO
                    temperatureTextView.setText("t=" + String
                            .format("%.2f", temperature)); //2 digits after point
                }
                cursor.close();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        temperatureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = temperatureTextView.getText().toString();
                if (!text.isEmpty()) {
                    text = text.replaceAll("t=", ""); //to get only a number
                    double temperature = Double.parseDouble(text);
                    switch (position) {
                        case 0: {
                            text = "t=" + String.format("%.2f", currentTemperature); //set cesium
                            showSnackBar(currentTemperature, getResources().getStringArray(R.array.temperature)[0]);
                            break;
                        }
                        case 1: {
                            text = "t=" + String.format("%.2f", currentTemperature * 1.8 + 32); //set fahrenheit
                            showSnackBar(currentTemperature * 1.8 + 32, getResources().getStringArray(R.array.temperature)[1]);
                            break;
                        }
                        case 2: {
                            text = "t=" + String.format("%.2f", currentTemperature + 273.15); //set kelvin
                            showSnackBar(currentTemperature + 273.15, getResources().getStringArray(R.array.temperature)[2]);
                            break;
                        }
                    }
                    temperatureTextView.setText(text);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeVariables() {
        getSettingsButton = findViewById(R.id.getSettingsButton);
        typeTextView = findViewById(R.id.typeTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        citiesSpinner = findViewById(R.id.citiesSpinner);
        seasonsSpinner = findViewById(R.id.seasonsSpinner);
        temperatureSpinner = findViewById(R.id.temperatureSpinner);
        sqLiteDB = new SQLiteDB(this, "WeatherDB", null, SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getSettingsButton : {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
                //after stopping started activity two MainActivities will open
                //so we stop current activity
            }
        }
    }

    public void showSnackBar(double temperature, String temp) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.relativeLayout),
                String.format(getResources().getString(R.string.temperature_snack), temperature, temp), Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }
}
