package com.example.uber.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.uber.Common;
import com.example.uber.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "HomeFragment";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private SupportMapFragment mapFragment;

    private DatabaseReference onlineRef, currentUserRef, driverLocationRef;
    private GeoFire geoFire;
    private ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists())
                currentUserRef.onDisconnect().removeValue();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Snackbar.make(mapFragment.getView(), databaseError.getMessage()
                    , BaseTransientBottomBar.LENGTH_LONG).show();
        }
    };

    private HomeViewModel homeViewModel;


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        init();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return root;
    }

    @SuppressLint("MissingPermission")
    private void init() {
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        driverLocationRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER_LOCATION_REFERANCE);
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER_LOCATION_REFERANCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        geoFire = new GeoFire(driverLocationRef);

        registerOnlineSystem();

        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 19f));

                //updateLocation
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid()
                        , new GeoLocation(newPosition.latitude, newPosition.longitude)
                        , (key, error) -> {
                            if (error != null)
                                Snackbar.make(mapFragment.getView(), error.getMessage()
                                        , Snackbar.LENGTH_LONG).show();
                            else
                                Snackbar.make(mapFragment.getView(), "You're Online"
                                        , Snackbar.LENGTH_LONG).show();
                        });
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //check the permission
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                fusedLocationProviderClient.getLastLocation()
                                        .addOnSuccessListener(location -> {
                                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f));
                                        }).addOnFailureListener(e -> {
                                    Log.d(TAG, "onMyLocationButtonClick: " + e.getMessage());
                                    showToast(e.getMessage());
                                });
                                return true;
                            }
                        });

                        //set layout Button
                        View locationBtn = ((View) mapFragment.getView()
                                .findViewById(Integer.parseInt("1")).getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params =
                                (RelativeLayout.LayoutParams) locationBtn.getLayoutParams();

                        //Right Button
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0, 0, 0, 50);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        showToast("Permission " + permissionDeniedResponse.getPermissionName() + " was denied!");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest,
                                                                   PermissionToken permissionToken) {

                    }
                }).check();

        try {
            boolean success = mMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(getContext(), R.raw.uber_maps_style));
            if (!success)
                Log.d(TAG, "onMapReady: style pasring error");
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, "onMapReady: " + e.getMessage());
        }

    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}