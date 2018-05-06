package example.com.trackme.persistence;

import android.arch.persistence.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.List;

public class LatLngListConverter {

    @TypeConverter
    public static List<LatLng> stringToLatLngList(String latLngListString) {
        if (latLngListString == null) return null;
        Gson gson = new Gson();
        Type listType = new TypeToken<List<LatLng>>() {}.getType();
        return gson.fromJson(latLngListString, listType);
    }

    @TypeConverter
    public static String latLngListToString(List<LatLng> latLngList) {
        if (latLngList==null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<LatLng>>(){}.getType();
        return gson.toJson(latLngList,type);
    }
}
