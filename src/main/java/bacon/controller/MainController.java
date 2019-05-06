package bacon.controller;

import bacon.model.Actor;
import bacon.model.MainModel;
import bacon.model.Movie;
import bacon.model.MovieFull;
import bacon.view.MainView;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainController{
    public enum RequestType{
        ActorByName, ActorsByMovie, MoviesByActor;
    }
    private MainModel mainModel;
    private MainView mainView;
    private BFS bfs;
    private Thread bfsThread;
    public MainController(){
        mainModel = new MainModel(this);
        mainView = new MainView(this);
        bfs = new BFS(this);
    }

    public String getRequestURL(RequestType requestType, String value){
        switch(requestType){
            case MoviesByActor:{
                return "https://java.kisim.eu.org/actors/" + value + "/movies";
            }
            case ActorByName:{
                return  "https://java.kisim.eu.org/actors/search/"+value;
            }
            case ActorsByMovie:{
                return "https://java.kisim.eu.org/movies/" + value;
            }
            default: return null;
        }
    }
    public MovieFull parseMovieFull(String JSON){
        try {
            return new ObjectMapper().readValue(JSON, MovieFull.class);
        } catch (Exception e) {
            System.out.println(JSON);
            e.printStackTrace();
            return null;
        }
    }
    public Movie[] parseMovies(String JSON){
        try {
            return new ObjectMapper().readValue(JSON, Movie[].class);
        } catch (Exception e) {
            System.out.println(JSON);
            e.printStackTrace();
            return null;
        }
    }
    Actor[] parseActors(String JSON){
        try {
            return new ObjectMapper().readValue(JSON, Actor[].class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public void addActorsByName(String name){
        //Platform.runLater(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("https://java.kisim.eu.org/actors/search/"+name).build();
            try {
                Response response = client.newCall(request).execute();
                String actorsJSON = response.body().string();
                Actor[] actorsSimple = new ObjectMapper().readValue(actorsJSON, Actor[].class);
                mainModel.foundActorsSetAll(actorsSimple);
            }catch (Exception e){
                e.printStackTrace();
            }
        //});
    }
    public void start(){
        bfsThread = new Thread(bfs);
        bfsThread.setDaemon(false);
        bfs.init();
        bfsThread.start();

    }
    public void setSource(Actor actor){
        bfs.setSource(actor);
    }
    public void setTarget(Actor actor){
        bfs.setTarget(actor);
    }
    public MainModel getMainModel() {
        return mainModel;
    }

    public MainView getMainView() {
        return mainView;
    }
    public boolean finished(){
        return bfs.isFinished();
    }
    public Thread getThread(){
        return bfsThread;
    }
    public BFS getTask(){
        return bfs;
    }
}
