package pesadadobatata.songsync;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.IntegerRes;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;
import samplesearch.SearchActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SpotifyPlayer.NotificationCallback,
        ConnectionStateCallback, RequestHandlerListener, ConnectionHandlerListener {


    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private Button loginbutton;
    private Button signinbutton;
    private ImageView iv;
    private boolean userState;
    private SpotifyApi api;
    public String spotifyToken;
    private Context context;
    private Boolean hasToken = false;
    private ImageView thumbnail;
    private DatabaseReference mDatabase;
    private Boolean rhloaded = false;
    private RequestHandler rh;
    private AlarmManager alarmMgr;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;
    private NavigationView navigationView;
    private ConstraintLayout cl;
    private DrawerLayout drawer;
    private TextView sideBarUserName;
    private TextView sideBarEmail;
    private ImageView pic;
    private TextView tv;
    private TextView partnerUserView;

    private Button playButton;
    private Button pauseButton;
    private Button resumeButton;
    private ProgressBar musicProgress;
    private TextView musicTimeRight;
    private TextView musicTimeLeft;
    private TextView songNameView;
    private TextView artistNameView;
    private Thread thread;
    private int flag;
    private boolean mStopHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        final Button loginbutton = (Button) findViewById(R.id.loginbutton);
        final Button signinbutton = (Button) findViewById(R.id.signinbutton);
        final ImageView iv = (ImageView) findViewById(R.id.imageView2);
        tv = (TextView) findViewById(R.id.textView2);
//        final EditText ssf = (EditText) findViewById(R.id.songsearchField);
        thumbnail = (ImageView) findViewById(R.id.thumbnailView);
        thumbnail.setVisibility(View.VISIBLE);
        playButton = (Button) findViewById(R.id.playButton);
        pauseButton = (Button) findViewById(R.id.pauseButton);
        resumeButton = (Button) findViewById(R.id.resumeButton);
        musicProgress = (ProgressBar) findViewById(R.id.musicProgress);
        musicTimeRight = (TextView) findViewById(R.id.musicTimeRight);
        musicTimeLeft = (TextView) findViewById(R.id.musicTimeLeft);
        songNameView = (TextView) findViewById(R.id.songNameView);
        artistNameView = (TextView) findViewById(R.id.artistNameView);

        final Context context = getApplicationContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View cv = inflater.inflate(R.layout.content_main, null);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        partnerUserView = (TextView) findViewById(R.id.partnerUserView);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_music:
                                goToSearchActivity();
                                break;
                            case R.id.action_friends:
                                goToFriendsActivity();
                                break;
                            case R.id.action_searchfriends:
                                startActivity(new Intent(getApplicationContext(), SearchFriendsActivity.class));
                                break;
                        }
                        return false;
                    }
                });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.hideOverflowMenu();
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

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
                    Log.d("kk", "onAuthStateChanged:signed_in:" + user.getUid() + " username: " + user.getDisplayName());
                    loginbutton.setVisibility(View.GONE);
                    signinbutton.setVisibility(View.GONE);
                    iv.setVisibility(View.GONE);
                    enableDrawer();
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    if (!MainActivity.this.hasToken){
                        SpotifyAPI.authSpotify(MainActivity.this);
                    }

                    if (!rhloaded){
                        tv.setText("Experimente conectar-se com um amigo para ouvir uma música em sincronia!");
                        rh = new RequestHandler();
                        Log.d("rhtest",rh.teste());
                        rhloaded = true;
                    }

                    rh.setRequestHandlerListener(MainActivity.this);
                    navigationView.setNavigationItemSelectedListener(MainActivity.this);
                    View header = navigationView.getHeaderView(0);
                    sideBarUserName = (TextView) header.findViewById(R.id.sideBarUser);
                    sideBarEmail = (TextView) header.findViewById(R.id.sideBarEmail);
                    sideBarUserName.setText(user.getDisplayName());
                    sideBarEmail.setText(user.getEmail());
