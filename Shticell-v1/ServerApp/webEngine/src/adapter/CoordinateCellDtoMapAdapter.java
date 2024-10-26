package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.reflect.TypeToken;
import dto.CellDto;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CoordinateCellDtoMapAdapter extends TypeAdapter<Map<Coordinate, CellDto>> {

    private final TypeAdapter<Coordinate> coordinateAdapter = new CoordinateAdapter();
    private final TypeAdapter<CellDto> cellDtoAdapter = new CellDtoAdapter();

    @Override
    public void write(JsonWriter out, Map<Coordinate, CellDto> map) throws IOException {
        out.beginObject();
        for (Map.Entry<Coordinate, CellDto> entry : map.entrySet()) {
            // Convert Coordinate to string (e.g., "row:column")
            String key = entry.getKey().getRow() + ":" + entry.getKey().getColumn();
            out.name(key);
            cellDtoAdapter.write(out, entry.getValue());
        }
        out.endObject();
    }

    @Override
    public Map<Coordinate, CellDto> read(JsonReader in) throws IOException {
        Map<Coordinate, CellDto> map = new HashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            // Convert string key back to Coordinate
            String key = in.nextName();
            String[] parts = key.split(":");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            Coordinate coordinate = new CoordinateImpl(row, column);
            CellDto cellDto = cellDtoAdapter.read(in);
            map.put(coordinate, cellDto);
        }
        in.endObject();
        return map;
    }
}
