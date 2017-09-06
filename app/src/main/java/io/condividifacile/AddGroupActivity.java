package io.condividifacile;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddGroupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    private int numberOfLines = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            Button b = (Button) findViewById(R.id.create_group);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createGroup();
                }
            });

            ImageView add = (ImageView) findViewById(R.id.add_user_button);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addMoreUsers();
                }
            });

        }

    }

    private void createGroup(){

        final ProgressDialog addExpDialog = new ProgressDialog(AddGroupActivity.this,R.style.Theme_AppCompat_Dialog);
        addExpDialog.setIndeterminate(true);
        addExpDialog.setMessage("Creating group...");
        addExpDialog.show();

        final ArrayList<String> usersEmails = new ArrayList<>();
        final HashMap<String,String> members = new HashMap<>();

        EditText firstMember = (EditText) findViewById(R.id.add_user);
        String firstMail = firstMember.getText().toString();
        usersEmails.add(currentUser.getEmail());
        if(!firstMail.equals("")) {
            usersEmails.add(firstMail);
        }
        View tmpView;
        LinearLayout ll = (LinearLayout) findViewById(R.id.add_users_layout);
        for(int i =0; i<ll.getChildCount();i++){
            tmpView = ll.getChildAt(i);
            if(tmpView instanceof EditText){
                usersEmails.add(((EditText) tmpView).getText().toString());
            }
        }
        EditText nameText = (EditText) findViewById(R.id.group_name);
        final String groupName = nameText.getText().toString();

        final DatabaseReference usersRef = database.getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Check if user already has a group with that name
                boolean groupExists = false;
                for(DataSnapshot group : dataSnapshot.child(currentUser.getUid()).child("groups").getChildren()){
                    if(group.getKey().equalsIgnoreCase(groupName)){
                        groupExists = true;
                    }
                }

                boolean usersExist = false;
                if(!groupExists) {
                    //Check if user exists
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        for (int i = 0; i < usersEmails.size(); i++) {
                            if (user.child("email").getValue().equals(usersEmails.get(i))) {
                                String uid = user.getKey();
                                String name = user.child("name").getValue().toString();
                                members.put(name, uid);
                                usersEmails.remove(i);
                            }
                        }
                    }
                    if (usersEmails.size() == 0) {
                        usersExist = true;
                    }
                    //Creating new group balance inside each user
                    if (usersExist) {
                        final ArrayList<String> memberNames = new ArrayList<String>(members.keySet());
                        for (int j = 0; j < memberNames.size(); j++) {
                            String name = memberNames.get(j);
                            String uid = members.get(name);
                            for (int k = 0; k < memberNames.size(); k++) {
                                if (!memberNames.get(k).equals(name)) {
                                    usersRef.child(uid).child("groups").child(groupName).child(memberNames.get(k)).setValue(0);
                                }
                            }
                        }

                        //Adding group to groups
                        DatabaseReference groupsRef = database.getReference("groups");
                        Group newGroup = new Group(groupName, members, null, null);
                        groupsRef.push().setValue(newGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                addExpDialog.dismiss();
                                String message = currentUser.getDisplayName()+" added you to "+groupName;
                                for(int i = 0; i < memberNames.size(); i++){
                                    if(!memberNames.get(i).equals(currentUser.getDisplayName())){
                                        sendNotification(members.get(memberNames.get(i)),message,"Group created","notification");
                                    }
                                }
                                finish();
                            }
                        });
                    } else {
                        addExpDialog.dismiss();
                        Toast.makeText(AddGroupActivity.this, "A user with that email doesn't exist", Toast.LENGTH_LONG).show();
                    }
                } else {
                    addExpDialog.dismiss();
                    Toast.makeText(AddGroupActivity.this, "A group with that name already exists", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void addMoreUsers(){

        if(numberOfLines < 5) {
            LinearLayout ll = (LinearLayout) findViewById(R.id.add_users_layout);
            EditText et = new EditText(this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            et.setLayoutParams(p);
            et.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            et.setEms(10);
            et.setHint("e-Mail");
            et.setId(numberOfLines + 1);
            ll.addView(et);
            numberOfLines++;
        }
    }

    public static void sendNotification(String user_id,String message,String description,String type){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications").child(user_id);
        String pushKey = databaseReference.push().getKey();

        Notification notification = new Notification();
        notification.setDescription(description);
        notification.setMessage(message);
        notification.setUser_id(user_id);
        notification.setType(type);

        Map<String, Object> forumValues = notification.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(pushKey, forumValues);
        databaseReference.setPriority(ServerValue.TIMESTAMP);
        databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){

                }
            }
        });
    }
}
