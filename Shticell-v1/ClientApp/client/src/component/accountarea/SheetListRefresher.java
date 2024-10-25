package component.sheets;

import dto.SheetInfoDto;
import util.Constants;
import util.http.HttpClientUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

import static util.Constants.GSON_INSTANCE;

public class SheetListRefresher extends TimerTask {

    private final Consumer<String> httpRequestLoggerConsumer;
    private final Consumer<List<SheetInfoDto>> sheetListConsumer;
    private int requestNumber;

    public SheetListRefresher(Consumer<String> httpRequestLoggerConsumer, Consumer<List<SheetInfoDto>> sheetListConsumer) {
        this.httpRequestLoggerConsumer = httpRequestLoggerConsumer;
        this.sheetListConsumer = sheetListConsumer;
        requestNumber = 0;
    }

    @Override
    public void run() {

        final int finalRequestNumber = ++requestNumber;
        httpRequestLoggerConsumer.accept("About to invoke: " + Constants.SHEET_LIST + " | Sheets Request # " + finalRequestNumber);

        // שליחת בקשה אסינכרונית לשרת כדי לקבל את רשימת הגיליונות
        HttpClientUtil.runAsync(Constants.SHEET_LIST, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                httpRequestLoggerConsumer.accept("Sheets Request # " + finalRequestNumber + " | Ended with failure...");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonArrayOfSheets = response.body().string();
                httpRequestLoggerConsumer.accept("Sheets Request # " + finalRequestNumber + " | Response: " + jsonArrayOfSheets);

                // המרת התשובה המתקבלת מ-JSON לרשימת אובייקטי SheetInfoDto
                SheetInfoDto[] sheetArray = GSON_INSTANCE.fromJson(jsonArrayOfSheets, SheetInfoDto[].class);
                sheetListConsumer.accept(List.of(sheetArray));  // עדכון הרשימה עם SheetInfoDto
            }
        });
    }
}
