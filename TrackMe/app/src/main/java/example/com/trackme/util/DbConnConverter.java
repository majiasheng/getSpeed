package example.com.trackme.util;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import example.com.trackme.persistence.TrackMeDatabase;

public class DbConnConverter {
    @TypeConverter
    public static TrackMeDatabase stringToDb(String db) {
        if (db == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<TrackMeDatabase>() {}.getType();
        return gson.fromJson(db, type);
    }

    @TypeConverter
    public static String dbToString(TrackMeDatabase db) {
        if (db==null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<TrackMeDatabase>(){}.getType();
        return gson.toJson(db, type);
    }
}
