package pl.edu.agh.smarttag.message;

import java.lang.reflect.Type;

import org.jscience.mathematics.vector.Float64Vector;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class WebsocketTextMessageDeserializer implements JsonDeserializer<WebsocketTextMessage>{

	@Override
	public WebsocketTextMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonMessageObject = jsonElement.getAsJsonObject();
		String messageType = jsonMessageObject.get("type").getAsString();
		
		JsonObject vector = jsonMessageObject.get("vector").getAsJsonObject();
		double x = vector.get("x").getAsDouble();
		double y = vector.get("y").getAsDouble();
		double z = vector.get("z").getAsDouble();
		
		Float64Vector messageVector = Float64Vector.valueOf(x,y,z);
		
		return new WebsocketTextMessage(MessageType.valueOf(messageType), messageVector);
	}

}
