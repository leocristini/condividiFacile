package io.condividifacile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddExpenseActivity extends AppCompatActivity {

    private String selectedCategory;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String selectedGroup = getIntent().getExtras().getString("selectedGroup");

        //Getting group members
        final ArrayList <String> members = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            name = user.getDisplayName();
            String email = user.getEmail();
            String uid = user.getUid();
            DatabaseReference groupsRef = database.getReference("groups/"+selectedGroup+"/members");
            groupsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String member = singleSnapshot.getKey();
                        members.add(member);
                        addMemberBox(member);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Getting group categories
        final ArrayList<String> categories = new ArrayList<>();
        final DatabaseReference categoriesRef = database.getReference("groups/"+selectedGroup+"/categories");
        final Spinner categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(AddExpenseActivity.this,android.R.layout.simple_spinner_dropdown_item,categories);
        categorySpinner.setAdapter(categoryAdapter);
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String category = singleSnapshot.getKey();
                    categories.add(category);
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final EditText amountText = (EditText) findViewById(R.id.amountEdit);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String formattedDate = df.format(c.getTime());

        Button completeBtn = (Button) findViewById(R.id.completeExp);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference expenseRef = database.getReference("groups/"+selectedGroup+"/expenses");
                float amount = Float.parseFloat(amountText.getText().toString());
                Expense exp = new Expense();
                exp.setCategory(selectedCategory);
                exp.setBuyer(name);
                exp.setDate(formattedDate);
                exp.setAmount(amount);
                expenseRef.push().setValue(exp);
                finish();
            }
        });

    }

    private void addMemberBox(String member){
        final LinearLayout boxesLayout = (LinearLayout) findViewById(R.id.boxes_layout);
        CheckBox box = new CheckBox(this);
        box.setText(member);
        box.setChecked(true);
        boxesLayout.addView(box);
    }
}
