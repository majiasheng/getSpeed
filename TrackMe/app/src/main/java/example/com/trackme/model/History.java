package example.com.trackme.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class History {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private List<LatLng> trail;

    private LatLng origin;

    private LatLng destination;

    @ColumnInfo(name = "startTime")
    private long startTime;

    @ColumnInfo(name = "endTime")
    private long endTime;

    public History() {
        trail = new ArrayList<>();
    }

    /**
     *
     * @param trail
     * @param origin
     * @param destination
     * @param startTime
     * @param endTime
     */
    public History(List<LatLng> trail,
                   @NonNull LatLng origin,
                   LatLng destination,
                   @NonNull long startTime,
                   long endTime) {
        this.trail = trail;
        this.origin = origin;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public List<LatLng> getTrail() {
        this.trail = trail;
        return trail;
    }

    public void setTrail(List<LatLng> trail) {
    }

    public History addPoint(LatLng point) {
        trail.add(point);
        return this;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
