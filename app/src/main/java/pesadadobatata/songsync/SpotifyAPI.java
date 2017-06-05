package pesadadobatata.songsync;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * Created by fredcurti on 27/05/17.
 */

public class SpotifyAPI {
    private static final SpotifyAPI ourInstance = new SpotifyAPI();
    private static String eaemen = "HUEHAUEHU";
    private static final String CLIENT_ID = "ae616876bd5545a8918eac8ce5af7c5f";
    private static final String REDIRECT_URI = "songsync://callback";
    private static final int REQUEST_CODE = 1337;
    private static String spotifyToken = "";
    private static String songUri = "";
    private static String songThumbnailUrl = "";
    private static SpotifyService spotify;

    static SpotifyAPI getInstance() {
        return ourInstance;
    }

    static public String getString() {
        return eaemen;
    }

    public SpotifyAPI() {

    }

    static public String getClientID(){
        return CLIENT_ID;
    }

    static public void setUri(String uri){
        Log.d("New Song Set: ", uri);
        songUri = uri;
    }

    static public String getUri(){
        return songUri;
    }

    static public void setThumbnailUrl(String url){
        songThumbnailUrl = url;
    }

    static public String getThumbnailUrl(){
        return songThumbnailUrl;
    }


    static public void setSpotifyToken(String token) {
        spotifyToken = token;
    }

    static public String getSpotifyToken() {
        return spotifyToken;
    }

    static public void authSpotify(Activity thisactivity) {
        Log.d("SPOTIFYAPI:", "Authenticating Spotify");
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(thisactivity, REQUEST_CODE, request);
    }

    static public void startWebAPI() {
        Log.d("SPOTIFYAPI:", "Started webAPI with token " + getSpotifyToken());
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(getSpotifyToken());
        spotify = api.getService();

//        spotify.getMySavedTracks(new SpotifyCallback<Pager<SavedTrack>>() {
//            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
//                Log.d("SPOTIFY-REQUEST:",savedTrackPager.toString());
//            }
//
//            public void failure(SpotifyError error) {
//                Log.d("ERROR:",error.getErrorDetails().toString());
//            }
//        });

    }

    static public SpotifyService getSpotifyService(){
        return spotify;
    }

    static public List<Track> searchSongs(String songname) {
        return spotify.searchTracks(songname).tracks.items;

    }
}
