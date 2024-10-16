package utils;


import com.google.gson.Gson;

public class JSONUtils {

    private static Gson gson = new Gson();

    // המרת אובייקט ל-JSON
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    // המרת JSON לאובייקט
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}
