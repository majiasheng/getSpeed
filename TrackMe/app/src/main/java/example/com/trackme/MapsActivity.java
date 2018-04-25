package example.com.trackme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * Reference [https://developers.google.com/maps/documentation/android-api/current-place-tutorial]
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;

    private boolean started;

    private Button tracking_switch;
    private TextView latLng_text;

    private LatLng origin;
    private LatLng destination;
    private Marker originMarker;
    private Marker dstMarker;
    private LatLng currentPosition;

    private Polyline trail;

    private LocationCallback mLocationCallback;

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

        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                updateCurrentPosition();
            }
        } catch (SecurityException e) {
            requestPermission();
        }

        /**
         * User toggles "Start Tracking"
         *
         * Every 2 seconds, request a location update,
         * then add new location (new LatLng) to polyline
         *
         * (do a time check)
         */

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

    }

    //TODO: rename this to be more meaningful
    private void preprocessing() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                // request location updates
                mFusedLocationProviderClient.requestLocationUpdates(
                        createLocationRequest(),
                        mLocationCallback,
                        null /* Looper */);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;

            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void startTracking() {

        started = true;

        // set button text to "Stop" (in red)
        tracking_switch.setText("Stop");

        preprocessing();

        updateCurrentPosition();
        //FIXME: this may not work because of the currentPosition is updated in an event listener
        origin = currentPosition;

        trail = mMap.addPolyline(new PolylineOptions()
                .add(origin)
                .width(5)
                .color(Color.RED));

        originMarker = markPosition(origin);
    }

    //TODO:
    private void stopTracking() {
        started = false;
        // set button text to "Start" (in green)
        tracking_switch.setText("Start");
        // save trail

        reset();

    }

    private void init() {
        // initialize global fields
        started = false; // global flag for the tracking state, default false
        tracking_switch = (Button) findViewById(R.id.tracking_switch);
        latLng_text = (TextView) findViewById(R.id.latLng);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // register button onclick listener
        tracking_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (started) {
                    stopTracking();
                } else {
                    startTracking();
                }
            }
        });

        // define location update call back
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    //TODO: polyline?
                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    latLng_text.setText("Lat: "+currentPosition.latitude + ", Long:" + currentPosition.longitude);
                    List<LatLng> pts = trail.getPoints();
                    pts.add(currentPosition);
                    trail.setPoints(pts);
                }
            }
        };

    }

    private Marker markPosition(LatLng pos) {
        return mMap.addMarker(new MarkerOptions()
                .title("Origin")
                .snippet("You started from here ;) ")
                .position(pos));
    }

    /**
     * Resets global parameters and map when a tracking session stops.
     */
    private void reset() {
        //TODO:
        // remove markers
        originMarker.remove();
        // mMap.

        // clear polyline?
        trail.remove();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

    }

    /**
     * Updates and goes to current position.
     */
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

                            // go to current position
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, DEFAULT_ZOOM));
                        } else {
                            // do nothing
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}
