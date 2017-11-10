package com.example.asarma.njrails.route;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.asarma.njrails.ParcelResult;
import com.example.asarma.njrails.R;
import com.example.asarma.njrails.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class RouteGoogleMapFragment extends Fragment implements  OnMapReadyCallback {
    public static final String ARG_OBJECT = "object";
    int multiplier=3;

    FragmentActivity myContext;
    ParcelResult stations;

    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        stations = args.getParcelable("data");
        System.out.println("got parcel:" + stations.data.size());
        // get the loction and station details form the bundle.
        final View rootView = inflater.inflate(R.layout.route_map_activity, container, false);

        // SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
     //   if (rootView.getActivity()==null)
       if (true) {
            Toast.makeText(rootView.getContext(), "Activity is null " + getActivity().getSupportFragmentManager(), Toast.LENGTH_LONG).show();
            //return rootView;
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // and move the map's camera to the same location.
        //LatLng sydney = new LatLng(-33.852, 151.211);
        //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        LatLng start = null;
        PolylineOptions pt = new PolylineOptions();
                //.add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
        for (int i = 0; i < stations.data.size(); i++) {

            LatLng tmp = new LatLng(Double.parseDouble(stations.data.get(i).get("stop_lat")), Double.parseDouble(stations.data.get(i).get("stop_lon")));
            if ( i == 0 ) {
                start = tmp;
            }
            pt.add(tmp);
            String title = Utils.capitalize(stations.data.get(i).get("stop_name")) + " " + Utils.formatToLocalTime(Utils.parseLocalTime(stations.data.get(i).get("arrival_time")));
            googleMap.addMarker(new MarkerOptions().position(tmp).title(title));
        }
        Polyline line=googleMap.addPolyline(pt.width(5).color(Color.GREEN));
        if(start!=null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(start));
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());

// Zoom out to zoom level 10, animating with a duration of 2 seconds.
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(6), 2000, null);

        }
    }
}
