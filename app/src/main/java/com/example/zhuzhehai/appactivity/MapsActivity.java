package com.example.zhuzhehai.appactivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;


    private static final int PLACE_PICKER_REQUEST = 1;
    public static final String TAG = "TAG";
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0;
    private static final int REQUEST_LOCATION = 100;
    private static final int COARSE_LOCATION = 200;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (googleServcesAvaliable()) {
            Toast.makeText(this, "You are in!!", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_maps);


            Button button = (Button) findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // auto completed search
                    SupportPlaceAutocompleteFragment();
                }
            });

            //show map
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(MapsActivity.this);


        } else {
            Toast.makeText(this, "not connect", Toast.LENGTH_LONG).show();
        }
    }

    public boolean googleServcesAvaliable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvaliable = api.isGooglePlayServicesAvailable(this);
        if (isAvaliable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvaliable)) {
            Dialog dialog = api.getErrorDialog(this, isAvaliable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "not connect", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void MoveCameraAndMarker(Place place, float Zoom) {
        LatLng ll = place.getLatLng();
        if (marker != null) {
            marker.remove();
        }
        MarkerOptions options = new MarkerOptions().title(place.getName() + "").position(new LatLng(ll.latitude, ll.longitude));
        marker = mMap.addMarker(options);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, Zoom);
        mMap.moveCamera(update);
        Toast.makeText(MapsActivity.this, "You are in " + place.getName() + "", Toast.LENGTH_LONG).show();
    }

    private void SupportPlaceAutocompleteFragment() {
        //do autocomleted.
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
//                            .setFilter(new AutocompleteFilter.Builder()
//                                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
//                                    .build())
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO:Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO:Handle the error.
        }

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            //after click the search result;
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
//                Toast.makeText(MapsActivity.this, "Current Place : " + place.getName(), Toast.LENGTH_LONG).show();
                MoveCameraAndMarker(place, 13); // move the Camera And Mark the palce;
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO:Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapsActivity.this, "ACCESS_FINE_LOCATION", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapsActivity.this, "coarse_localtion", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(MapsActivity.this, "Not permission", Toast.LENGTH_LONG).show();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);


        //doing GPS!!!
//        mGoogleApiClient = new GoogleApiClient
//                .Builder(this)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//        mGoogleApiClient.connect();


    }


    LocationRequest myLocationR;
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationR = LocationRequest.create();
        myLocationR.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        myLocationR.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, myLocationR, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
            if (location == null) {
                Toast.makeText(MapsActivity.this, "Can't Find Location", Toast.LENGTH_LONG).show();
            } else {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(MapsActivity.this, ll.latitude + " " + ll.longitude, Toast.LENGTH_LONG).show();
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
                mMap.animateCamera(update);
            }
//        myLocationM.removeUpdates();

    }

    // TODO: Please implement GoogleApiClient.OnConnectionFailedListener to
    // handle connection failures.
}
