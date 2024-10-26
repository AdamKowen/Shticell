package utils;


import adapter.CellDtoAdapter;
import adapter.CoordinateAdapter;
import adapter.CoordinateCellDtoMapAdapter;
import adapter.EffectiveValueAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dto.CellDto;
import sheet.cell.api.EffectiveValue;
import sheet.coordinate.api.Coordinate;

import java.util.Map;

public class JSONUtils {

    //private static Gson gson = new Gson();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CellDto.class, new CellDtoAdapter())
            .registerTypeAdapter(Coordinate.class, new CoordinateAdapter())
            .registerTypeAdapter(new TypeToken<Map<Coordinate, CellDto>>(){}.getType(), new CoordinateCellDtoMapAdapter())
            .registerTypeAdapter(EffectiveValue.class, new EffectiveValueAdapter())
            .setPrettyPrinting()
            .create();


    // המרת אובייקט ל-JSON
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    // המרת JSON לאובייקט
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

}
