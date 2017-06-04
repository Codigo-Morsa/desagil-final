package pesadadobatata.songsync;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SimpleTimeZone;

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
        Log.d(TAG,"Connection Handler Started, Reference is "+ connectionKey);
        ch = this;

        HashMap<String,String> playback = new HashMap<>();
        playback.put("status","paused");
        playback.put("songuri","none");
        playback.put("imguri","none");
        playback.put("playtime", "none");

        connectionRef.child("ready").setValue("0").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                connectionRef.child("ready").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String readycount = dataSnapshot.getValue(String.class);
                        Log.d("READYCOUNTER",readycount);
                        if (Objects.equals(readycount, "2")){
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

        connectionRef.child("playback").setValue(playback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // listens for changes on the playback tab
                connectionRef.child("playback").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String songuri = dataSnapshot.child("songuri").getValue(String.class);
                        String timeStamp = dataSnapshot.child("playtime").getValue(String.class);
                        String songimg = dataSnapshot.child("imguri").getValue(String.class);
                        Log.d(TAG,songuri);
                        if (!Objects.equals(songuri, "none")){
                            Log.d("playtime", timeStamp);
                            Log.d("getTime", String.valueOf(System.currentTimeMillis()));
                            chl.onSongChanged(songuri, songimg, timeStamp);
                            lastTimestamp = timeStamp;
                        }
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            String songurl = snapshot.;
//                            Log.d(TAG,"Song: " + songurl);
//                        }
                        Log.d(TAG,"PLAYBACK CHANGED");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
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
    public void selectSong(String songUri, String imgUri){
        Map<String,Object> playback = new HashMap<>();
        playback.put("status","paused");
        playback.put("songuri",songUri);
        playback.put("imguri",imgUri);
        playback.put("playtime", String.valueOf(System.currentTimeMillis() + 5000));
        connectionRef.child("playback").updateChildren(playback);
    }

}
