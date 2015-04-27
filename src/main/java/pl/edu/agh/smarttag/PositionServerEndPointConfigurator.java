package pl.edu.agh.smarttag;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class PositionServerEndPointConfigurator extends Configurator {

	private PositionServerEndPoint positionServer = new PositionServerEndPoint();

	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass)
			throws InstantiationException {
		return (T)positionServer;
	}
}