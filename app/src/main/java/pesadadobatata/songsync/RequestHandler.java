package pesadadobatata.songsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import samplesearch.Search;

import static android.content.ContentValues.TAG;

class RequestHandler{

    private static RequestHandler rh;
    private RequestHandlerListener rhl;
    private DataSnapshot lastrequest;
    static RequestHandler getInstance() {
        return rh;
    }
    private LinkedList<Request> requestsList;
    DatabaseReference statusRef;
    static String partnerUser;

    public RequestHandler() {
        // Checks is there is a new request on the user requests array
        statusRef = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/status");
        requestsList = new LinkedList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/requests");
//        Query childQuery = mDatabase.orderByKey().limitToLast(1);
        setStatus("online");
        mDatabase.removeValue();

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("REQUEST_HANDLER", "New request received from " + dataSnapshot.child("username").getValue(String.class));
                partnerUser = dataSnapshot.child("username").getValue(String.class);
//                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
//                    String uid = messageSnapshot.getKey();
//                    Log.d("REQUEST_RECIEVER",uid);
////                        mDatabase.updateChildren(Map<String, Object>);
//                }
                lastrequest = dataSnapshot;
                rhl.onEvent();
//                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rh = this;

        }

    public String teste(){
        return "Request handler is running";
    }

    public void setRequestHandlerListener (RequestHandlerListener rhl){
        this.rhl = rhl;
    }

    public void setStatus(String status){
        statusRef.setValue(status);
    }

    public void connect(String syncid){
        final DatabaseReference connectionRef = FirebaseDatabase.getInstance().getReference().child("connections").child(syncid);
        Log.d("CONNECT","Trying to connect to " + connectionRef.getKey());
        final HashMap<String,String> client = new HashMap<>();
        client.put("username",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        connectionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("DATA_CHANGED",ds.getKey());
                    if (Objects.equals(ds.getKey(), "host")){
                        partnerUser = ds.child("username").getValue(String.class);
                        Log.d("CONNECT","Host is connected, connecting client");
                        connectionRef.child("client").setValue(client);
                        new ConnectionHandler(connectionRef.getKey());
                        connectionRef.removeEventListener(this);
                        rhl.onConnectionHandlerCreated();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showRequestAlert(final Activity activity){
        // Shows a new request dialog whichever activity the user is
        final String syncid = this.lastrequest.getKey();
        String syncrequester = this.lastrequest.child("username").getValue(String.class);
        final DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/requests/"+syncid);

        final AlertDialog alert = new AlertDialog.Builder(activity)
                .setTitle("Novo pedido de sync")
//                .setMessage("O usuário " + syncrequester + " solicitou uma sincronização com você")
                .setMessage(Html.fromHtml("O usuário <b>" + syncrequester + "</b> solicitou uma sincronização com você"))
                .setCancelable(false)
                .setPositiveButton("Aceitar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestRef.child("status").setValue("accepted");
                        connect(syncid);
                        Intent intent = new Intent(activity,MainActivity.class);
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton("Recusar", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        requestRef.child("status").setValue("rejected");
                    }
                }).create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
            }
        });
        alert.show();
    }

    public Task<String> checkAvaiability(final String destinationId) {
        final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users/")
                .child(destinationId+"/status")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String status = dataSnapshot.getValue().toString();
                        if (null == status) {
                            tcs.setResult(null);
                        } else {
                            tcs.setResult(status);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("TaskError",databaseError.getMessage());
                    }

                });
        return tcs.getTask();
    }

    static String getPartnerUser(){
        return partnerUser;
    }


    public void sendRequest(final String destinationUid, final String destinationUsername) {
        // Deals with sent requests and its response;
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        partnerUser = destinationUsername;

        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("users/" + destinationUid + "/requests");

        final HashMap<String, String> request = new HashMap<>();
        request.put("uid", mAuth.getCurrentUser().getUid());
        request.put("username", mAuth.getCurrentUser().getDisplayName());
        request.put("destusr", destinationUsername);
        request.put("status", "sent");

        final DatabaseReference requestKeyRef = requestRef.push();
        String requestKey = requestKeyRef.getKey();
        requestKeyRef.setValue(request);

        Request requestObj = new Request(requestKey, mAuth.getCurrentUser().getUid(), destinationUid);
        requestsList.push(requestObj);

        Log.d("REQUEST_HANDLER", "New request Key:" + requestKey + " sent to " + destinationUid);
        requestRef.child(requestKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String requestKey = dataSnapshot.getKey();
                String requestStatus = dataSnapshot.child("status").getValue(String.class);
                Log.d("REQUEST_",requestKey + " " + requestStatus);

                if (Objects.equals(requestStatus, "rejected")){
                    Log.d("REF",dataSnapshot.getRef().toString());
                    dataSnapshot.getRef().removeEventListener(this);
                    dataSnapshot.getRef().removeValue();

                } else if (Objects.equals(requestStatus, "accepted")){
//                    Log.d("REF",);
                    openConnection(requestKey,mAuth.getCurrentUser().getUid(),mAuth.getCurrentUser().getDisplayName(),
                            dataSnapshot.getRef().getParent().getParent().getKey(),dataSnapshot.child("destusr").getValue(String.class));
                    dataSnapshot.getRef().removeEventListener(this);
                    dataSnapshot.getRef().removeValue();
                    rhl.onRequestAccepted();
                    new ConnectionHandler(requestKey);
                    rhl.onConnectionHandlerCreated();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void openConnection(String ckey,String hostid, String hostusr, String clientid, String clientusr){
        Log.d("Params",ckey + hostid + hostusr + clientid + clientusr);
        DatabaseReference connectionRef = FirebaseDatabase.getInstance().getReference().getRoot().child("connections").child(ckey);
        HashMap<String,String> host = new HashMap<>();
        host.put("username",hostusr);
        host.put("uid",hostid);
        connectionRef.child("host").setValue(host);
//        connectionRef.setValue();
    }

    public void destroyRequestHandler(){
        Log.d("REQUEST_HANDLER","Request Handler has been destroyed") ;
        rh = null;

    }

    public void clearRequests(){
        Log.d("REQUEST_HANDLER","Clearing requests and setting status offline");
        setStatus("offline");
        for (Request request : requestsList) {
//            Log.d("CR","Trying to remove requests @ users/"+request.getDestination()+"/requests/"+request.getRequestKey());
            FirebaseDatabase.getInstance().getReference("users/" + request.getDestination() + "/requests/" + request.getRequestKey()).removeValue();
        }
        requestsList.clear();
    }

}

