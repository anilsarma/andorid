package com.example.asarma.njrails.route;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.asarma.njrails.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class RouteGoogleMapFragment extends Fragment implements  OnMapReadyCallback {
    public static final String ARG_OBJECT = "object";
    int multiplier=3;

    FragmentActivity myContext;
    ArrayList<HashMap<String, String>> stations;

    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        stations = args.getParcelable("data");
        System.out.println("got parcel:" + stations.size());
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
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
