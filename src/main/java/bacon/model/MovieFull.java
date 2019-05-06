package bacon.model;

public class MovieFull extends Movie {
    private Actor[] actors;
    MovieFull(){
        super();
    }
    MovieFull(String title, String id, Actor[] actors){
        super(title, id);
        this.actors = actors;
    }
    public Actor[] getActors(){
        return actors;
    }
}
