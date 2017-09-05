package io.condividifacile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

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

        Button create_btn = (Button) findViewById(R.id.create_group);
        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

    }

    private void createGroup(){

        final ProgressDialog addExpDialog = new ProgressDialog(AddGroupActivity.this,R.style.Theme_AppCompat_Dialog);
        addExpDialog.setIndeterminate(true);
        addExpDialog.setMessage("Creating group...");
        addExpDialog.show();

        EditText groupNameTxt = (EditText) findViewById(R.id.group_name);
        final String groupName = groupNameTxt.getText().toString();

        final ArrayList<String> usersEmails = new ArrayList<>();

        EditText firstMember = (EditText) findViewById(R.id.add_user);
        usersEmails.add(currentUser.getEmail());
        usersEmails.add(firstMember.getText().toString());
        View tmpView;
        LinearLayout ll = (LinearLayout) findViewById(R.id.add_users_layout);
        for(int i =0; i<ll.getChildCount();i++){
            tmpView = ll.getChildAt(i);
            if(tmpView instanceof EditText){
                usersEmails.add(((EditText) tmpView).getText().toString());
            }
        }

        final HashMap<String,String> members = new HashMap<>();

        //Adding user data to new group
        final DatabaseReference usersRef = database.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
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
                    if(usersEmails.size() == 0){
                        usersExist = true;
                    }

                    //Creating new group balance inside each user
                    if(usersExist) {
                        ArrayList<String> memberNames = new ArrayList<String>(members.keySet());
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
                                finish();
                            }
                        });
                    }else{
                        Toast.makeText(AddGroupActivity.this,"A user with that email doesn't exist",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //TODO sistema l' aggiunta del gruppo nel database (togli la chiave iniziale)

    }

    void addMoreUsers(){

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

}
