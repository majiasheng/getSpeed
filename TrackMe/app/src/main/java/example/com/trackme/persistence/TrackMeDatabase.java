package example.com.trackme.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import example.com.trackme.model.History;

@Database(entities = {History.class}, version = 1)
public abstract class TrackMeDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}
