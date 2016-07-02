package com.hedgehog.compass;

import com.google.android.gms.maps.model.LatLng;

public interface MapDialogListener {
    void onPointSelected(LatLng point);
}
