package component.accountarea;

import dto.SheetInfoDto;
import javafx.application.Platform;
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
    private int listVersionNum = 1;

    public SheetListRefresher(Consumer<String> httpRequestLoggerConsumer, Consumer<List<SheetInfoDto>> sheetListConsumer) {
        this.httpRequestLoggerConsumer = httpRequestLoggerConsumer;
        this.sheetListConsumer = sheetListConsumer;
        requestNumber = 0;
    }

    @Override
    public void run() {
        final int finalRequestNumber = ++requestNumber;
        httpRequestLoggerConsumer.accept("About to invoke: " + Constants.SHEET_LIST_VERSION + " | Version Request # " + finalRequestNumber);

        // async request for new list of sheets
        HttpClientUtil.runAsync(Constants.SHEET_LIST_VERSION, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                httpRequestLoggerConsumer.accept("Version Request # " + finalRequestNumber + " | Ended with failure...");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    String versionResponse = response.body().string();
                    int serverVersion = Integer.parseInt(versionResponse.trim());
                    httpRequestLoggerConsumer.accept("Version Request # " + finalRequestNumber + " | Server Version: " + serverVersion);

                    // only if there is new version
                    if (serverVersion != listVersionNum) {
                        //update according to version of server
                        listVersionNum = serverVersion;
                        fetchSheetList(finalRequestNumber);
                    }
                }
            }
        });
    }

    // if there is a new version only then will ask for new list
    private void fetchSheetList(int requestNumber) {
        httpRequestLoggerConsumer.accept("About to invoke: " + Constants.SHEET_LIST + " | Sheets Request # " + requestNumber);

        HttpClientUtil.runAsync(Constants.SHEET_LIST, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                httpRequestLoggerConsumer.accept("Sheets Request # " + requestNumber + " | Ended with failure...");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    String jsonArrayOfSheets = response.body().string();
                    httpRequestLoggerConsumer.accept("Sheets Request # " + requestNumber + " | Response: " + jsonArrayOfSheets);

                    // from json to sheetinfo
                    SheetInfoDto[] sheetArray = GSON_INSTANCE.fromJson(jsonArrayOfSheets, SheetInfoDto[].class);

                    Platform.runLater(() -> {
                        sheetListConsumer.accept(List.of(sheetArray));  // update with sheet info
                    });
                }
            }
        });
    }
}
