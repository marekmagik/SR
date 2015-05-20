package smarttag;

import static org.junit.Assert.assertEquals;

import org.jscience.mathematics.vector.Float64Vector;
import org.junit.Before;
import org.junit.Test;

import pl.edu.agh.smarttag.message.MessageType;
import pl.edu.agh.smarttag.message.WebsocketTextMessage;
import pl.edu.agh.smarttag.message.WebsocketTextMessageDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class WebsocketDeserializerTest {

	private Gson gson;
	
	@Before
	public void setup(){
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(WebsocketTextMessage.class, new WebsocketTextMessageDeserializer());
		gson = builder.create();
	}
	
	@Test
	public void testShotMessage() {
		String json ="{type=SHOT,vector={x=1,y=2,z=3}}";
		WebsocketTextMessage textMessage = gson.fromJson(json, WebsocketTextMessage.class);
		assertEquals(MessageType.SHOT,textMessage.getMessageType());
		Float64Vector vector = textMessage.getVector();
		assertEquals(1.0, vector.get(0).doubleValue(),0.0);
		assertEquals(2.0, vector.get(1).doubleValue(),0.0);
		assertEquals(3.0, vector.get(2).doubleValue(),0.0);
	}
	
	@Test
	public void testPositionMessage() {
		String json ="{type=POSITION,vector={x=1,y=2,z=3}}";
		WebsocketTextMessage textMessage = gson.fromJson(json, WebsocketTextMessage.class);
		assertEquals(MessageType.POSITION,textMessage.getMessageType());
		Float64Vector vector = textMessage.getVector();
		assertEquals(1.0, vector.get(0).doubleValue(),0.0);
		assertEquals(2.0, vector.get(1).doubleValue(),0.0);
		assertEquals(3.0, vector.get(2).doubleValue(),0.0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalMessageType() {
		String json ="{type=APPLE_PIE,vector={x=1,y=2,z=3}}";
		gson.fromJson(json, WebsocketTextMessage.class);
	}
	
	@Test(expected=JsonParseException.class)
	public void testInavlidJson() {
		String json ="{type=APPLE_]PIE{{vect[or={x=1,y=2,z=3}}";
		gson.fromJson(json, WebsocketTextMessage.class);
	}

}
