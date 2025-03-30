package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dto.CellDto;
import dto.CellDtoImpl;
import dto.CellStyleDto;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CellDtoAdapter extends TypeAdapter<CellDto> {

    private final TypeAdapter<Coordinate> coordinateAdapter = new CoordinateAdapter();

    @Override
    public void write(JsonWriter out, CellDto cellDto) throws IOException {
        out.beginObject();

        out.name("coordinate");
        coordinateAdapter.write(out, cellDto.getCoordinate());

        out.name("originalValue").value(cellDto.getOriginalValue());

        // get value returns the effective value of cell
        out.name("value").value(cellDto.getValue());

        out.name("version").value(cellDto.getVersion());

        out.name("dependsOn");
        writeCoordinateList(out, cellDto.getDependsOn());

        out.name("influencingOn");
        writeCoordinateList(out, cellDto.getInfluencingOn());

        out.name("style");
        writeCellStyle(out, cellDto.getStyle());

        out.name("lastUserUpdated").value(cellDto.getLastUserUpdated());

        out.endObject();
    }

    @Override
    public CellDto read(JsonReader in) throws IOException {
        Coordinate coordinate = null;
        String originalValue = null;
        String value = null;
        int version = 0;
        List<Coordinate> dependsOn = new ArrayList<>();
        List<Coordinate> influencingOn = new ArrayList<>();
        CellStyleDto style = null;
        String lastUserUpdated = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "coordinate":
                    coordinate = coordinateAdapter.read(in);
                    break;
                case "originalValue":
                    originalValue = in.nextString();
                    break;
                case "value":
                    value = in.nextString();
                    break;
                case "version":
                    version = in.nextInt();
                    break;
                case "dependsOn":
                    dependsOn = readCoordinateList(in);
                    break;
                case "influencingOn":
                    influencingOn = readCoordinateList(in);
                    break;
                case "style":
                    style = readCellStyle(in);
                    break;
                case "lastUserUpdated":
                    lastUserUpdated = in.nextString();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        // creating effective value of string
        EffectiveValue effectiveValue = new EffectiveValueImpl(CellType.STRING, value);

        // creating cell dto with all collected data
        return new CellDtoImpl(coordinate, originalValue, effectiveValue, version, dependsOn, influencingOn, style, lastUserUpdated);
    }

    private void writeCoordinateList(JsonWriter out, List<Coordinate> coordinates) throws IOException {
        out.beginArray();
        for (Coordinate coordinate : coordinates) {
            coordinateAdapter.write(out, coordinate);
        }
        out.endArray();
    }

    private List<Coordinate> readCoordinateList(JsonReader in) throws IOException {
        List<Coordinate> coordinates = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) {
            coordinates.add(coordinateAdapter.read(in));
        }
        in.endArray();
        return coordinates;
    }

    private void writeCellStyle(JsonWriter out, CellStyleDto style) throws IOException {
        out.beginObject();
        out.name("backgroundColor").value(style.getBackgroundColor());
        out.name("textColor").value(style.getTextColor());
        out.name("alignment").value(style.getAlignment());
        out.name("isWrapped").value(style.isWrapped());
        out.endObject();
    }

    private CellStyleDto readCellStyle(JsonReader in) throws IOException {
        String backgroundColor = null;
        String textColor = null;
        String alignment = null;
        boolean isWrapped = false;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "backgroundColor":
                    backgroundColor = in.nextString();
                    break;
                case "textColor":
                    textColor = in.nextString();
                    break;
                case "alignment":
                    alignment = in.nextString();
                    break;
                case "isWrapped":
                    isWrapped = in.nextBoolean();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new CellStyleDto(backgroundColor, textColor, alignment, isWrapped);
    }
}
