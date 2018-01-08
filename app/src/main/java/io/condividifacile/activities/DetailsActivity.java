package io.condividifacile.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

import io.condividifacile.utils.ExpandOrCollapse;
import io.condividifacile.R;
import io.condividifacile.data.Expense;

public class DetailsActivity extends AppCompatActivity {

    private ExpandOrCollapse mAnimationManager;

    //test data
    private ArrayList <Expense> expenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String categoria = getIntent().getExtras().getString("categoria");
        getSupportActionBar().setTitle(categoria);
        int color = getIntent().getExtras().getInt("color");

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setBackgroundColor(color);

        //creating test data
        expenses = (ArrayList<Expense>) getIntent().getExtras().getSerializable("expenses");

        addTableRows(expenses, categoria);
    }


    //function to add data to the view
    private void addTableRows(ArrayList<Expense> exps, String category) {
        mAnimationManager = new ExpandOrCollapse();

        final TableLayout table = (TableLayout) findViewById(R.id.tableView);
        int categoryExp = 0;

        for (int i = 0; i < exps.size(); i++) {

            if(exps.get(i).getCategory().equalsIgnoreCase(category)) {
                categoryExp++;
                final TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.table_row_item, null);
                if(categoryExp%2 == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    row.setBackground(getResources().getDrawable(R.drawable.row_background));
                }
                TextView tv1 = (TextView) row.findViewById(R.id.cell1);
                tv1.setText(exps.get(i).getDate());
                TextView tv2 = (TextView) row.findViewById(R.id.cell2);
                tv2.setText(exps.get(i).getBuyer());
                TextView tv3 = (TextView) row.findViewById(R.id.cell3);
                tv3.setText("" + exps.get(i).getAmount());
                table.addView(row);
                registerForContextMenu(row);

                final GridLayout expandableLayout = (GridLayout) getLayoutInflater().inflate(R.layout.expandable_details, null);
                TextView buyerView = (TextView) expandableLayout.findViewById(R.id.buyerText);
                buyerView.setText(exps.get(i).getBuyer());
                TextView amountView = (TextView) expandableLayout.findViewById(R.id.amountText);
                amountView.setText(""+exps.get(i).getAmount());
                TextView dateView = (TextView) expandableLayout.findViewById(R.id.dateText);
                dateView.setText(exps.get(i).getDate());
                TextView descrView = (TextView) expandableLayout.findViewById(R.id.descrText);
                String divided = "";
                if(exps.get(i).getDivision() != null) {
                    ArrayList <String> name = new ArrayList<>(exps.get(i).getDivision().keySet());
                    for(int j = 0; j < name.size(); j++) {
                        divided = divided + name.get(j).split(" ")[0] + " ";
                    }
                    descrView.setText(divided);
                }
                table.addView(expandableLayout);

                final boolean[] isExpanded = {false};
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!isExpanded[0]) {
                            DisplayMetrics dm = getResources().getDisplayMetrics();
                            expandableLayout.setVisibility(View.VISIBLE);
                            mAnimationManager.expand(expandableLayout, 500, dm.heightPixels/3);
                            isExpanded[0] = true;
                        }else{
                            mAnimationManager.collapse(expandableLayout, 500, 0);
                            isExpanded[0] = false;
                        }
                    }
                });
            }

        }
    }

}
