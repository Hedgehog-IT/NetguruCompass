package com.hedgehog.netgurucompass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity implements SensorEventListener, LocationListener, OnMapReadyCallback {

    private ImageView arrow;
    private ImageView direction;
    private EditText lat;
    private EditText lng;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Location currentLoc;
    private Location destination;
    private GoogleMap map;
    private View mapFragment;
    private boolean doubleBackToExitPressedOnce;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView compass_rose = (ImageView) findViewById(R.id.compass_rose);
        arrow = (ImageView) findViewById(R.id.compass_arrow);
        direction = (ImageView) findViewById(R.id.direction);
        lat = (EditText) findViewById(R.id.lat);
        lng = (EditText) findViewById(R.id.lng);
        Button showDirectionButton = (Button) findViewById(R.id.showDirectionButton);
        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapFragment = findViewById(R.id.mapFragment);
        checkGPS();
        destination = new Location(LocationManager.PASSIVE_PROVIDER);
//        destination.setLatitude(Double.parseDouble(lat.getText().toString()));
//        destination.setLongitude(Double.parseDouble(lng.getText().toString()));

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this);

        currentLoc = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        showDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    destination.setLatitude(Double.parseDouble(lat.getText().toString()));
                    destination.setLongitude(Double.parseDouble(lng.getText().toString()));
                    if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        direction.setVisibility(View.VISIBLE);
                    }
                    checkGPS();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Something wrong with data...", Toast.LENGTH_LONG).show();
                }

            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.setVisibility(View.VISIBLE);
            }
        });

        mapFragment.setVisibility(View.GONE);

        SupportMapFragment fragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction() .add(R.id.map, fragment).commit();
        fragment.getMapAsync(this);
    }

    private void checkGPS() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        arrow.setRotation(-event.values[0]);
        float bearTo = 0;
        if(currentLoc != null){
            bearTo = (currentLoc.bearingTo(destination));
        }
        direction.setRotation(bearTo - event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
        map.setTrafficEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        MapsInitializer.initialize(this);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                map.clear();
                setDestination(point);
                Marker marker = map.addMarker(new MarkerOptions().position(point));
                mapFragment.setVisibility(View.GONE);
            }
        });
        GoogleMap.OnMarkerClickListener markerListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                IconGenerator factory = new IconGenerator(Geometeo.getAppContext());
//                Bitmap icon = factory.makeIcon("no siema parówo");
//                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                return false;
            }
        };

        map.setOnMarkerClickListener(markerListener);
    }

    private void setDestination(LatLng point) {
        destination.setLongitude(point.longitude);
        destination.setLatitude(point.latitude);
        lat.setText(point.latitude + "");
        lng.setText(point.longitude + "");
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            direction.setVisibility(View.VISIBLE);
        }
        checkGPS();
    }

    @Override
    public void onBackPressed() {
        if(mapFragment.getVisibility() == View.VISIBLE){
            mapFragment.setVisibility(View.GONE);
        }else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to quit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
}
