package example.com.trackme;

import com.google.android.gms.maps.model.Polyline;

import java.util.List;

import example.com.trackme.model.History;

public class TrackMe {

    private String username;
    List<History> histories;

    public TrackMe(String username, List<History> histories) {
        this.username = username;
        this.histories = histories;
    }

}
