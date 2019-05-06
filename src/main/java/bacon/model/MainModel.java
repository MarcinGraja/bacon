package bacon.model;

import bacon.controller.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class MainModel {
    private ObservableList<Actor> foundActorsObservableList;
    private MainController controller;
    public void foundActorsSetAll(Actor[] actorsSimple) {
        foundActorsObservableList.setAll(actorsSimple);
    }
    public MainModel(MainController controller) {
        this.controller = controller;
        foundActorsObservableList = FXCollections.observableList(new ArrayList<>());
    }

    public ObservableList<Actor> getFoundActors() {
        return foundActorsObservableList;
    }
}
