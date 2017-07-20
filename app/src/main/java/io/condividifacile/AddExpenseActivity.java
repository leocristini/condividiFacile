package io.condividifacile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AddExpenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        //fake data
        String [] groups = {"Group1","Group2","Group3"};
        String [] members = {"Leonardo","Silvio","Gianmaria"};

        final Spinner groupSpinner = (Spinner) findViewById(R.id.groupSpinner);
        ArrayAdapter<String> groupsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,groups);
        groupSpinner.setAdapter(groupsAdapter);
        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
}
