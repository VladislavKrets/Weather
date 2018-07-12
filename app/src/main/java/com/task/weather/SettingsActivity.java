package com.task.weather;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText cityEditText, firstMonthEditText, secondMonthEditText, thirdMonthEditText;
    private Spinner typeSpinner, firstMonthSpinner, secondMonthSpinner, thirdMonthSpinner;
    private String cityName, firstMonthValue, secondMonthValue,
            thirdMonthValue, firstMonth, secondMonth, thirdMonth, season;
    private int tripletPos, type;
    private TextView errorTextView;
    private Button addButton;
    private SQLiteDB sqLiteDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeVariables(); //initializing widgets
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setSpinnerAdapters();
        addButton.setOnClickListener(this);
        setOnItemSelectedListeners();
    }

    private void setOnItemSelectedListeners() {
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        firstMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectMonth(position); //to get away difficulties with season definition
                String[] months = getResources().getStringArray(R.array.months);
                firstMonth = months[position];
                months = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        secondMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectMonth(position); //to get away difficulties with season definition
                String[] months = getResources().getStringArray(R.array.months);
                secondMonth = months[position];
                months = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        thirdMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println();
                selectMonth(position); //to get away difficulties with season definition
                String[] months = getResources().getStringArray(R.array.months);
                thirdMonth = months[position];
                months = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setSpinnerAdapters() {
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstMonthSpinner.setAdapter(adapter);
        secondMonthSpinner.setAdapter(adapter);
        thirdMonthSpinner.setAdapter(adapter);
        adapter =
                ArrayAdapter.createFromResource(this, R.array.type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void initializeVariables() {
        cityEditText = findViewById(R.id.cityEditText);
        typeSpinner = findViewById(R.id.typeSpinner);
        firstMonthSpinner = findViewById(R.id.firstMonthSpinner);
        secondMonthSpinner = findViewById(R.id.secondMonthSpinner);
        thirdMonthSpinner = findViewById(R.id.thirdMonthSpinner);
        firstMonthEditText = findViewById(R.id.firstMonthEditText);
        secondMonthEditText = findViewById(R.id.secondMonthEditText);
        thirdMonthEditText = findViewById(R.id.thirdMonthEditText);
        errorTextView = findViewById(R.id.errorTextView);
        addButton = findViewById(R.id.addButton);
        sqLiteDB = new SQLiteDB(this, "WeatherDB", null, SQLiteDatabase.CREATE_IF_NECESSARY);
    }


    private void selectMonth(int position) {
        tripletPos = position / 3;
        firstMonthSpinner.post(new Runnable() {
            @Override
            public void run() {
                firstMonthSpinner.setSelection(3 * tripletPos, false);
            }
        });
        secondMonthSpinner.post(new Runnable() {
            @Override
            public void run() {
                secondMonthSpinner.setSelection(3 * tripletPos + 1, false);
            }
        });
        thirdMonthSpinner.post(new Runnable() {
            @Override
            public void run() {
                thirdMonthSpinner.setSelection(3 * tripletPos + 2, false);
            }
        });

    }

    private class SettingsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentValues contentValues = new ContentValues();
            SQLiteDatabase sqLiteDatabase = sqLiteDB.getWritableDatabase();
            contentValues.put("city", cityName);
            contentValues.put("type", type);
            Cursor c = sqLiteDatabase.query("WeatherSettings", null, "city=?",
                    new String[]{cityName}, null, null, null);
            //checking is the city in db
            if (!c.moveToFirst())
                sqLiteDatabase.insert("WeatherSettings", null, contentValues);
            //if not adding it
            c.close();

            c = sqLiteDatabase.rawQuery("select id from WeatherSettings where city= ?", new String[]{cityName});
            contentValues = new ContentValues();
            //getting id
            if (c.moveToFirst()) {
                int cityId = c.getInt(0);
                String[] seasons = getResources().getStringArray(R.array.seasons);
                c.close();
                c = sqLiteDatabase.query("SeasonSettings", null, "cityId = ? and season= ?",
                        new String[]{String.valueOf(cityId), String.valueOf(tripletPos)}, null, null, null);
                //checking is season in db
                if (c.moveToFirst())
                    sqLiteDatabase.delete("SeasonSettings", "cityId=? and season=?", new String[]{String.valueOf(cityId), String.valueOf(tripletPos)});
                //if it is delete it to update
                contentValues.put("cityId", cityId);
                contentValues.put("season", tripletPos);
                double average = (Double.parseDouble(firstMonthValue) + Double.parseDouble(secondMonthValue) + Double.parseDouble(thirdMonthValue)) / 3;
                contentValues.put("averageTemperature", average);
                sqLiteDatabase.insert("SeasonSettings", null, contentValues);

                c.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addButton: {
                cityName = cityEditText.getText().toString();
                firstMonthValue = firstMonthEditText.getText().toString();
                secondMonthValue = secondMonthEditText.getText().toString();
                thirdMonthValue = thirdMonthEditText.getText().toString();
                if (areFieldsEmpty()) {
                    errorTextView.setText(getResources().getString(R.string.not_filled_lines));
                }
                else {
                    new SettingsTask().execute();
                }
            }
        }
    }

    private boolean areFieldsEmpty() {
        return (cityName == null || cityName.isEmpty())
                || (firstMonthValue == null || firstMonthValue.isEmpty())
                || (secondMonthValue == null || secondMonthValue.isEmpty())
                || (thirdMonthValue == null || thirdMonthValue.isEmpty());
    }
}
