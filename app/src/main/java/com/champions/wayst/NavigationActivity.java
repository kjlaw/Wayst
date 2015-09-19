package com.champions.wayst;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG = NavigationActivity.class.getSimpleName();

    private static final String TRAVEL_MODE = "bicycling";
    private static final String API_KEY = "AIzaSyA-gfC_TmedowzzVdH4l3QveSS4DDdi4YM";
    private static final String KEY_STATUS = "status";
    private static final String STATUS_OK = "OK";
    public static final String LAT_LNG_KEY = "latlng";

    private EditText mDestinationEditText;
    private TextView mDirectionsLabel;
    private ListView mDirectionsList;

    private String mCurrentLocationCoords;

    private ArrayList<LatLng> mStepCoordinates = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Intent intent = getIntent();
        double currentLat = intent.getDoubleExtra(MainActivity.CURRENT_LAT_KEY, -1000);
        double currentLng = intent.getDoubleExtra(MainActivity.CURRENT_LNG_KEY, -1000);
        if (currentLat != -1000 && currentLng != -1000) {
            mCurrentLocationCoords = currentLat + "," + currentLng;
        }

        Button navigateButton = (Button) findViewById(R.id.navigate_button);
        EditText originEditText = (EditText) findViewById(R.id.origin);
        mDestinationEditText = (EditText) findViewById(R.id.destination);
        mDirectionsLabel = (TextView) findViewById(R.id.directions_label);
        mDirectionsList = (ListView) findViewById(R.id.directions_list);

        originEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), getCurrentLocation(), Toast.LENGTH_SHORT).show();
            }
        });

        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                mStepCoordinates = new ArrayList<LatLng>();

                DirectionsDataModel.Directions directions = getDirections();
                DirectionsDataModel.Step[] steps = getSteps(directions);
                if (steps == null) {
                    Log.d(TAG, "steps is null!");
                    return;
                }

                List<String> stepList = new ArrayList<String>();
                for (DirectionsDataModel.Step step : steps) {
                    String instructions = Html.fromHtml(step.html_instructions).toString();
                    instructions = instructions.replaceAll("[\r\n]+", "\n").trim();
                    stepList.add(instructions);

                    if (step.start_location != null) {
                        mStepCoordinates.add(new LatLng(step.start_location.lat, step.start_location.lng));
                    }
                }

                if (stepList.size() > 0) {
                    showDirections(stepList);
                }

                Intent intent = new Intent(v.getContext(), LocationService.class);
                intent.putParcelableArrayListExtra(LAT_LNG_KEY, mStepCoordinates);
                startService(intent);
            }
        });

    }

    private DirectionsDataModel.Step[] getSteps(DirectionsDataModel.Directions directions) {
        if (directions == null) {
            Log.d(TAG, "directions is null!");
            return null;
        }
        DirectionsDataModel.Route[] routes = directions.routes;
        if (routes == null) {
            Log.d(TAG, "routes is null!");
            return null;
        }
        DirectionsDataModel.Leg[] legs = null;
        if (routes.length > 0) {
            legs = routes[0].legs;
        }
        if (legs == null) {
            Log.d(TAG, "legs is null!");
            return null;
        }
        DirectionsDataModel.Step[] steps = null;
        if (legs.length > 0) {
            steps = legs[0].steps;
        }
        return steps;
    }

    private void showInvalidDestinationToast() {
        Toast.makeText(getApplicationContext(), "Invalid destination!", Toast.LENGTH_LONG).show();
    }

    private String getCurrentLocation() {
        Log.d(TAG, "current location: " + mCurrentLocationCoords);
        return mCurrentLocationCoords;
    }

    private DirectionsDataModel.Directions getDirections() {
        String origin = getCurrentLocation();
        String destination = mDestinationEditText.getText().toString();

        String url = buildUrl(origin, destination);
        JsonObject directionsJson = getDirectionsJson(url);
        if (directionsJson == null) {
            showInvalidDestinationToast();
            Log.d(TAG, "directionsJson is null!");
            return null;
        }
        JsonElement jsonStatus = directionsJson.get(KEY_STATUS);
        if (jsonStatus == null) {
            showInvalidDestinationToast();
            Log.d(TAG, "jsonStatus is null!");
            return null;
        }
        String status = jsonStatus.getAsString();
        if (!status.equals(STATUS_OK)) {
            showInvalidDestinationToast();
            Log.d(TAG, "status is invalid");
            return null;
        }
        Log.d(TAG, "json: " + directionsJson.toString());
        Gson gson = new Gson();
        return gson.fromJson(directionsJson, DirectionsDataModel.Directions.class);
    }

    private JsonObject getDirectionsJson(String url) {
        Response<JsonObject> jsonResponse;
        int timeoutMs = 25000;
        try {
            jsonResponse = Ion.with(getApplicationContext())
                    .load(url)
                    .setTimeout(timeoutMs)
                    .asJsonObject()
                    .withResponse()
                    .get(timeoutMs, TimeUnit.MILLISECONDS);
            if (jsonResponse.getHeaders().code() == 200) {
                return jsonResponse.getResult();
            } else {
                Log.d(TAG, "Couldn't get json, response code: " + jsonResponse.getHeaders().code());
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception getting json " + e);
        }
        return null;
    }

    private String buildUrl(String origin, String destination) {
        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?";

        Uri uri = Uri.parse(baseUrl);

        return uri.buildUpon().appendQueryParameter("origin", origin)
                .appendQueryParameter("destination", destination)
                .appendQueryParameter("key", API_KEY)
                .appendQueryParameter("mode", TRAVEL_MODE)
                .build().toString();
    }

    private void showDirections(List<String> instructions) {
        mDirectionsLabel.setVisibility(View.VISIBLE);
        mDirectionsList.setVisibility(View.VISIBLE);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, instructions);
        mDirectionsList.setAdapter(adapter);
    }
}
