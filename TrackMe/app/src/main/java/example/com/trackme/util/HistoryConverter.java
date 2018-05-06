package example.com.trackme.util;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import example.com.trackme.model.History;

public class HistoryConverter {
    @TypeConverter
    public static History stringToHistory(String historyString) {
        if (historyString == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<History>() {}.getType();
        return gson.fromJson(historyString, type);
    }

    @TypeConverter
    public static String historyToString(History history) {
        if (history==null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<History>(){}.getType();
        return gson.toJson(history, type);
    }
}
