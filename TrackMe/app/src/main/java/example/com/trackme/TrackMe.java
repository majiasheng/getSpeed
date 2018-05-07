package example.com.trackme;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import example.com.trackme.model.History;
import example.com.trackme.persistence.TrackMeDatabase;

public class TrackMe {

    // Define the theoretical deviation range/radius of origin, R,
    // to be 860m away from the actual origin.
    public static final float R = 860.0f;

    // Define the theoretical deviation range of departure time, T,
    // to be 2 hour (7200 * 1000 milliseconds) away from the actual departure time.
    public static final long T = 7200000L;

    public TrackMe() {}

    /**
     * Retrieves list of candidate predictions and finds the first matching prediction.
     * @param db
     * @param newOrigin
     * @param newStartTime
     * @return prediction
     */
    public static History getPrediction(@NonNull TrackMeDatabase db, @NonNull LatLng newOrigin, @NonNull long newStartTime) {

        List<History> candidates = db.historyDao().getCandidatePredictions(newStartTime);
        if (candidates == null) {
            // get all entries if no result for the given new departure time
            candidates = db.historyDao().getAll();
        }

        for (History candidate : candidates) {
            if (isWithinRadius(candidate.getOrigin(), newOrigin))
                return candidate;
        }

        return null;
    }

    private static boolean isWithinRadius(@NonNull LatLng oldOrigin, @NonNull LatLng newOrigin) {
        Location oldLocation = new Location("");
        oldLocation.setLatitude(oldOrigin.latitude);
        oldLocation.setLongitude(oldOrigin.longitude);
        Location newLocation = new Location("");
        newLocation.setLatitude(newOrigin.latitude);
        newLocation.setLongitude(newOrigin.longitude);

        return oldLocation.distanceTo(newLocation) <= R;
    }
}
