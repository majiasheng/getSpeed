package example.com.trackme.util;

import android.arch.persistence.room.TypeConverter;

import java.util.Collections;
import java.util.List;

import example.com.trackme.model.History;

public class HisotryTypeConverter {

//    Gson gson = new Gson();
//
//    @TypeConverter
//    public static List<History> stringToHistorytList(String data) {
//        if (data == null) {
//            return Collections.emptyList();
//        }
//
//        Type listType = new TypeToken<List<History>>() {}.getType();
//
//        return gson.fromJson(data, listType);
//    }
//
//    @TypeConverter
//    public static String historyListToString(List<History> someObjects) {
//        return gson.toJson(someObjects);
//    }
}
