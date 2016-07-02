package com.hedgehog.compass;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CompassView extends RelativeLayout implements SensorEventListener, LocationListener {
    private View rootView;
    private ImageView arrow;
    private ImageView direction;
    private Location currentLoc;
    private Location destination;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private LocationManager mLocationManager;
    private CompassListener compassListener;

    public CompassView(Context context) {
        super(context);
        init(context);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.compass_layout, this);
        arrow = (ImageView) rootView.findViewById(R.id.compass_arrow);
        direction = (ImageView) rootView.findViewById(R.id.direction);

        destination = new Location(LocationManager.PASSIVE_PROVIDER);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this);

        currentLoc = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        mLocationManager.addGpsStatusListener(new GpsStatus.Listener() {

            @Override
            public void onGpsStatusChanged(int event) {
                if (event == GpsStatus.GPS_EVENT_STARTED) {
                    compassListener.providerEnabled();
                } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
                    compassListener.providerDisabled();
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        arrow.setRotation(-event.values[0]);
        float bearTo = 0;
        if (currentLoc != null) {
            bearTo = (currentLoc.bearingTo(destination));
        }
        direction.setRotation(bearTo - event.values[0]);
    }

    public void setDestination(LatLng point) {
        destination.setLongitude(point.longitude);
        destination.setLatitude(point.latitude);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            direction.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLoc = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        compassListener.providerEnabled();
    }

    @Override
    public void onProviderDisabled(String provider) {
        compassListener.providerDisabled();
    }

    public void setCompassListener(CompassListener compassListener) {
        this.compassListener = compassListener;
    }

    public void onResume() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        mSensorManager.unregisterListener(this);
    }
}
