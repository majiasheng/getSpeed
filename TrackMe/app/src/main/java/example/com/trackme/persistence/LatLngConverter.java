package example.com.trackme.persistence;

import android.arch.persistence.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class LatLngConverter {

    @TypeConverter
    public static LatLng stringToLatLng(String latLngString) {
        if (latLngString == null) return null;
        Gson gson = new Gson();
        Type type= new TypeToken<LatLng>() {}.getType();
        return gson.fromJson(latLngString, type);
    }

    @TypeConverter
    public static String latLngToString(LatLng latLng) {
        if (latLng==null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<LatLng>(){}.getType();
        return gson.toJson(latLng,type);
    }
}
