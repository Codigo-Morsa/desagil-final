package pesadadobatata.songsync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;


public class FriendsActivity extends AppCompatActivity implements RequestHandlerListener{
    private DatabaseReference mDatabase;
    private HashMap[] eita;
    private List<Friend> userNames;
    private String[] usernamesArray;
    private GridView list;
    private RequestHandler rh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        userNames = new LinkedList<>();
        super.onCreate(savedInstanceState);
        setTitle("Lista de amigos");

        rh = RequestHandler.getInstance();
        try{
            rh.setRequestHandlerListener(FriendsActivity.this);
            rh.setStatus("online");
        } catch (NullPointerException e){
            Log.d("Weird","Weird error on FriendActivity");
        }


        setContentView(R.layout.activity_friends);
        list = (GridView) findViewById(R.id.friendsList);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(FriendsActivity.this,SearchFriendsActivity.class);
                startActivity(intent);
            }
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list.setAdapter(null);
                userNames.clear();

                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String uid =  messageSnapshot.getKey();
                    String userName = (String) messageSnapshot.child("username").getValue();

                    userNames.add(new Friend(uid, userName));
                }

                showUsers();
                mDatabase.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w("Erou", "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void showUsers(){
        usernamesArray = new String[userNames.size()];
        for (int i=0;i<usernamesArray.length;i++){
            usernamesArray[i] = userNames.get(i).getUserName();
        }

        final List<String> alo = new ArrayList<String>(Arrays.asList(usernamesArray));
        final ArrayAdapter<String> gridViewArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alo);
        list.setAdapter(gridViewArrayAdapter);
    }

    public void onStart() {
        super.onStart();
        Log.d("ACTIVITY","Returned to FriendsActivity via onStart");
        rh = RequestHandler.getInstance();
        rh.setRequestHandlerListener(FriendsActivity.this);
        rh.setStatus("online");
    }

    @Override
    public void onEvent() {
//        Snackbar.make(findViewById(R.id.progressBar2), "Existe uma solicitaçao para sincronizar", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
        Log.d("EVENTHANDLER", "Event fired on activity FriendsActivity");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rh.showRequestAlert(FriendsActivity.this);
                }
            });
    }

    @Override
    protected void onPause() {
        super.onPause();
        rh.setStatus("offline");
    }

    @Override
    public void onRequestAccepted() {
        Snackbar.make(list, "Seu pedido de sincronização foi aceito!", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        super.onDismissed(transientBottomBar, event);
                    }
                }).show();
    }

    @Override
    public void onConnectionHandlerCreated() {

    }

    @Override
    public void showPartnerName(String partnerName) {

    }

}


class Friend{
    private String UserID;
    private String userName;

    public Friend(String UserId, String userName){
            this.UserID = UserID;
            this.userName = userName;

        }
    public String getUserName(){
        return this.userName;
    }
    }
