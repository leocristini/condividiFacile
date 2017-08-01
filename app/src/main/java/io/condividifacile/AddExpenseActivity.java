package io.condividifacile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.GridLayout.Spec;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddExpenseActivity extends AppCompatActivity {

    private String [] xData = {"Alimenti","Bollette","Internet"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        //fake data
        final String [] groups = {"testGroup"};

        //TODO: query to get groups user belongs to

        final Spinner groupSpinner = (Spinner) findViewById(R.id.groupSpinner);
        ArrayAdapter<String> groupsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,groups);
        groupSpinner.setAdapter(groupsAdapter);
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO: query to get group members

                final LinearLayout boxesLayout = (LinearLayout) findViewById(R.id.boxes_layout);
                boxesLayout.removeAllViews();
                DatabaseReference myRef = database.getReference(groups[0]+"/members");
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int i = 0;
                        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                            String member = singleSnapshot.getKey();
                            Log.d("swag","member: "+member);
                            i++;
                            addMemberBox(member);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final Spinner categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,xData);
        categorySpinner.setAdapter(categoryAdapter);

    }

    private void addMemberBox(String member){
        final LinearLayout boxesLayout = (LinearLayout) findViewById(R.id.boxes_layout);
        CheckBox box = new CheckBox(this);
        box.setText(member);
        box.setChecked(true);
        boxesLayout.addView(box);
    }
}
