package example.com.trackme.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import example.com.trackme.model.History;

@Database(entities = {History.class}, version = 1)
@TypeConverters({LatLngListConverter.class, LatLngConverter.class})
public abstract class TrackMeDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}
