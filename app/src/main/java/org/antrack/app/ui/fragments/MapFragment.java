package org.antrack.app.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.antrack.app.Init;
import org.antrack.app.ui.U;
import org.antrack.app.ui.V;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import app.R;

public class MapFragment extends BaseFragment implements OnMapReadyCallback {
    final String TAG = "MapFragment";

    private String locationFile = "/location";

    private MapView mapView;
    private Location currentLocation;

    private class Location {
        public String date = "0";
        public String time = "0";
        public String lat = "0";
        public String lng = "0";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        View view = inflater.inflate(R.layout.fragment_mapview, null);

        currentLocation = new Location();

        mapView = (MapView) view.findViewById(R.id.fragment_mapview_map);
        mapView.onCreate(savedInstanceState);

        onFileUpdate();

        U.runCommandAsync("locate");
        if (!U.isDeviceMain()) {
            U.getFileAsync(locationFile);
        }

        return view;
    }

    @Override
    public String getName() { return "Map"; }

    @Override
    public String getWatchFile() {
        return locationFile;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                blocked = true;
                if (readFile()) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                mapView.getMapAsync(MapFragment.this);
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng
                (Double.parseDouble(currentLocation.lat), Double.parseDouble(currentLocation.lng)), 15);
        map.animateCamera(cameraUpdate);

        // Add marker
        // FIXME при обновлении - два маркера
        map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(currentLocation.lat), Double.parseDouble(currentLocation.lng)))
                .title(currentLocation.date + " " + currentLocation.time));
    }

    private boolean readFile() {
        String path = U.getFullPath(locationFile);

        if (!new File(path).exists()) {
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(U.getFullPath(locationFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] loc = line.split(" ");
                if (loc.length < 4)
                    // FIXME надо выводить картинку, что данных нет
                    return false;
                currentLocation.date = loc[0];
                currentLocation.time = loc[1];
                currentLocation.lat  = loc[2];
                currentLocation.lng  = loc[3];
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read apps file: " + e);
        }

        return true;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
