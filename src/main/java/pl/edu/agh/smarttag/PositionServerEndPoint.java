package pl.edu.agh.smarttag;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jscience.mathematics.vector.Float64Vector;

import pl.edu.agh.smarttag.message.MessageType;
import pl.edu.agh.smarttag.message.WebsocketTextMessage;
import pl.edu.agh.smarttag.message.WebsocketTextMessageDeserializer;
import pl.edu.agh.smarttag.position.DistanceCalculator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

@ServerEndpoint(value="/position", configurator=PositionServerEndPointConfigurator.class)
public class PositionServerEndPoint {
   
	public final static Logger LOGGER = LogManager.getLogger(PositionServerEndPoint.class);
	
    private Map<Session,Float64Vector> userPositionMap = Collections.synchronizedMap(new HashMap<Session,Float64Vector>());
    private Gson gson = new GsonBuilder().registerTypeAdapter(WebsocketTextMessage.class, new WebsocketTextMessageDeserializer()).create();
    private static final double RADIUS = 1.0; 

    /**
     * Callback hook for Connection open events. This method will be invoked when a 
     * client requests for a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        userPositionMap.put(userSession,null);
        LOGGER.debug(" Opening session with id: "+userSession.getId());
    }
    
    /**
     * Callback hook for Connection close events. This method will be invoked when a
     * client closes a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnClose
    public void onClose(Session userSession) {
        userPositionMap.remove(userSession);
        LOGGER.debug(" CLosing session with id: "+userSession.getId());
    }
    
    /**
     * Callback hook for Message Events. This method will be invoked when a client
     * send a message.
     * @param message The text message
     * @param userSession The session of the client
     */
	@OnMessage
    public void onMessage(String message, Session userSession) {
    	
    	String info = null;
    	String enemyInfo = null;
		Session nearestEnemy = null;
    	WebsocketTextMessage websocketMessage = parseWebsocketMessageFromJson(message);
    	if(websocketMessage == null)
    		info = "Invalid request";
    	else if(websocketMessage != null){
    		LOGGER.debug(" Received message: "+ websocketMessage);
			
    		if(websocketMessage.getMessageType().equals(MessageType.POSITION)){
    			userPositionMap.put(userSession, websocketMessage.getVector());
    			info = "Position updated";
    		} else if (websocketMessage.getMessageType().equals(MessageType.SHOT)){
    			double nearestEnemyDistance = 0.0;
    			Float64Vector userPosition = userPositionMap.get(userSession);
    			Float64Vector direction = websocketMessage.getVector();
    			
    			if(userPosition == null){
    				info = "Your position is unknown";
    			} else {
    				SearchResult searchResult = findNearestEnemy(direction, userPosition, userSession);
    				
    				nearestEnemy = searchResult.getSession();
    				nearestEnemyDistance = searchResult.getDistance();
    				
    				if(nearestEnemy != null && nearestEnemyDistance < RADIUS){
    					info = "Enemy shot";
    					enemyInfo = "You've been shot"; 
    					userPositionMap.remove(nearestEnemy);
    				} else {
    					info = "You've missed";
    				}
    			}
    		}
    	}
    	
    	LOGGER.info("Sending message: "+ info);
    	try {
			userSession.getBasicRemote().sendText(info);
			if(enemyInfo != null)
				nearestEnemy.getBasicRemote().sendText(enemyInfo);
			
		} catch (IOException e) {
			LOGGER.error("Exception while writing to websocket: "+e.getMessage());
			e.printStackTrace();
		}
    }
    
    @SuppressWarnings("resource")
	private SearchResult findNearestEnemy(Float64Vector direction, Float64Vector userPosition, Session userSession){
    	Session nearestEnemy = null; 
    	double nearestEnemyDistance = 0.0; 
    	
    	DistanceCalculator distanceCalculator = new DistanceCalculator();
    	for(Entry<Session,Float64Vector> entry : userPositionMap.entrySet()){
    		if(!entry.getKey().equals(userSession)){
				if(nearestEnemy == null){
					nearestEnemy = entry.getKey();
					nearestEnemyDistance = distanceCalculator.computeDistance(userPosition, entry.getValue(), direction);
				} else {
					double actualEnemyDistance = distanceCalculator.computeDistance(userPosition, entry.getValue(), direction);
					if(actualEnemyDistance < nearestEnemyDistance){
						nearestEnemyDistance = actualEnemyDistance;
						nearestEnemy = entry.getKey();
					}
				}
    		}
		}
    	LOGGER.info("Nearest enemy distance: "+nearestEnemyDistance);
    	return new SearchResult(nearestEnemy, nearestEnemyDistance);
    }
    
    private WebsocketTextMessage parseWebsocketMessageFromJson(String message){
    	WebsocketTextMessage result = null;
    	
    	try{
    		result = gson.fromJson(message, WebsocketTextMessage.class);
    	} catch (IllegalArgumentException ex){
    		LOGGER.error(ex.getMessage());
    	} catch (JsonParseException ex){
    		LOGGER.error(ex.getMessage());
    	} catch (NullPointerException ex){
    		LOGGER.error(ex.getMessage());
    	}
    	
    	return result;
    }
    
    private class SearchResult{
    	private Session session;
    	private double distance;
    	
		public SearchResult(Session session, double distance) {
			super();
			this.session = session;
			this.distance = distance;
		}
		public Session getSession() {
			return session;
		}
		public double getDistance() {
			return distance;
		}
    	
    }
}