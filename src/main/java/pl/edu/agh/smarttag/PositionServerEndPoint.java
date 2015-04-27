package pl.edu.agh.smarttag;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ServerEndpoint(value="/position", configurator=PositionServerEndPointConfigurator.class)
public class PositionServerEndPoint {
   
	public final static Logger LOGGER = LogManager.getLogger(PositionServerEndPoint.class);
	
    private Map<Session,Object> userSessionsMap = Collections.synchronizedMap(new HashMap<Session,Object>());

    /**
     * Callback hook for Connection open events. This method will be invoked when a 
     * client requests for a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        userSessionsMap.put(userSession,null);
        LOGGER.debug(" Opening session with id: "+userSession.getId());
    }
    
    /**
     * Callback hook for Connection close events. This method will be invoked when a
     * client closes a WebSocket connection.
     * @param userSession the userSession which is opened.
     */
    @OnClose
    public void onClose(Session userSession) {
        userSessionsMap.remove(userSession);
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
    	userSessionsMap.put(userSession, message);
    	LOGGER.debug(" Received message: "+message);
    	try {
			userSession.getBasicRemote().sendText(" Date "+new Date()+" message: "+message);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}