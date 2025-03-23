package component.accountarea;

import dto.PermissionDTO;
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

public class PermissionListRefresher extends TimerTask {

    private final Consumer<String> httpRequestLoggerConsumer;
    private final Consumer<List<PermissionDTO>> permissionListConsumer;
    private final String sheetName;
    private int lastListHash = -1;

    public PermissionListRefresher(String sheetName, Consumer<String> httpRequestLoggerConsumer, Consumer<List<PermissionDTO>> permissionListConsumer) {
        this.sheetName = sheetName;
        this.httpRequestLoggerConsumer = httpRequestLoggerConsumer;
        this.permissionListConsumer = permissionListConsumer;
    }

    @Override
    public void run() {
        String url = Constants.SHEET_PERMISSION_URL + sheetName;
        httpRequestLoggerConsumer.accept("Fetching permissions for sheet: " + sheetName);

        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                httpRequestLoggerConsumer.accept("Error fetching permissions for sheet: " + sheetName + ", Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    String responseBody = response.body().string();
                    PermissionDTO[] permissionsArray = GSON_INSTANCE.fromJson(responseBody, PermissionDTO[].class);

                    // hash code new list
                    int newHash = List.of(permissionsArray).hashCode();
                    if (newHash != lastListHash) { // will update only if list has been changed
                        lastListHash = newHash;

                        Platform.runLater(() -> permissionListConsumer.accept(List.of(permissionsArray)));
                    }
                }
            }
        });
    }
}
