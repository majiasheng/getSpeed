package example.com.trackme;

import android.Manifest;
import android.arch.persistence.room.Room;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import example.com.trackme.model.History;
import example.com.trackme.model.TrackFinishDialog;
import example.com.trackme.persistence.TrackMeDatabase;
import example.com.trackme.util.HistoryConverter;

/**
 * Reference [https://developers.google.com/maps/documentation/android-api/current-place-tutorial]
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final long UPDATE_INTERVAL = 3000L; // ms
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;

    private Button tracking_switch;
    private Button delete_btn;
    private TextView latLng_text;

    private boolean started;
    private boolean ended;
    private LatLng origin;
    private LatLng destination;
    private Marker originMarker;
    private Marker dstMarker;
    private LatLng currentPosition;
    private Polyline trail;
    private List<Polyline> predictions;

    // predictions related
    private Polyline predictedTrail;

    private Marker predictionDstMarker;


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private History currentHistory;

    // db
    private TrackMeDatabase db;

    private TrackFinishDialog dialog;

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
                updateToCurrentPosition(false);
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

    /**
     * Refreshes map settings every time before tracking session starts.
     * Checks permission first, then determine whether enable "current location"
     * button and location update.
     */
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
                mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
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
        ended = false;
        refresh();
        updateToCurrentPosition(true);

    }

    /**
     * Predicts users trail based on origin and time.
     * @param origin
     * @param currentTime
     */
    public void predict(LatLng origin, long currentTime) {

        History prediction = TrackMe.getPrediction(db, origin, currentTime);

        if (prediction != null) {
            System.out.println(
                    "++++++++++++++++++++++++++++++++\n"
                    + ">> Prediction: \n"
                    + prediction.toString()
                    + "\n++++++++++++++++++++++++++++++++");
            // mark dst
            predictionDstMarker = markPosition(prediction.getDestination(),
                    "Predicted Destination",
                    "You want to come here?");
            predictionDstMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            // draw trajectory
            predictedTrail = mMap.addPolyline(new PolylineOptions()
                    .addAll(prediction.getTrail())
                    .width(5)
                    .color(Color.GRAY));
//            predictedTrail = mMap.addPolyline(new PolylineOptions()
//                    .add(origin)
//                    .width(5)
//                    .color(Color.GRAY));
//            predictedTrail.setPoints(prediction.getTrail());
            System.out.println("predictedTrail: " + predictedTrail.getPoints().size());

        }

    }

    private void stopTracking() {
        //TODO: set button text to "Start" (in green)
        tracking_switch.setText(getString(R.string.start));
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

        started = false;
        ended = true;
        updateToCurrentPosition(false);
    }

    private void init() {
        initDb();
        // initialize global fields
        started = false; // global flag for the tracking state, default false
        ended = false;
        predictions = new ArrayList<>();
        tracking_switch = (Button) findViewById(R.id.tracking_switch);
        delete_btn = (Button) findViewById(R.id.delete_btn);
        latLng_text = (TextView) findViewById(R.id.latLng);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        dialog = new TrackFinishDialog();

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

        // register button onclick listener
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });

        /** Defines location update call back
         *  this is where the trail gets drawn
         */
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // draw trail
                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    latLng_text.setText("Lat: " + currentPosition.latitude + ", Long:" + currentPosition.longitude);
                    if (trail != null) {
                        List<LatLng> pts = trail.getPoints();
                        pts.add(currentPosition);
                        trail.setPoints(pts);
                    }
                }
            }
        };

    }

    private void initDb() {
        if (db == null) {
            db = Room.databaseBuilder(getApplicationContext(),
                    TrackMeDatabase.class, "trackMe")
                    .allowMainThreadQueries()
                    .build();
        }
    }

    private Marker markPosition(LatLng pos, String title, String snippet) {
        return mMap.addMarker(new MarkerOptions()
                .title(title)
                .snippet(snippet)
                .position(pos));
    }

    /**
     * Resets global parameters and map when a tracking session stops.
     */
    private void reset() {
        if (originMarker != null)
            originMarker.remove();
        if (dstMarker != null)
            dstMarker.remove();
        if (trail != null)
            trail.remove();
        if (predictedTrail != null)
            predictedTrail.remove();
        if (predictionDstMarker != null)
            predictionDstMarker.remove();
        //TODO: remove lat long from screen
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /**
     * Updates and goes to current position.
     * Called only on tracking session start/end and for the starting up of the app.
     */
    public void updateToCurrentPosition(final boolean newlyStarted) {

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

                            // zoom to current position
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, DEFAULT_ZOOM));

                            // do this only when tracking session is newly started
                            if (newlyStarted) {
                                long currentTime = System.currentTimeMillis();
                                origin = currentPosition;
                                currentHistory = new History(null, origin, null, currentTime, -1);
                                trail = mMap.addPolyline(new PolylineOptions()
                                        .add(origin)
                                        .width(5)
                                        .color(Color.RED));
                                originMarker = markPosition(origin, "Origin", "You started here :)");

                                predict(origin, currentTime);

                            }

                            // do this only when tracking session ended
                            if (ended) {
                                destination = currentPosition;
                                // save to histories
                                currentHistory.setDestination(destination);
                                currentHistory.setEndTime(System.currentTimeMillis());
                                currentHistory.setTrail(trail.getPoints());
                                // this may not be necessary since the dialog box will cover it
                                dstMarker = markPosition(destination, "Destination", "");
                                //TODO: get lat long's location name
                                String message = "Origin: (" + origin.latitude + ", " + origin.longitude + ")\n"
                                        + "Destination: (" + destination.latitude + ", " + destination.longitude + ")";

                                // history will be prompted to save in dialog's handler
                                showDialog(message, HistoryConverter.historyToString(currentHistory));
                                reset();
                            }
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

    //TODO: override onResume and onPause

    /**
     * Shows dialog to notify user that the tracking session is finish.
     * Prompts user to save trail to history.
     * @param message
     * @param history
     */
    public void showDialog(String message, String history/*, Marker marker*/) {
        Bundle bundle = new Bundle();
        bundle.putString("msg_key", message);
        bundle.putString("history_key", history);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "finish_tracking");
    }

    public void clearHistory() {
        db.historyDao().deleteAll();
    }

}
