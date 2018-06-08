package nl.harmannij.achtungchromecast;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.cast.framework.CastButtonFactory;

import org.json.JSONObject;

public class ControllerActivity extends AppCompatActivity implements CastConnection.ConnectionListener {
    private enum Stage { WAITING_ROOM, GAME_STARTED }

    public static final String EXTRA_THEME_ID = "nl.harmannij.achtungchromecast.THEMEID";

    private static final String TAG = "ControllerActivity";
    private CastConnection mCastConnection;
    private Button mReadyButton;
    private LinearLayout mControllerLayout;
    private Button mLeftButton;
    private Button mRightButton;
    private Stage currentStage = Stage.WAITING_ROOM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int theme = getIntent().getIntExtra(EXTRA_THEME_ID, R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_controller);

        mCastConnection = CastConnection.getInstance(this);

        mControllerLayout = (LinearLayout)findViewById(R.id.controllerLayout);
        mReadyButton = (Button)findViewById(R.id.readyButton);

        setWidgetVisibility();

        mReadyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCastConnection.sendMessage("Ready");
            }
        });

        mCastConnection.addConnectionListener(this);

        mLeftButton = (Button)findViewById(R.id.leftButton);
        mLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_POINTER_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    mCastConnection.sendMessage("Left");
                }
                else {
                    mCastConnection.sendMessage("Released");
                }
                return true;
            }
        });

        mRightButton = (Button)findViewById(R.id.rightButton);
        mRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_POINTER_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    mCastConnection.sendMessage("Right");
                }
                else {
                    mCastConnection.sendMessage("Released");
                }
                return true;
            }
        });
    }

    private void setWidgetVisibility() {
        switch (currentStage) {
            case WAITING_ROOM:
                mControllerLayout.setVisibility(View.GONE);
                mReadyButton.setVisibility(View.VISIBLE);
                break;
            case GAME_STARTED:
                mControllerLayout.setVisibility(View.VISIBLE);
                mReadyButton.setVisibility(View.GONE);
                break;
        }
    }

    private void changeColor(int color) {
        mLeftButton.setTextColor(color);
        mRightButton.setTextColor(color);
        mReadyButton.setTextColor(color);

    }

    @Override
    public void onStarted() { }

    @Override
    public void onEnded() {
        finish();
    }

    @Override
    public void onMessage(String message) {
        if(message.equals("WaitingRoom")) {
            currentStage = Stage.WAITING_ROOM;
        }
        else if(message.equals("GameStarted")) {
            currentStage = Stage.GAME_STARTED;
        }
        setWidgetVisibility();
    }

    @Override
    public void onMessage(JSONObject json) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onDestroy() {
        mCastConnection.close();
        super.onDestroy();
    }
}
