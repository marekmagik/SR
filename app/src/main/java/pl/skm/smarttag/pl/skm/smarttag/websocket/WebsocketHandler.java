package pl.skm.smarttag.pl.skm.smarttag.websocket;

import android.widget.TextView;

import de.tavendo.autobahn.WebSocketConnectionHandler;

/**
 * Created by Marcin on 2015-04-25.
 */
public class WebsocketHandler extends WebSocketConnectionHandler {

    private TextView textView;

    public WebsocketHandler(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void onOpen() {
        textView.setText(" Connection established ");
    }

    @Override
    public void onTextMessage(String payload) {
        textView.setText(" Received message: " + payload);
    }

    @Override
    public void onClose(int code, String reason) {
        textView.setText(" Connection closed. Reason:  " + reason);
    }

}
