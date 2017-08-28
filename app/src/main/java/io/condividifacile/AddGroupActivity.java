package io.condividifacile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

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

    void createGroup(){

        EditText gorupName = (EditText) findViewById(R.id.group_name);
        String groupName = gorupName.getText().toString();

        ArrayList<String> usersString = new ArrayList<>();
        View tmpView;
        LinearLayout ll = (LinearLayout) findViewById(R.id.add_users_layout);
        for(int i =0; i<ll.getChildCount();i++){
            tmpView = ll.getChildAt(i);
            if(tmpView instanceof EditText){
                usersString.add(((EditText) tmpView).getText().toString());
            }
            Log.d("username",usersString.get(i));
        }



        ArrayList<Expense> spese = new ArrayList<>();
        ArrayList<String> categorie = new ArrayList<>();

        Group newGroup = new Group(groupName,usersString,spese,categorie);



        DatabaseReference dbReference = database.getReference("groups");
        dbReference.push().setValue(newGroup);
        Log.d("group",groupName);
    }

    void addMoreUsers(){
        LinearLayout ll = (LinearLayout)findViewById(R.id.add_users_layout);
        EditText et = new EditText(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(p);
        et.setHint("e-Mail");
        et.setId(numberOfLines + 1);
        ll.addView(et,numberOfLines);
        numberOfLines++;
    }

}
