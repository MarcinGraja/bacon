package bacon.model;

public class Movie extends org.jgrapht.graph.DefaultEdge{
    private String name;
    private String id;

    public Movie(String name, String id) {
        this.name = name;
        this.id = id;
    }
}
