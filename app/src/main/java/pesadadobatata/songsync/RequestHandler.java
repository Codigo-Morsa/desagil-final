package pesadadobatata.songsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

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


    public RequestHandler() {
        // Checks is there is a new request on the user requests array
        statusRef = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/status");
        requestsList = new LinkedList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/requests");
        Query childQuery = mDatabase.orderByKey().limitToLast(1);
        setStatus("online");
        mDatabase.removeValue();

        childQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("REQUEST_HANDLER", "New request");
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String uid = messageSnapshot.getKey();
                    Log.d("REQUEST_RECIEVER",uid);
//                        mDatabase.updateChildren(Map<String, Object>);
                }
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


    public void showRequestAlert(Activity activity){
        // Shows a new request dialog whichever activity the user is
        final String syncid = this.lastrequest.getKey();
        String syncrequester = this.lastrequest.child("username").getValue(String.class);
        final DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/requests/"+syncid);

        new AlertDialog.Builder(activity)
                .setTitle("Novo pedido de sync")
                .setMessage("Sync ID:" + syncid + "\n" + "o usuário " + syncrequester + " solicitou uma sincronização com você")
                .setCancelable(false)
                .setPositiveButton("Aceitar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestRef.child("status").setValue("accepted");
                    }
                })
                .setNegativeButton("Recusar", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        requestRef.child("status").setValue("rejected");
                    }
                }).show();
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


    public void sendRequest(final String destinationuid) {
        // Deals with sent requests and its response;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("users/" + destinationuid + "/requests");

        final HashMap<String, String> request = new HashMap<>();
        request.put("uid", mAuth.getCurrentUser().getUid());
        request.put("username", mAuth.getCurrentUser().getDisplayName());
        request.put("status", "sent");


        final DatabaseReference requestKeyRef = requestRef.push();
        String requestKey = requestKeyRef.getKey();
        requestKeyRef.setValue(request);

        Request requestObj = new Request(requestKey, mAuth.getCurrentUser().getUid(), destinationuid);
        requestsList.push(requestObj);

        Log.d("REQUEST_HANDLER", "New request Key:" + requestKey + " sent to " + destinationuid);
        requestRef.child(requestKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String requestKey = dataSnapshot.getKey();
                String requestStatus = dataSnapshot.child("status").getValue(String.class);

                Log.d("REQUEST_"+requestKey,requestStatus);

                if (Objects.equals(requestStatus, "rejected")){
                    Log.d("REF",dataSnapshot.getRef().toString());
                    dataSnapshot.getRef().removeEventListener(this);
                    dataSnapshot.getRef().removeValue();
                } else if (Objects.equals(requestStatus, "accepted")){
                    Log.d("REF",dataSnapshot.getRef().toString());
                    dataSnapshot.getRef().removeEventListener(this);
                    dataSnapshot.getRef().removeValue();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        requestRef.child(requestKey).child("status").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                try {
//                String result = dataSnapshot.child("status").getValue(String.class);
//                    if (Objects.equals(result, "accepted")) {
//                        // Request has been accepted
//                        Log.d("Request_result", "Request accepted");
//                        dataSnapshot.getRef().getParent().addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                String clientUid = dataSnapshot.child("uid").getValue().toString();
//                                String clientUsername = dataSnapshot.child("username").getValue().toString();
//                                FirebaseUser self = FirebaseAuth.getInstance().getCurrentUser();
//                                openConnection(dataSnapshot.getKey(),self.getUid(),self.getDisplayName(),clientUid,clientUsername);
//                                requestKeyRef.removeValue();
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//
//                        });
//
////                        try {
//////                            requestsList.pop();
////                        } catch (NoSuchElementException e) {
////                            Log.d("ERR", "Empty requestsList");
////                        }
//
//                    } else if (Objects.equals(result, "rejected")) {
//                        Log.d("Request_result", "Request rejected");
//                        requestKeyRef.removeValue();
//
////                        try {
////                            requestsList.pop();
////                        } catch (NoSuchElementException e) {
////                            Log.d("ERR", "Empty requestsList");
////                        }
//                    }
////                    Log.d("RequestsList", Arrays.toString(requestsList.toArray()));
////                } catch (NullPointerException e) {
////                    Log.d("ERR", "NullPointer on ValueListener. probably cleared all remaining requests @ pause");
////                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//
//        });
    }

    public void openConnection(String ckey,String hostid, String hostusr, String clientid, String clientusr){
        Log.d("Params",ckey);
        DatabaseReference connectionRef = FirebaseDatabase.getInstance().getReference().getRoot().child("connections");
//        connectionRef.setValue();
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

