package bacon.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class ActorDeserializer extends StdDeserializer<Actor> {
    public ActorDeserializer(){
        this(null);
    }
    public ActorDeserializer(Class<?> vc) {
        super(vc);
    }
    @Override
    public Actor deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String name;
        String id;
        try {
            name = node.get("name").textValue();
            id = node.get("id").textValue();
            return  new Actor(name, id);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

