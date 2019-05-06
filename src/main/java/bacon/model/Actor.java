package bacon.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;
@JsonDeserialize(using = ActorDeserializer.class)
public class Actor {
    private String id;
    private String name;

    public Actor(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Actor(Actor actor) {
        this.name = actor.getName();
        this.id = actor.getID();
    }
    public Actor(){}
    public String getID() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actor)) return false;
        Actor that = (Actor) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString(){
        return name;
    }
}
