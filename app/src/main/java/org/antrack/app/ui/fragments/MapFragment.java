package org.antrack.app.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.antrack.app.ui.State;
import org.antrack.app.ui.U;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import app.R;

public class MapFragment extends BaseFragment implements OnMapReadyCallback {
    final String TAG = "MapFragment";

    private MapView mapView = null;
    private Location currentLocation;

    private String locationFile;
    private String locationCmd;

    private class Location {
        String date = "0";
        String time = "0";
        String lat = "0";
        String lng = "0";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Otherwise GetActivity() return null after orientation change
        setRetainInstance(true);

        if (!Mod.check(Mod.LOCATE)) {
            showNoModule(Mod.LOCATE);
            return null;
        }

        locationFile = Mod.getFile(Mod.LOCATE);
        locationCmd = Mod.getCommand(Mod.LOCATE);

        View view = inflater.inflate(R.layout.fragment_mapview, container, false);

        currentLocation = new Location();

        mapView = (MapView) view.findViewById(R.id.fragment_mapview_map);
        mapView.onCreate(savedInstanceState);

        onFileUpdate();

        U.runCommandAsync(locationCmd);
        if (!State.device.isMain()) {
            U.getFileAsync(locationFile);
        }

        return view;
    }

    @Override
    public String getWatchFile() {
        return locationFile;
    }

    @Override
    public void onFileUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!readFile()) {
                    showNoData();
                    return;
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapView.setVisibility(View.VISIBLE);
                        mapView.getMapAsync(MapFragment.this);
                        hideAllMessages();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Clear map from previous markers
        map.clear();

        // Add marker
        map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(currentLocation.lat), Double.parseDouble(currentLocation.lng)))
                .title(currentLocation.date + " " + currentLocation.time));

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng
                (Double.parseDouble(currentLocation.lat), Double.parseDouble(currentLocation.lng)), 15);
        map.animateCamera(cameraUpdate);

    }

    private boolean readFile() {
        String path = U.getLocalPath(locationFile);

        if (!new File(path).exists()) {
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(U.getLocalPath(locationFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] loc = line.split(" ");
                if (loc.length < 4)
                    continue;
                currentLocation.date = loc[0];
                currentLocation.time = loc[1];
                currentLocation.lat  = loc[2];
                currentLocation.lng  = loc[3];
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read apps file: " + e);
            return false;
        }

        return true;
    }

    @Override
    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
