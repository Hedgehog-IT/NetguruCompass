package com.hedgehog.compass;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends ActionBarActivity {

    private EditText lat;
    private EditText lng;
    private boolean doubleBackToExitPressedOnce;
    private CompassView compassView;
    private Button showDirectionButton;
    private Button mapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        setViews();
        //        checkGPS();
        initButtons();
    }

    private void setViews() {
        lat = (EditText) findViewById(R.id.lat);
        lng = (EditText) findViewById(R.id.lng);
        compassView = (CompassView) findViewById(R.id.compass_view);
        showDirectionButton = (Button) findViewById(R.id.showDirectionButton);
        mapButton = (Button) findViewById(R.id.mapButton);
        compassView.setCompassListener(new CompassListener() {
            @Override
            public void providerEnabled() {
            }

            @Override
            public void providerDisabled() {
                //                checkGPS();
            }
        });
    }

    private void initButtons() {
        showDirectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng destination = new LatLng(Double.parseDouble(lat.getText().toString()),
                        Double.parseDouble(lng.getText().toString()));
                compassView.setDestination(destination);
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapDialog mapDialog = new MapDialog();
                mapDialog.setListener(new MapDialogListener() {
                    @Override
                    public void onPointSelected(LatLng point) {
                        setDestination(point);
                    }
                });
                mapDialog.setStyle(DialogFragment.STYLE_NORMAL,
                        android.R.style.Theme_Holo_Wallpaper_NoTitleBar);
                mapDialog.show(getFragmentManager(), "Dialog Fragment");
            }
        });
    }

    private void checkGPS() {
        final LocationManager manager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert).setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int id) {
                    dialog.cancel();
                }
            });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void setDestination(LatLng point) {
        lat.setText(String.valueOf(point.latitude));
        lng.setText(String.valueOf(point.longitude));
        compassView.setDestination(point);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.exit_conf, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassView.onPause();
    }
}
