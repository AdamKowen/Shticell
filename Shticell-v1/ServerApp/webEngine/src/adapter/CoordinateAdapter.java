package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateImpl;

import java.io.IOException;

public class CoordinateAdapter extends TypeAdapter<Coordinate> {

    @Override
    public void write(JsonWriter out, Coordinate coordinate) throws IOException {
        out.beginObject();
        out.name("row").value(coordinate.getRow());
        out.name("column").value(coordinate.getColumn());
        out.endObject();
    }

    @Override
    public Coordinate read(JsonReader in) throws IOException {
        int row = 0;
        int column = 0;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "row":
                    row = in.nextInt();
                    break;
                case "column":
                    column = in.nextInt();
                    break;
            }
        }
        in.endObject();

        return new CoordinateImpl(row, column);  // יצירת CoordinateImpl
    }
}