//                    tv.setVisibility(View.GONE);

                } else {
                    // User is signed out
                    bottomNavigationView.setVisibility(View.INVISIBLE);
                    thumbnail.setVisibility(View.GONE);
                    userState = false;
                    rh = null;
                    rhloaded = false;
                    Log.d("kk", "onAuthStateChanged:signed_out");
                    loginbutton.setVisibility(View.VISIBLE);
                    signinbutton.setVisibility(View.VISIBLE);
                    iv.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.VISIBLE);
                    partnerUserView.setVisibility(View.GONE);
                    disableDrawer();
                }

            }
        };


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        Inflater searchButton = Inflate r;
//        View cv = inflater.inflate(R.layout.content_main, null);
    }

    public void enableDrawer(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void disableDrawer(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void goToSearchActivity(){
        startActivity(new Intent(this, SearchActivity.class));
    }
    public void goToFriendsActivity(){
        startActivity(new Intent(this, FriendsActivity.class));
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
        rh.setStatus("offline");
        mPlayer.pause(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Error error) {
            }
        });
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
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
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

        if (id == R.id.nav_searchMusic) {
            startActivity(new Intent(this, SearchActivity.class));

        } else if (id == R.id.nav_friends) {
            startActivity(new Intent(this, FriendsActivity.class));

        } else if ( id == R.id.nav_logout){
            rh.setStatus("offline");
            FirebaseAuth.getInstance().signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("ACTIVITY","Returned to MainActivity via onStart");
        if (rh != null){
            rh.setStatus("online");
        }

        if (ConnectionHandler.getInstance() != null){
            ConnectionHandler.getInstance().setConnectionHandlerListener(this);
        }

        if (mAuthListener != null){
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private Handler mHandler = new Handler();

//    public void playSong(View view){
//        mPlayer.playUri(null, SpotifyAPI.getUri(),0,0);
//    }

    public void pauseSong(View view){
        ConnectionHandler.getInstance().onSongPause();


        /*Metadata metadata = mPlayer.getMetadata();
        Metadata.Track musica = metadata.currentTrack;
        Log.d("METADATA", metadata.toString());*/

    }

    public void resumeSong(View view){
        pauseButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.GONE);
        ConnectionHandler.getInstance().onSongResume();
    }

    public void playerSetup(){

//        playSong(null);
//        pauseSong(null);
        pauseButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
        musicProgress.setVisibility(View.VISIBLE);
        musicTimeRight.setVisibility(View.VISIBLE);
        musicTimeLeft.setVisibility(View.VISIBLE);

        if(thread != null){
            thread.interrupt();
        }

        musicTimeRight.setText(transformTime(SpotifyAPI.getSongDuration()));
        musicTimeLeft.setText("0:00");

    }

    public String transformTime(long durMS){
        long secLong = durMS/1000;
        int sec = (int) secLong;
        int min = sec/60;
        sec = sec % 60;
        String segundos = Integer.toString(sec);
        if(sec<10){
            segundos = "0"+segundos;
        }
        String duration = min + ":" + segundos;
        return duration;
    }

    public void playSong(String songuri){
        mPlayer.playUri(null, songuri,0,0);
    }

    public void drawSongThumbnail(String thumburl){
        Picasso.with(getApplicationContext()).load(thumburl).into(thumbnail);
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        rh.setStatus("online");
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
        if (Objects.equals(playerEvent.name(), "kSpPlaybackEventAudioFlush")){
            ConnectionHandler.getInstance().isReady();
            Log.d("PLAYER","Player is ready");
            mPlayer.pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(Error error) {

                }
            });
        }
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
        rh.setStatus("online");
        Log.d("ACTIVITY","Returned to MainActivity");
        if (ConnectionHandler.getInstance() != null){
            Log.d("MAIN_ACTIVITY","Resetting ConnectionHandlerListener");
            ConnectionHandler.getInstance().setConnectionHandlerListener(this);
        }
         //Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE && !this.hasToken) {
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
                        Log.d("mPlayer","mPlayer created");
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

    @Override
    public void onEvent() {
//        Snackbar.make(findViewById(R.id.progressBar2), "Existe uma solicitaçao para sincronizar", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
        Log.d("EVENTHANDLER", "Event fired on MainActivity");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    rh.showRequestAlert(MainActivity.this);
                }
            }
        });
    }

    public void showPartnerUser(){
        Log.d("SHOWPARTNERUSER","FUNCTION CALLED");
        if (RequestHandler.getPartnerUser() != null){
            Log.d("SHOWPARTNERUSER","FUNCTION ENTERED");
            partnerUserView.setVisibility(View.VISIBLE);
            partnerUserView.setText("Conectado com " + RequestHandler.getPartnerUser());
            tv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestAccepted() {
        Snackbar.make(this.getCurrentFocus(), "Seu pedido de sincronização foi aceito!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onConnectionHandlerCreated() {
        ConnectionHandler.getInstance().setConnectionHandlerListener(MainActivity.this);
    }

    @Override
    public void showPartnerName(String partnerName) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rhloaded){
            rh.clearRequests();
        }
        Log.d("REQUEST_HANDLER","Clearing requests from server");
        }

    @Override
    protected void onResume() {
        if (rhloaded){
            showPartnerUser();
            rh.setStatus("online");
        }
        super.onResume();
    }

    @Override
    public void onSongChanged(final Track track, String timeStamp) {
        tv.setVisibility(View.GONE);
        songNameView.setText(track.name);
        String artists = "";
        for (ArtistSimple artist : track.artists) {
            artists = artists + artist.name + ", " ;
        }
        artists = artists.substring(0,artists.length() - 2);
        artistNameView.setText(artists);
        playSong(track.uri);
        drawSongThumbnail(track.album.images.get(1).url);
        SpotifyAPI.setSongDuration(track.duration_ms);
        playerSetup();

//        alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
//        pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,alarmIntent,0);
//
////        DateFormat.getDateInstance().format(timeStamp);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(Long.parseLong(timeStamp));
//
//        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmMgr.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), pendingIntent);
//        Log.d("ALARM","Alarm set to " + timeStamp + "Current time: " + System.currentTimeMillis());

//        long dif = calendar.getTimeInMillis() - SystemClock.uptimeMillis();
//        long delay = calendar.getTimeInMillis() - Long.parseLong(timeStamp);
////        calendar.set(Calendar.MINUTE,0);
////        calendar.set(Calendar.SECOND,0);
////        calendar.set(Calendar.MILLISECOND,0);
//        calendar.set(Calendar.MILLISECOND, (int) (calendar.get(Calendar.MILLISECOND) + delay));
//        long start = calendar.getTimeInMillis() - dif + delay;

        Log.d("OnSongChanged", track.uri);
        Log.d("CURRENT TIMESTAMP",String.valueOf(System.currentTimeMillis()));
        Log.d("SERVER TIMESTAMP",timeStamp);
        Log.d("SystemClock",String.valueOf(SystemClock.uptimeMillis()));

//        long startTime = Long.parseLong(timeStamp);
//        Log.d("Calculated start", String.valueOf(start));
//        new android.os.Handler().postAtTime(playTrigger,start);
    }

//    @Override
//    public void onSongChanged(Track track, String timeStamp) {
//
//    }

    @Override
    public void onBothClientsReady() {
        Date start = new Date(Long.parseLong(ConnectionHandler.getInstance().getLastTimestamp()));
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("TIMERTASK","TIMERTASK FIRED");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String message = "Timer triggered";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        mPlayer.resume(new Player.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("PLAYER","Resuming playback because both players are ready");
                                flag = 1;
                                mStopHandler = false;
                                Runnable runnable = new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void run() {
                                        if (mPlayer.getPlaybackState().positionMs >= SpotifyAPI.getSongDuration()){
                                            mStopHandler = true;
                                        }

                                        Log.d("Tick","Task ticked");
                                        musicTimeLeft.setText(transformTime(mPlayer.getPlaybackState().positionMs));
                                        long tillFinish = SpotifyAPI.getSongDuration() - mPlayer.getPlaybackState().positionMs;
                                        musicTimeRight.setText(transformTime(tillFinish));
                                        long pos = mPlayer.getPlaybackState().positionMs;
                                        long total = SpotifyAPI.getSongDuration();
                                        long percent = pos*1000/total;
                                        int per = (int) percent;
                                        musicProgress.setProgress(per);
                                        if (!mStopHandler) {
                                            mHandler.postDelayed(this, 1000);
                                        }
                                    }
                                };
                                mHandler.post(runnable);

                            }
                            @Override
                            public void onError(Error error) {
                            }
                        });
                    }
                });
            }
        }, start );
    }

    @Override
    public void onStatusChanged(String status) {
        Log.d("OnStatusChanged",status);
        if (Objects.equals(status, "paused")){
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
            mPlayer.pause(null);
        } else if (Objects.equals(status, "playing")){
            mPlayer.resume(null);
            resumeButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);

        }
    }

//    thread =  new Thread(new Runnable() {
//        public void run() {
//            while (mPlayer.getPlaybackState().positionMs < SpotifyAPI.getSongDuration() && flag==1) {
//                // Update the progress bar
//                mHandler.post(new Runnable() {
//                    @RequiresApi(api = Build.VERSION_CODES.N)
//                    public void run() {
////                            mProgress.setProgress(mProgressStatus);
//                        musicTimeLeft.setText(transformTime(mPlayer.getPlaybackState().positionMs));
//                        long tillFinish = SpotifyAPI.getSongDuration() - mPlayer.getPlaybackState().positionMs;
//                        musicTimeRight.setText(transformTime(tillFinish));
//
//                        long pos = mPlayer.getPlaybackState().positionMs;
//                        long total = SpotifyAPI.getSongDuration();
//                        long percent = pos*1000/total;
//                        int per = (int) percent;
//                        musicProgress.setProgress(per, true);
//                    }
//                });
//            }
//        }
//    });
//                                thread.start();

}







