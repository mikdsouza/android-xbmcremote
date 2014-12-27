package org.xbmc.android.remote.presentation.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.xbmc.android.remote.presentation.controller.RemoteController;

public class RemoteService extends Service implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks{

    private static final String WEAR_MESSAGE_PATH = "/WearActivity";
    private GoogleApiClient mApiClient;

    public RemoteService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initGoogleApiClient();
        Log.d("WearAPI", "Wear listener service started");
    }

    @Override
    public void onDestroy(){
        if( mApiClient != null ) {
            Wearable.MessageApi.removeListener(mApiClient, this);
            mApiClient.unregisterConnectionCallbacks(this);
        }
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(WEAR_MESSAGE_PATH)) {
            String command = new String(messageEvent.getData());
            Log.d("WearAPI", "Message received: \"" + command + "\"");

            RemoteController mRemoteController;
            mRemoteController = new RemoteController(getApplicationContext());
            mRemoteController.sendButton(command);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mApiClient, this);
        Log.d("WearAPI", "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }
}
