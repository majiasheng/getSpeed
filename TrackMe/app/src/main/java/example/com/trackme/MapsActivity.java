package example.com.trackme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean started;

    private Button tracking_switch;

    Location currentLocation;
    private LatLng origin;
    private LatLng destination;
    private LatLng currentPosition;

    //TODO: use time instead?
    private float lastUpdatedTime;
    private float currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap = googleMap;

//        mMap.setMyLocationEnabled(true);
//        currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//        System.out.println(currentPosition);
//        mMap.addMarker(new MarkerOptions().position(currentPosition).title("You started from here :)"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));

        LatLng mapCenter = new LatLng(41.889, -87.622);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 13));
//
//        // Flat markers will rotate when the map is rotated,
//        // and change perspective when the map is tilted.
//        mMap.addMarker(new MarkerOptions()
//                .position(mapCenter)
//                .flat(true)
//                .rotation(245));
//
//        CameraPosition cameraPosition = CameraPosition.builder()
//                .target(mapCenter)
//                .zoom(13)
//                .bearing(90)
//                .build();
//
//        // Animate the change in camera view over 2 seconds
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
//                2000, null);
//
//        Polyline line = mMap.addPolyline(new PolylineOptions()
//                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0), new LatLng(41.889, -87.622))
//                .width(5)
//                .color(Color.RED));
        /**
         * User toggles "Start Tracking"
         *
         * Every 2 seconds, request a location update,
         * then add new location (new LatLng) to polyline
         *
         * (do a time check)
         */

    }

    private void init() {
        // zoom to current location


        // global flag for the tracking state, default false
        started = false;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // register button onclick listener
        tracking_switch = (Button) findViewById(R.id.tracking_switch);
        tracking_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO:
                if (started) {
                    started = false;
                    // set button text to "Start" (in green)
                    tracking_switch.setText("Stop");
                    // save trail
                    reset();
                } else {
                    started = true;
                    origin = currentPosition;
                    tracking_switch.setText("Start");
                    // set button text to "Stop" (in red)
                    // get current position, then set origin to current position
                    // add origin to
                }
            }
        });

    }

    /**
     * Resets global parameters and map when a tracking session stops.
     */
    private void reset() {
        //TODO:
        // clear polyline?
    }

}
