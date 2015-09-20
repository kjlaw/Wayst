package com.champions.wayst;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();

    private static final int LOCATION_INTERVAL = 4000;
    private static final float LOCATION_DISTANCE = 0;
    private static final int LOCATION_RANGE = 5;

    private LocationManager mLocationManager = null;
    private Location mLastLocation;

    private List<LatLng> mStepCoordinates = null;
    private List<String> mStepDescription = null;
    private LatLng mDestination = null;

    private LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private class LocationListener implements android.location.LocationListener{

        public LocationListener(String provider)
        {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            if (mStepCoordinates != null) {
                navigate();
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");

        mStepCoordinates = intent.getParcelableArrayListExtra(NavigationActivity.COORDS_KEY);
        mStepDescription = intent.getStringArrayListExtra(NavigationActivity.DESCS_KEY);
        mDestination = intent.getParcelableExtra(NavigationActivity.DEST_KEY);
        if (mStepCoordinates != null && mStepDescription != null && mDestination != null) {
            navigate();
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void navigate() {
        Log.d(TAG, "navigate()");

        if (mStepCoordinates == null) {
            Log.d(TAG, "mStepCoordinates is null");
            return;
        }

        float [] dist = new float[1];
        double lat1 = mLastLocation.getLatitude();
        double lng1 = mLastLocation.getLongitude();

        if (mStepCoordinates.size() > 0) {
            Log.d(TAG, "calculate distance");

            double lat2 = mStepCoordinates.get(0).latitude;
            double lng2 = mStepCoordinates.get(0).longitude;

            Location.distanceBetween(lat1, lng1, lat2, lng2, dist);
            if (dist[0] < LOCATION_RANGE) {
                Log.d(TAG, "distance is less than 5 meters");
                mStepCoordinates.remove(0);
                DirectionsDataModel.Direction direction = parseDescription(mStepDescription.remove(0));
                Log.d(TAG, "direction: " + direction.desc);
                SparkComm.Cmd dir = null;
                if (direction == DirectionsDataModel.Direction.LEFT) {
                    dir = SparkComm.Cmd.TURNLEFT;
                } else if (direction == DirectionsDataModel.Direction.RIGHT) {
                    dir = SparkComm.Cmd.TURNRIGHT;
                }
                SparkComm.callFunc(dir);
            }
        }

        double lat2 = mDestination.latitude;
        double lng2 = mDestination.longitude;

        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);

        if (dist[0] < LOCATION_RANGE) {
            Log.d(TAG, "Arrived!");
            SparkComm.callFunc(SparkComm.Cmd.DESTREACHED);
            // Stop service, we have arrived
            this.stopSelf();
        }
    }

    private DirectionsDataModel.Direction parseDescription(String description) {
        description = description.toLowerCase();
        if (description.contains("left")) {
            return DirectionsDataModel.Direction.LEFT;
        } else if (description.contains("right")) {
            return DirectionsDataModel.Direction.RIGHT;
        } else if (description.contains("continue")) {
            return DirectionsDataModel.Direction.CONTINUE;
        }
        return DirectionsDataModel.Direction.UNKNOWN;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
