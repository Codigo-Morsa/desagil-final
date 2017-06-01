package pesadadobatata.songsync;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import samplesearch.Search;

class RequestHandler{

    private static RequestHandler rh;
    private RequestHandlerListener rhl;

    static RequestHandler getInstance() {
        return rh;
    }

    public RequestHandler() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/requests");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (rhl != null) {
                    rhl.onEvent();
                }

                if (dataSnapshot.exists()){
                    Log.d("REQUEST_HANDLER","Requests exist");
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        String uid = messageSnapshot.getKey();
//                        mDatabase.updateChildren(Map<String, Object>);
                    }
                } else {
                    Log.d("REQUEST_HANDLER","No new requests");
//                    String[] empty = {"teste"};
//                    mDatabase.setValue(new ArrayList<>(Arrays.asList(empty)));
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rh = this;
        }

    public String teste(){
        return "kkeaemen";
    }

    public void setRequestHandlerListener (RequestHandlerListener rhl){
        this.rhl = rhl;
    }


}


