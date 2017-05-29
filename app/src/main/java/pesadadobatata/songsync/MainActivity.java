package pesadadobatata.songsync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.net.InternetDomainName;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;
import samplesearch.SearchActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SpotifyPlayer.NotificationCallback,
        ConnectionStateCallback {


    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private Button loginbutton;
    private Button signinbutton;
    private ImageView iv;
    private TextView tv;
    private ProgressBar pb;
    private boolean userState;
    private SpotifyApi api;
    public String spotifyToken;
    private Context context;
    private Boolean hasToken = false;
    private ImageView thumbnail;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button loginbutton = (Button) findViewById(R.id.loginbutton);
        final Button signinbutton = (Button) findViewById(R.id.signinbutton);
        final ImageView iv = (ImageView) findViewById(R.id.imageView2);
        final TextView tv = (TextView) findViewById(R.id.textView2);
        final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar2);
        final ImageButton fb = (ImageButton) findViewById(R.id.friendsButton);
        final ImageButton sb = (ImageButton) findViewById(R.id.searchButton2);
//        final EditText ssf = (EditText) findViewById(R.id.songsearchField);
        thumbnail = (ImageView) findViewById(R.id.thumbnailView);

        Context context = getApplicationContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View cv = inflater.inflate(R.layout.content_main, null);

        final ConstraintLayout cl = (ConstraintLayout) cv.findViewById(R.id.cl);

        Log.d("AEAWWA", String.valueOf(cl.isActivated()));
        Log.d("eaemenkk", SpotifyAPI.getString());
        mAuth =  FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    userState = true;
                    Log.d("kk", "onAuthStateChanged:signed_in:" + user.getUid());
                    pb.setVisibility(View.GONE);
                    loginbutton.setVisibility(View.GONE);
                    signinbutton.setVisibility(View.GONE);
                    iv.setVisibility(View.GONE);
                    tv.setVisibility(View.GONE);
                    sb.setVisibility(View.VISIBLE);
//                    ssf.setVisibility(View.VISIBLE);
                    fb.setVisibility(View.VISIBLE);
                    if (!MainActivity.this.hasToken){
                        SpotifyAPI.authSpotify(MainActivity.this);
                    }
//

                } else {
                    // User is signed out
                    userState = false;
                    Log.d("kk", "onAuthStateChanged:signed_out");
                    pb.setVisibility(View.GONE);
                    loginbutton.setVisibility(View.VISIBLE);
                    signinbutton.setVisibility(View.VISIBLE);
                    iv.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.VISIBLE);
//                    ssf.setVisibility(View.GONE);
                    sb.setVisibility(View.GONE);
                    fb.setVisibility(View.GONE);


//                    for (int i = 0; i<= cl.getChildCount() ; i ++){
//                        cl.getChildAt(i).setVisibility(View.VISIBLE);
//                    }
                    //                Log.d("jikasa", String.valueOf(cl.getChildCount()));
                    //                Log.d("kk", String.valueOf(cl.getChildAt(0).getId()));
                    //                cl.getChildAt(0).setVisibility(View.GONE);
                }
                // ...
            }
        };

        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                Map<String, String> value = dataSnapshot.getValue(Map.class);
                Map<String, String> value = (Map<String, String>) dataSnapshot.getValue();
                Log.d("Database now", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Erou", "Failed to read value.", error.toException());
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        Inflater searchButton = Inflate r;
//        View cv = inflater.inflate(R.layout.content_main, null);
    }

    public void goToSearchActivity(View view){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        startActivity(new Intent(this, SearchActivity.class));
    }


    public void onLoginButtonPressed(View view){
        Intent myIntent = new Intent(this, LoginActivity.class);
        startActivity(myIntent);
    }

    public void onSignupButtonPressed(View view){
        Intent myIntent = new Intent(this, SignupActivity.class);
        startActivity(myIntent);
    }

    public void signOut(MenuItem menuItem){
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
//        inflate(R.layout.app_bar_main,menu);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("ACTIVITY","Returned to MainActivity via onStart");
        if (mAuthListener != null){
            mAuth.addAuthStateListener(mAuthListener);
        }
        if (!Objects.equals(SpotifyAPI.getUri(), "")){
            playSong();
            drawSongThumbnail();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void playSong(){
        mPlayer.playUri(null, SpotifyAPI.getUri(),0,0);
    }

    public void drawSongThumbnail(){
        thumbnail.setVisibility(View.VISIBLE);
        Picasso.with(context).load(SpotifyAPI.getThumbnailUrl()).into(thumbnail);
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
//        mPlayer.playUri(null, "spotify:track:4tNaC9xdo65pgl1QAaygA4", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");

    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("ACTIVITY","Returned to MainActivity");
         //Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE && this.hasToken == false) {
            super.onActivityResult(requestCode, resultCode, intent);
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                spotifyToken = response.getAccessToken();
                Log.d("Token", spotifyToken);
                SpotifyAPI.setSpotifyToken(spotifyToken);
                this.hasToken = true;
                SpotifyAPI.startWebAPI();
                Config playerConfig = new Config(this, SpotifyAPI.getSpotifyToken(), SpotifyAPI.getClientID());
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }



//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        // Get the layout inflater
//        LayoutInflater inflater = this.getLayoutInflater();
//
//        // Inflate and set the layout for the dialog
//        // Pass null as the parent view because its going in the dialog layout
//        builder.setView(inflater.inflate(R.layout.dialog_signin, null))
//                // Add action buttons
//                .setPositiveButton(R.string.username, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        // sign in the user ...
//                    }
//                })
//                .setNegativeButton(R.string.password, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        LoginDialogFragment.this.getDialog().cancel();
//                    }
//                });
//        return builder.create();
//    }


}







