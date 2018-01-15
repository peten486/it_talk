package project001.itcore.it_talk.model;

import android.app.Application;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;


/**
 * Created by peten on 2017. 10. 1..
 */


public class SocketConnection extends Application {
    private static SocketConnection mInstance;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.ChatS);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static SocketConnection getInstance(){
        return mInstance;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public Socket disconnect(){
        return mSocket.disconnect();
    }

    @Override
    public void onCreate() {

        Log.d(this.getClass().getSimpleName(),"onCreate");
        super.onCreate();
        mInstance = this;
    }



}