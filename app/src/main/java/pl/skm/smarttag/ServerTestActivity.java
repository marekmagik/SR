package pl.skm.smarttag;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import pl.skm.smarttag.R;
import pl.skm.smarttag.pl.skm.smarttag.websocket.WebsocketHandler;

public class ServerTestActivity extends ActionBarActivity {

    private final WebSocketConnection mConnection = new WebSocketConnection();
    private TextView outputTextView;
    private EditText inputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_test);
        outputTextView = (TextView) findViewById(R.id.message_out);
        inputEditText = (EditText) findViewById(R.id.message_in);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void connect(View view){
        EditText editText = (EditText) findViewById(R.id.server_address_in);
        String serverAddress = editText.getText().toString();
        try {
            mConnection.connect(serverAddress,new WebsocketHandler(outputTextView));
        } catch (WebSocketException e) {
            outputTextView.setText(" Error connection to server. "+e.getMessage());
        }
    }

    public void sendMessage(View view){
        if(mConnection.isConnected()){
            mConnection.sendTextMessage(inputEditText.getText().toString());
        } else {
            outputTextView.setText(" You're not connected to the server ");
        }
    }

    public void disconnect(View view){
        if(mConnection.isConnected())
            mConnection.disconnect();
        else
            outputTextView.setText(" Already disconnected ");
    }
}
