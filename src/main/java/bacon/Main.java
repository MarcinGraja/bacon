package bacon;
//--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED
//        --add-exports=javafx.base/com.sun.javafx.reflect=ALL-UNNAMED
//        --add-exports=javafx.base/com.sun.javafx.beans=ALL-UNNAMED
//        --add-exports=javafx.graphics/com.sun.glass.utils=ALL-UNNAMED
//        --add-exports=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import bacon.model.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.SimpleGraph;

import java.util.List;
import java.util.Set;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mainView.fxml"));
        fxmlLoader.setController(new bacon.controller.Controller());
        primaryStage.setTitle("Degrees of separation");
        primaryStage.setScene(new Scene(fxmlLoader.load(), 600, 400));

        primaryStage.show();
    }
    public static void main(String []args){
        launch(args);
        runSimpleGraphExample();
    }
    static private void runSimpleGraphExample(){
        Graph <Actor, Movie> g = new SimpleGraph<>(Movie.class);
        Actor a = new Actor("A1","nm0001");
        Actor b = new Actor("A2","nm0002");
        Actor c = new Actor("A3","nm0003");
        Actor d = new Actor("A4","nm0004");
        //model.Actor newA = new model.Actor("A1", "t0001");
        // poeksperymentuj!
        g.addVertex(a);
        g.addVertex(b);
        g.addVertex(c);
        g.addVertex(d);
        //g.addVertex(newA);
        g.addEdge(a,b, new Movie("M1", "t1"));
        g.addEdge(b,c, new Movie("M2", "t1"));
        g.addEdge(b,d, new Movie("M3", "t1"));
        g.addEdge(d,a, new Movie("M4", "t1"));
        //g.addEdge(newA,c, new model.Movie("M5", ""));
        Set<Actor> vertices = g.vertexSet();
        BellmanFordShortestPath<Actor, Movie> bfsp = new BellmanFordShortestPath<>(g);
        GraphPath<Actor, Movie> shortestPath = bfsp.getPath(a,c);
        List<Movie> edges = shortestPath.getEdgeList();
        List<Actor> actors = shortestPath.getVertexList();
        for(int i = 0; i < actors.size(); ++i){
            if(i == actors.size()-1)
                System.out.print(actors.get(i));
            else
                System.out.print(actors.get(i) + " -> " + edges.get(i).toString()  + " -> ");
        }
    }

}
