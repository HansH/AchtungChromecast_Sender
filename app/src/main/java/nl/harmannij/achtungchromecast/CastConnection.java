package nl.harmannij.achtungchromecast;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class CastConnection implements SessionManagerListener {
    private static final String TAG = "CastConnection";
    private static CastConnection mInstance = null;

    private CastContext mCastContext;
    private CustomChannel mCustomChannel;
    private CastSession mCastSession;
    private SessionManager mSessionManager;
    private CopyOnWriteArrayList<ConnectionListener> mConnectionListeners = new CopyOnWriteArrayList<ConnectionListener>();

    private CastConnection(Context context) {
        mCastContext = CastContext.getSharedInstance(context);
        mSessionManager = mCastContext.getSessionManager();
        mSessionManager.addSessionManagerListener(this);
        mCustomChannel = new CustomChannel();
    }

    public static CastConnection getInstance(Context context) {
        if(mInstance == null)
            mInstance = new CastConnection(context);

        return mInstance;
    }

    public void sendMessage(String message) {
        if (mCustomChannel != null) {
            try {
                StringBuffer buffer = new StringBuffer(message.length() + 2);
                buffer.append('"');
                buffer.append(message);
                buffer.append('"');
                mCastSession.sendMessage(mCustomChannel.getNamespace(), buffer.toString())
                        .setResultCallback(
                                new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status result) {
                                        if (!result.isSuccess()) {
                                            Log.e(TAG, "Sending message failed");
                                        }
                                        else {
                                            Log.d(TAG, "Sending message succesful");
                                        }
                                    }
                                });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        }
    }

    public void close() {
        mSessionManager.endCurrentSession(false);
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
         mConnectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener) {
        mConnectionListeners.remove(connectionListener);
    }

    @Override
    public void onSessionStarting(Session session) { }

    @Override
    public void onSessionStarted(Session session, String sessionId) {
        mCastSession = mSessionManager.getCurrentCastSession();

        try {
            mCastSession.setMessageReceivedCallbacks(
                    mCustomChannel.getNamespace(),
                    mCustomChannel);
        } catch (IOException e) {
            Log.e(TAG, "Exception while creating channel", e);
        }
        for(ConnectionListener listener : mConnectionListeners)
            listener.onStarted();
    }

    @Override
    public void onSessionStartFailed(Session session, int i) { }

    @Override
    public void onSessionEnding(Session session) { }

    @Override
    public void onSessionResumed(Session session, boolean wasSuspended) {
        mCastSession = mSessionManager.getCurrentCastSession();

        try {
            mCastSession.setMessageReceivedCallbacks(
                    mCustomChannel.getNamespace(),
                    mCustomChannel);
        } catch (IOException e) {
            Log.e(TAG, "Exception while creating channel", e);
        }
        for(ConnectionListener listener : mConnectionListeners)
            listener.onStarted();
    }

    @Override
    public void onSessionResumeFailed(Session session, int i) { }

    @Override
    public void onSessionSuspended(Session session, int i) {
        for(ConnectionListener listener : mConnectionListeners)
            listener.onEnded();
    }

    @Override
    public void onSessionEnded(Session session, int error) {
        for(ConnectionListener listener : mConnectionListeners)
            listener.onEnded();
    }

    @Override
    public void onSessionResuming(Session session, String s) { }

    private class CustomChannel implements Cast.MessageReceivedCallback {
        public String getNamespace() {
            return "urn:x-cast:nl.harmannij.achtungchromecast";
        }
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
                                      String message) {
            Log.d(TAG, "onMessageReceived: " + message);
            try {
                Object json = new JSONTokener(message).nextValue();
                if(json instanceof String) {
                    for(ConnectionListener listener : mConnectionListeners)
                        listener.onMessage((String)json);
                }
                else if(json instanceof JSONObject) {
                    for(ConnectionListener listener : mConnectionListeners)
                        listener.onMessage((JSONObject)json);
                }
                else {
                    Log.e(TAG, "Unexpected JSON type received in message");
                    for(ConnectionListener listener : mConnectionListeners)
                        listener.onMessage(message);
                }
            } catch (JSONException e) {
                Log.w(TAG, "Cannot parse received message as JSON");
            }
            for(ConnectionListener listener : mConnectionListeners)
                listener.onMessage(message);
        }
    }

    public interface ConnectionListener {
        void onStarted();
        void onEnded();
        void onMessage(String message);
        void onMessage(JSONObject json);
    }
}
