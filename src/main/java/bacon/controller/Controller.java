package bacon.controller;

import bacon.model.Actor;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.ArrayList;
public class Controller {
    @FXML
    private TextArea output;
    @FXML
    private TextField searchBox;
    @FXML
    private ListView <Actor> foundActors;
    @FXML
    private Button searchButton;
    private ObservableList <Actor> foundActorsObservableList;
    public Controller(){
    }
    @FXML private void initialize(){
        output.setEditable(false);
        foundActorsObservableList = FXCollections.observableArrayList(new ArrayList<>());
        foundActors.setItems(foundActorsObservableList);
        searchButton.setOnAction(e->{
            System.out.println(getActor(searchBox.getText()));
            addActors(getActor(searchBox.getText()));
        });

    }
    private String getActor(String name){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://java.kisim.eu.org/actors/search/"+name).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private void addActors(String actorsJSON){
        if(actorsJSON == null) return;
        try {
            Actor []actors = new ObjectMapper().readValue(actorsJSON, Actor[].class);
            foundActorsObservableList.addAll(actors);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
