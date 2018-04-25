package example.com.trackme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Reference [https://developers.google.com/maps/documentation/android-api/current-place-tutorial]
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;

    private boolean started;

    private Button tracking_switch;

    private LatLng origin;
    private LatLng destination;
    private LatLng currentPosition;

    //TODO: use time instead?
    private float lastUpdatedTime;
    private float currentTime;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        init();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        requestPermission();

        if (mLocationPermissionGranted) {
            System.out.println("b4\n\n");
            updateCurrentPosition();
            System.out.println("af\n\n");

        }

        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0), new LatLng(41.889, -87.622))
                .width(5)
                .color(Color.RED));
        /**
         * User toggles "Start Tracking"
         *
         * Every 2 seconds, request a location update,
         * then add new location (new LatLng) to polyline
         *
         * (do a time check)
         */

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        mLocationPermissionGranted = false;

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    System.out.println("Permission granted");
                }
            }
            default: {
                System.out.println("Permission denied");
            }
        }

        updateLocationUI();

    }

    private void init() {
        // zoom to current location


        // global flag for the tracking state, default false
        started = false;

        // register button onclick listener
        tracking_switch = (Button) findViewById(R.id.tracking_switch);
        tracking_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO:
                if (started) {
                    started = false;
                    // set button text to "Start" (in green)
                    tracking_switch.setText("Start");
                    // save trail

                    reset();
                } else {
                    started = true;
                    // set button text to "Stop" (in red)
                    tracking_switch.setText("Stop");

                    // get current position, then set origin to current position

                    // add origin to
                }
            }
        });

//        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = false;
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mLocationPermissionGranted = true;
        }
    }


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 3, this);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;

            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



    /**
     * Resets global parameters and map when a tracking session stops.
     */
    private void reset() {
        //TODO:
        // clear polyline?
    }

    public void updateCurrentPosition() {

        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            currentPosition = new LatLng(
                                    mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()
                            );

                            System.out.println("middle");

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, DEFAULT_ZOOM));

                        } else {
                            // do nothing
                            // mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, DEFAULT_ZOOM));
        locationManager.removeUpdates(this);
        System.out.println(currentPosition.toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
