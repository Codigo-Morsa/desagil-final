package pesadadobatata.songsync;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SimpleTimeZone;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by fredcurti on 03/06/17.
 */

public class ConnectionHandler {
    private static ConnectionHandler ch;
    private ConnectionHandlerListener chl;
    private DatabaseReference connectionRef;
    public static ConnectionHandler getInstance() {
        return ch;
    }
    private String TAG = "CONNECTION_HANDLER";
    private String lastTimestamp;

    public ConnectionHandler(final String connectionKey) {
//        RequestHandler rh = RequestHandler.getInstance();
//        rh.destroyRequestHandler();

        connectionRef = FirebaseDatabase.getInstance().getReference().child("connections").child(connectionKey);
        Log.d(TAG, "Connection Handler Started, Reference is " + connectionKey);
        ch = this;

        connectionRef.child("ready").setValue("0").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                connectionRef.child("ready").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String readycount = dataSnapshot.getValue(String.class);
                        Log.d("READYCOUNTER", readycount);
                        if (Objects.equals(readycount, "2")) {
                            chl.onBothClientsReady();
                            connectionRef.child("ready").setValue("0");
//                            connectionRef.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        connectionRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class) != null){
                    String status = dataSnapshot.getValue(String.class);
                    Log.d("SONG STATUS LISTENER",status);
                    if (Objects.equals(status, "playing")){
                        chl.onStatusChanged(status);
                    } else if (Objects.equals(status, "paused")){
                        chl.onStatusChanged(status);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        connectionRef.child("playback").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 2){
                    Track track = dataSnapshot.child("track").getValue(Track.class);
                    String playtime = dataSnapshot.child("playtime").getValue(String.class);
                    Log.d("track obtained",track.toString());
                    lastTimestamp = playtime;
                    Log.d("playtime", playtime);
                    chl.onSongChanged(track, playtime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void onSongPause(){
        connectionRef.child("status").setValue("paused");
    }

    public void onSongResume(){
        connectionRef.child("status").setValue("playing");
    }

    public String getLastTimestamp(){
        return lastTimestamp;
    }

    public void isReady(){
        connectionRef.child("ready").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentCount = dataSnapshot.getValue(String.class);
                Integer countInt = Integer.parseInt(currentCount);
                countInt++;
                String result = countInt.toString();
                connectionRef.child("ready").setValue(result);
                connectionRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setConnectionHandlerListener (ConnectionHandlerListener chl){
        this.chl = chl;
    }
//
//    public void selectSong(String songUri, String imgUri , String songName, String ){
//        Map<String,Object> playback = new HashMap<>();
//        playback.put("songname",songName);
//        playback.put("duration",)
//        playback.put("status","paused");
//        playback.put("songuri",songUri);
//        playback.put("imguri",imgUri);
//        playback.put("playtime", String.valueOf(System.currentTimeMillis() + 5000));
////        playback.put("playtime", String.valueOf(ServerValue.TIMESTAMP));
//        connectionRef.child("playback").updateChildren(playback);
//    }

    public void selectSong2(Track track){
        connectionRef.child("playback").child("track").setValue(track);
        connectionRef.child("playback").child("playtime").setValue(String.valueOf(System.currentTimeMillis() + 5000));
        connectionRef.child("status").setValue("playing");
    }

}
