package component.sheetViewfinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class SheetVersionRefresher extends TimerTask {

    private final Supplier<Integer> localSheetVersionSupplier;
    private final Consumer<Boolean> versionCheckHandler;

    public SheetVersionRefresher(Supplier<Integer> localSheetVersionSupplier, Consumer<Boolean> versionCheckHandler) {
        this.localSheetVersionSupplier = localSheetVersionSupplier;
        this.versionCheckHandler = versionCheckHandler;
    }

    @Override
    public void run() {
        int localSheetVersion = localSheetVersionSupplier.get();
        // checks if version matches the server version
        checkVersionWithServer(localSheetVersion);
    }

    private void checkVersionWithServer(int localSheetVersion) {
        // creating req to server
        String url = Constants.VERSION_CHECK_URL + "?sheetVersion=" + localSheetVersion;

        Request request = new Request.Builder()
                .url(url)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Error checking version: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean isUpToDate = response.isSuccessful(); // checks if version matched
                versionCheckHandler.accept(isUpToDate); // handles the result (updates if needed - according to response mode)
                response.close();
            }
        });
    }
}
