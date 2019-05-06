package bacon.controller;

import javafx.concurrent.Task;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ParallelRequest extends Task {
    private String url;
    private String response;
    public ParallelRequest(String url) {
        this.url = url;
    }

    @Override
    protected Object call() throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        response = client.newCall(request).execute().body().string();
        return null;
    }
    
    public String getResponse() {
        return response;
    }
}
