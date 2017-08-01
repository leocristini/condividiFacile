package io.condividifacile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.GridLayout.Spec;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class AddExpenseActivity extends AppCompatActivity {

    private String [] xData = {"Alimenti","Bollette","Internet"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        //fake data
        String [] groups = {"Group1","Group2","Group3"};
        final String [] members = {"Leonardo","Silvio","Gianmaria"};

        final Spinner groupSpinner = (Spinner) findViewById(R.id.groupSpinner);
        ArrayAdapter<String> groupsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,groups);
        groupSpinner.setAdapter(groupsAdapter);
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO: query to get group members
                addMembersBoxes(members);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final Spinner categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,xData);
        categorySpinner.setAdapter(categoryAdapter);

    }

    private void addMembersBoxes(String [] members){
        final LinearLayout boxesLayout = (LinearLayout) findViewById(R.id.boxes_layout);
        boxesLayout.removeAllViews();
        for(int i = 0; i < members.length; i++){
            CheckBox box = new CheckBox(this);
            box.setText(members[i]);
            box.setChecked(true);
            boxesLayout.addView(box);
        }
    }
}
