package nl.harmannij.achtungchromecast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastButtonFactory;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements CastConnection.ConnectionListener {
    private static final String TAG = "MainActivity";
    private MediaRouteButton mMediaRouteButton;
    private CastConnection mCastConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCastConnection = CastConnection.getInstance(this);

        mCastConnection.addConnectionListener(this);

        mMediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mMediaRouteButton);
    }

    private void startControllerThemed(int resid) {
        Intent intent = new Intent(this, ControllerActivity.class);
        intent.putExtra(ControllerActivity.EXTRA_THEME_ID, resid);
        startActivity(intent);
    }

    @Override
    public void onStarted() { }

    @Override
    public void onEnded() { }

    @Override
    public void onMessage(String message) { }

    @Override
    public void onMessage(JSONObject json) {
        if(json.has("name")) {
            try {
                switch (json.getString("name")) {
                    case "Red":
                        startControllerThemed(R.style.RedTheme);
                        return;
                    case "Green":
                        startControllerThemed(R.style.GreenTheme);
                        return;
                    case "Yellow":
                        startControllerThemed(R.style.YellowTheme);
                        return;
                    case "Blue":
                        startControllerThemed(R.style.BlueTheme);
                        return;
                    case "Pink":
                        startControllerThemed(R.style.PinkTheme);
                        return;
                    case "Gray":
                        startControllerThemed(R.style.GrayTheme);
                        return;
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
                /*if(json.has("color")) {
                    try {
                        int color = Color.parseColor(json.getString("color"));
                        changeColor(color);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }

}
