package com.champions.wayst;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import io.particle.android.sdk.cloud.ParticleCloudSDK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String CURRENT_LAT_KEY = "current_lat";
    public static final String CURRENT_LNG_KEY = "current_lng";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParticleCloudSDK.init(this);
        SparkComm.init();

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int connnectionResult = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (connnectionResult != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Please install/update Google Play Services!", Toast.LENGTH_LONG).show();
            return;
        }

        buildGoogleApiClient();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        final Switch objectAvoidanceSwitch = (Switch) findViewById(R.id.object_avoidance_switch);
        final Switch navigationSwitch = (Switch) findViewById(R.id.navigation_switch);
        final Button continueButton = (Button) findViewById(R.id.continue_button);

        // Object Avoidance Mode will be on by default
        // Object Avoidance Mode and Navigation Mode will toggle if one or the other is switched on/off

        objectAvoidanceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "object avoidance on");
                    navigationSwitch.setChecked(false);
                    SparkComm.callFunc(SparkComm.Cmd.NAVOFF);
                } else {
                    Log.d(TAG, "object avoidance off");
                    navigationSwitch.setChecked(true);
                }
            }
        });

        navigationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "navigation on");
                    objectAvoidanceSwitch.setChecked(false);
                    continueButton.setVisibility(View.VISIBLE);
                    SparkComm.callFunc(SparkComm.Cmd.NAVON);

                } else {
                    Log.d(TAG, "navigation off");
                    objectAvoidanceSwitch.setChecked(true);
                    continueButton.setVisibility(View.GONE);
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "continue button clicked, going to navigation activity");
                startService(new Intent(MainActivity.this, LocationService.class));
                Intent intent = new Intent(v.getContext(), NavigationActivity.class);
                Log.d(TAG, "mLastLocation null? " + (mLastLocation == null));
                if (mLastLocation != null) {
                    intent.putExtra("current_lat", mLastLocation.getLatitude());
                    intent.putExtra("current_lng", mLastLocation.getLongitude());
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient()");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            Log.d(TAG, "buildGoogleApiClient mLastLocation: " + mLastLocation);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        } else {
            new AsyncTask<Void, Void, Location>() {
                @Override
                protected Location doInBackground(Void... params) {
                    return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }

                @Override
                protected void onPostExecute(Location location) {
                    mLastLocation = location;
                    Log.d(TAG, "onPostExecute mLastLocation: " + mLastLocation);
                }
            }.execute();
        }
    }
}
