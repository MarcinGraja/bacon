package bacon.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class MovieFullDeserializer extends StdDeserializer<MovieFull> {
    public MovieFullDeserializer(){
        this(null);
    }
    public MovieFullDeserializer(Class<?> vc) {
        super(vc);
    }
    @Override
    public MovieFull deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String title;
        String id;
        Actor[] actors;
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        try {
            title = node.get("title").textValue();
            id = node.get("id").textValue();
            actors = new ObjectMapper().readValue(node.get("actors").toString(), Actor[].class);
            return new MovieFull(title, id, actors);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
