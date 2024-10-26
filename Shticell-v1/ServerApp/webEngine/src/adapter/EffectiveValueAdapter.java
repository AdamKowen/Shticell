package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;

import java.io.IOException;

public class EffectiveValueAdapter extends TypeAdapter<EffectiveValue> {

    @Override
    public void write(JsonWriter out, EffectiveValue effectiveValue) throws IOException {
        out.beginObject();
        out.name("cellType").value(effectiveValue.getCellType().name());
        out.name("value").value(effectiveValue.getValue().toString());
        out.endObject();
    }

    @Override
    public EffectiveValue read(JsonReader in) throws IOException {
        CellType cellType = null;
        Object value = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "cellType":
                    cellType = CellType.valueOf(in.nextString());
                    break;
                case "value":
                    value = in.nextString();
                    break;
            }
        }
        in.endObject();

        return new EffectiveValueImpl(cellType, value);
    }
}
