package pesadadobatata.songsync;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.GridView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class SearchFriendsActivity extends AppCompatActivity implements RequestHandlerListener{
    private DatabaseReference mDatabase;
    private HashMap[] eita;
    private List<User> userNames;
    private String[] usernamesArray;
    private GridView list;
    private RequestHandler rh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        userNames = new LinkedList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchfriends);
        list = (GridView) findViewById(R.id.friendsResultList);
        rh = RequestHandler.getInstance();
        rh.setRequestHandlerListener(this);
        rh.setStatus("online");

        final android.widget.SearchView searchView = (android.widget.SearchView) findViewById(R.id.friendsearch_view);
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                Query search = mDatabase.orderByChild("username").startAt(query).endAt(query+"\uf8ff");
                search.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("DATA",String.valueOf(dataSnapshot.getChildrenCount()));
                        userNames.clear();

                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                            String uid = messageSnapshot.getKey();
                            String username = (String) messageSnapshot.child("username").getValue();
                            userNames.add(new User(uid, username));
                        }
                        showUsers();
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
//                search.
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Log.d("TEXT",newText);
                return false;
            }
        });


//        mDatabase.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                list.setAdapter(null);
//                userNames.clear();
//
//                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
//                    String uid =  messageSnapshot.getKey();
//                    String userName = (String) messageSnapshot.child("username").getValue();
//                    Log.d("name", uid);
//                    Log.d("message", userName);
//                    userNames.add(new Friend(uid, userName));
//                }
//
//                showUsers();
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Failed to read value
//                Log.w("Erou", "Failed to read value.", databaseError.toException());
//            }
//        });
    }

    @Override
    protected void onResume() {
        rh.setRequestHandlerListener(SearchFriendsActivity.this);
        rh.setStatus("online");
        super.onResume();
    }

    public void showUsers(){
        usernamesArray = new String[userNames.size()];
        for (int i=0;i<usernamesArray.length;i++){
            usernamesArray[i] = userNames.get(i).getUsername();
        }
        final List<String> alo = new ArrayList<String>(Arrays.asList(usernamesArray));
        final ArrayAdapter<String> gridViewArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alo);

        list.setAdapter(gridViewArrayAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                rh.checkAvaiability(userNames.get(position).getUid()).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (view.isEnabled()) {
                            if (Objects.equals(task.getResult(), "online")) {
                                rh.sendRequest(userNames.get(position).getUid(),userNames.get(position).getUsername());
                                Snackbar.make(view, "Solicitando sincronização com " + userNames.get(position).getUsername(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                view.setEnabled(false);
                                view.setClickable(false);
                                view.setFocusable(false);

                            } else {
                                Snackbar.make(view, "O usuário " + userNames.get(position).getUsername() + " parece estar offline", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    }
                });
//                Snackbar.make(view, "Solicitando sincronização com " + userNames.get(position).getUsername(), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d("BUTTON","finishing SearchFriends");
        finish();
    }

    @Override
    public void onEvent() {
//        Snackbar.make(findViewById(R.id.progressBar2), "Existe uma solicitaçao para sincronizar", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
        Log.d("EVENTHANDLER", "Event fired on SearchFriendsActivity");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!isFinishing()) {
                    rh.showRequestAlert(SearchFriendsActivity.this);
                }
            }
        });
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

    @Override
    protected void onPause() {
        rh.setStatus("offline");
        super.onPause();
    }
}

class User{
    public String UserID;
    public String userName;

    public User(String UserId, String userName){
        this.UserID = UserId;
        this.userName = userName;

    }
    public String getUsername(){
        return this.userName;
    }
    public String getUid(){ return this.UserID; }
}



