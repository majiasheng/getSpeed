package example.com.trackme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import example.com.trackme.model.History;

/**
 * Reference [https://developers.google.com/maps/documentation/android-api/current-place-tutorial]
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;

    private Button tracking_switch;
    private TextView latLng_text;

    private boolean started;
    private LatLng origin;
    private LatLng destination;
    private Marker originMarker;
    private Marker dstMarker;
    private LatLng currentPosition;
    private Polyline trail;


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private List<History> histories;
    private History currentHistory;

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
                updateToCurrentPosition();
            }
        } catch (SecurityException e) {
            requestPermission();
        }
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = false;
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            );
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
    private void refresh() {
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
        // set button text to "Stop" (in red)
        tracking_switch.setText(getString(R.string.stop));
        started = true;

        refresh();

        updateToCurrentPosition();
        //FIXME: this may not work because of the currentPosition is updated in an event listener
        origin = currentPosition;

        currentHistory = new History(new ArrayList<LatLng>(), origin, null, new Date(System.currentTimeMillis()), null);

        trail = mMap.addPolyline(new PolylineOptions()
                .add(origin)
                .width(5)
                .color(Color.RED));

        originMarker = markPosition(origin);
    }

    //TODO:
    private void stopTracking() {
        // set button text to "Start" (in green)
        tracking_switch.setText(getString(R.string.start));
        started = false;
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

        updateToCurrentPosition();

        // save to histories
        currentHistory.setDestination(currentPosition);
        currentHistory.setEndTime(new Date(System.currentTimeMillis()));
        saveToHistory(currentHistory);

        reset();

    }

    private void init() {
        // initialize global fields
        started = false; // global flag for the tracking state, default false
        // TODO: arraylist for history is a temporary solution, should use a database
        histories = new ArrayList<>();
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
                    // draw trail
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
        originMarker.remove();
        trail.remove();
    }

    private void saveToHistory(History history) {
        histories.add(history);
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /**
     * Updates and goes to current position.
     */
    public void updateToCurrentPosition() {

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
