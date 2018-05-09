package example.com.trackme.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import example.com.trackme.persistence.LatLngConverter;
import example.com.trackme.persistence.LatLngListConverter;

@Entity
public class History {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @TypeConverters({LatLngListConverter.class, LatLngConverter.class})
    @ColumnInfo(name = "trail")
    private List<LatLng> trail;

    @TypeConverters(LatLngConverter.class)
    @ColumnInfo(name = "origin")
    private LatLng origin;

    @TypeConverters(LatLngConverter.class)
    @ColumnInfo(name = "destination")
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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {

        return id;
    }

    public List<LatLng> getTrail() {
        return trail;
    }

    public void setTrail(List<LatLng> trail) {
        this.trail = trail;
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

    @Override
    public String toString() {
        return "Origin: (" + origin.latitude + ", " + origin.longitude + ")\n"
                +  "Destination: (" + destination.latitude + ", " + destination.longitude + ")\n"
                + "Time start: " + new Date(startTime) + "\n"
                + "Time end: " + new Date(endTime) + "\n"
                + "Points in trail: " + trail.size();
    }
}
