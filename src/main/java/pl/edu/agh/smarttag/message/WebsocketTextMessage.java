package pl.edu.agh.smarttag.message;

import org.jscience.mathematics.vector.Float64Vector;

public class WebsocketTextMessage {
	
	private MessageType messageType;
	private Float64Vector vector;
		
	public WebsocketTextMessage(MessageType messageType, Float64Vector vector) {
		super();
		this.messageType = messageType;
		this.vector = vector;
	}
	
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	public Float64Vector getVector() {
		return vector;
	}
	public void setVector(Float64Vector vector) {
		this.vector = vector;
	}

	@Override
	public String toString() {
		return "WebsocketTextMessage [messageType=" + messageType + ", vector=" + vector + "]";
	}
	
	
}
