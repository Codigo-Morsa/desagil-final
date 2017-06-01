package pesadadobatata.songsync;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;

import com.google.firebase.database.ChildEventListener;
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



public class SearchFriendsActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private HashMap[] eita;
    private List<User> userNames;
    private String[] usernamesArray;
    private GridView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        userNames = new LinkedList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchfriends);
        list = (GridView) findViewById(R.id.friendsResultList);

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
                        Log.d("name", userNames.toString());
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

    public void showUsers(){
        usernamesArray = new String[userNames.size()];
        for (int i=0;i<usernamesArray.length;i++){
            usernamesArray[i] = userNames.get(i).getUsername();
        }
        final List<String> alo = new ArrayList<String>(Arrays.asList(usernamesArray));
        final ArrayAdapter<String> gridViewArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alo);
        list.setAdapter(gridViewArrayAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, "Solicitando sincronização com " + userNames.get(position).getUsername(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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



