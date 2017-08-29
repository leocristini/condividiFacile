package io.condividifacile;

import android.os.Bundle;
import android.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddExpenseActivity extends AppCompatActivity {

    private String name;
    private ArrayList <Pair<String,String>> members;
    private ArrayList<Pair<String,Double>> userBalance;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        database = FirebaseDatabase.getInstance();
        final String selectedGroup = getIntent().getExtras().getString("selectedGroup");

        //Getting group members
        members = new ArrayList<>();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            name = user.getDisplayName();
            DatabaseReference groupsRef = database.getReference("groups/"+selectedGroup+"/members");
            groupsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String member = singleSnapshot.getKey();
                        String id = (String) singleSnapshot.getValue();
                        members.add(new Pair<String, String>(member,id));
                        addMemberBox(member);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        //Getting user balance
        getUserBalance(user.getUid(),selectedGroup);

        //Getting group categories
        final ArrayList<String> categories = new ArrayList<>();
        final DatabaseReference categoriesRef = database.getReference("groups/"+selectedGroup+"/categories");
        final AutoCompleteTextView categoryEdit = (AutoCompleteTextView) findViewById(R.id.categoryEdit);
        final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(AddExpenseActivity.this,android.R.layout.simple_spinner_dropdown_item,categories);
        categoryEdit.setThreshold(0);
        categoryEdit.setAdapter(categoryAdapter);
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


        final EditText amountText = (EditText) findViewById(R.id.amountEdit);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String formattedDate = df.format(c.getTime());

        Button completeBtn = (Button) findViewById(R.id.completeExp);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference expenseRef = database.getReference("groups/"+selectedGroup+"/expenses");
                float amount = Float.parseFloat(amountText.getText().toString());
                String category = categoryEdit.getText().toString();
                Expense exp = new Expense();
                exp.setCategory(category);
                if (!categories.contains(category)){
                    expenseRef.getParent().child("categories").child(category).setValue(true);
                }
                exp.setBuyer(name);
                exp.setDate(formattedDate);
                exp.setAmount(amount);

                LinearLayout boxesLayout = (LinearLayout) findViewById(R.id.boxes_layout);
                int n_boxes = boxesLayout.getChildCount();
                final ArrayList <String> selectedMembers = new ArrayList<String>();
                for(int i = 0; i < n_boxes; i++) {
                    CheckBox memberBox = (CheckBox) boxesLayout.getChildAt(i);
                    if(memberBox.isChecked()){
                        selectedMembers.add(memberBox.getText().toString());
                    }
                }

                final ArrayList<Pair<String,Double>> divisions = divideEqually(amount,selectedMembers);

                //Debug
                for(int i = 0; i < divisions.size(); i++){
                    if(!divisions.get(i).first.equalsIgnoreCase(user.getDisplayName())) {
                        Log.d("swag", "To " + divisions.get(i).first + ": " + divisions.get(i).second);
                    }else{
                        Log.d("swag", "To " + divisions.get(i).first + ": " + divisions.get(i).second);
                    }
                }

                exp.setDivision(divisions);
                expenseRef.push().setValue(exp);

                //TODO: update user balance
                final ArrayList<Pair<String,Double>> newUserBalance = new ArrayList<Pair<String, Double>>();
                for(int i = 0; i < userBalance.size(); i++){
                    for(int j = 0; j < divisions.size(); j++){
                        if(userBalance.get(i).first.equals(divisions.get(j).first)){
                            double oldBalance = userBalance.get(i).second;
                            double newBalance = Math.round((oldBalance + divisions.get(j).second)*100.0)/100.0;
                            Log.d("swag","Old balance for "+userBalance.get(i).first+" was "+oldBalance+", new balance is "+newBalance);
                            newUserBalance.add(new Pair<String, Double>(divisions.get(j).first,newBalance));
                        }
                    }
                }
                DatabaseReference usersRef = database.getReference("users");

                for(int i = 0; i < members.size(); i++){

                    //Updating balance for the logged user
                    if(members.get(i).first.equals(user.getDisplayName())){
                        for(int j = 0; j < newUserBalance.size(); j++){
                            usersRef.child(members.get(i).second).child("groups").child(selectedGroup).child(newUserBalance.get(j).first).setValue(newUserBalance.get(j).second);
                        }
                    }//else update balance for other users in the group
                    else{
                        double invertedBalance = 0;
                        for(int j = 0; j < newUserBalance.size(); j++){
                            if(newUserBalance.get(j).first.equals(members.get(i).first)){
                                invertedBalance = -newUserBalance.get(j).second;
                            }
                        }
                        usersRef.child(members.get(i).second).child("groups").child(selectedGroup).child(user.getDisplayName()).setValue(invertedBalance);
                    }
                }

                finish();
            }
        });

    }

    private void addMemberBox(String member){
        final LinearLayout boxesLayout = (LinearLayout) findViewById(R.id.boxes_layout);
        CheckBox box = new CheckBox(this);
        box.setId(members.indexOf(member));
        box.setText(member);
        box.setChecked(true);
        boxesLayout.addView(box);
    }

    // returns an ArrayList<Double> with the total divided in equal amounts as by the divider parameter
    public ArrayList<Pair<String,Double>> divideEqually(double total, ArrayList<String> members)
    {
        ArrayList<Pair<String,Double>> membersBalance = new ArrayList<>();
        int divider = members.size();
        for(int i = 0; i < members.size(); i++) {
            double amount = Math.round((total / divider) * 100.0) / 100.0;
            membersBalance.add(new Pair<String, Double>(members.get(i),amount));
            total -= amount;
            divider--;
        }

        return membersBalance;
    }

    //method to get user balance inside a group from DB
    private void getUserBalance(String userId, String groupName){

        userBalance = new ArrayList<>();
        DatabaseReference balanceRef = database.getReference("users/"+userId+"/groups/"+groupName);
        balanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    String member = singleSnapshot.getKey();
                    double balance = (double) singleSnapshot.getValue();
                    android.util.Pair<String, Double> memberBalance = new android.util.Pair<String, Double>(member,balance);
                    userBalance.add(memberBalance);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
