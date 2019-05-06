package bacon.controller;

import javafx.concurrent.Task;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelRequest extends Task {
    private static final AtomicInteger failCount = new AtomicInteger();
    private String url;
    private String response;
    public ParallelRequest(String url) {
        this.url = url;
    }

    @Override
    protected Object call(){

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        OkHttpClient client = builder.build();
        Request request = new Request.Builder().url(url).build();
        try {
            response = client.newCall(request).execute().body().string();
        }catch(Exception e){
            System.out.println(e.toString() + ": " + url + ": " + failCount);
            failCount.getAndIncrement();
        }
        return null;
    }
    
    public String getResponse() {
        return response;
    }
}
