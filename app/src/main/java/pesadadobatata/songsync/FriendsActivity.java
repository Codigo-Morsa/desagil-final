package pesadadobatata.songsync;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;


public class FriendsActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private HashMap[] eita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth =  FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final ValueEventListener valueEventListener = mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                Map<String, String> value = dataSnapshot.getValue(Map.class);
                Map<HashMap, HashMap> value = (Map<HashMap, HashMap>) dataSnapshot.getValue();
                HashMap<HashMap, HashMap> users = new HashMap<>();
                Log.d("Database now", "Value is: " + value);
                for (Map.Entry<HashMap, HashMap> entry : value.entrySet()) {
                    users.put(entry.getKey(), entry.getValue());
                    System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue());
                    Log.d("alo", entry.getClass().toString());
//                    System.out.printf(entry.getKey().getClass().toString());
//                    System.out.printf(String.valueOf()entry.getValue().getClass()));
                }

//                Iterator<Map.Entry<, HashMap>>
//                Iterator<Map.Entry<HashMap<String, String>, HashMap<String, String>>> parent = alo.entrySet().iterator();
//                while (parent.hasNext()) {
//                    Map.Entry<String, HashMap<String, String>> parentPair = parent.next();
//                    System.out.println("parentPair.getKey() :   " + parentPair.getKey() + " parentPair.getValue()  :  " + parentPair.getValue());
//
////                    Iterator<Map.Entry<String, String>> child = (parentPair.getValue()).entrySet().iterator();
////                    while (child.hasNext()) {
////                        Map.Entry childPair = child.next();
////                        System.out.println("childPair.getKey() :   " + childPair.getKey() + " childPair.getValue()  :  " + childPair.getValue());
////
////                        child.remove(); // avoids a ConcurrentModificationException
////                    }
//
//                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Erou", "Failed to read value.", error.toException());
            }
        });
    }

}
