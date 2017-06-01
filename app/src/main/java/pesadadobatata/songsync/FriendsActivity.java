package pesadadobatata.songsync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import samplesearch.SearchActivity;


public class FriendsActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private HashMap[] eita;
    private List<Friend> userNames;
    private String[] usernamesArray;
    private GridView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        userNames = new LinkedList<>();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friends);
        list = (GridView) findViewById(R.id.friendsList);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final List<String> emptylist = new ArrayList<String>();
        final ArrayAdapter<String> emptygridViewArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emptylist);


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
                    Log.d("name", uid);
                    Log.d("message", userName);
                    userNames.add(new Friend(uid, userName));
                }

                showUsers();

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
