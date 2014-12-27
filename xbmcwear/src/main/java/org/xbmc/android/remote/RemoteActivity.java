package org.xbmc.android.remote;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class RemoteActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

    private TextView mTextView;
    private static final String WEAR_MESSAGE_PATH = "/WearActivity";

    private GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        initGoogleApiClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    public void playButtonOnClick(View v) {
        //Toast.makeText(getApplicationContext(), "You pressed play", Toast.LENGTH_LONG).show();
        sendMessage(WEAR_MESSAGE_PATH, ButtonCodes.REMOTE_PLAY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("WearAPI", "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mApiClient.connect();
    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                    Log.d("WearAPI", "Sent message \"" + text + "\" TO " + node.getId());
                }
            }
        }).start();
    }
}
