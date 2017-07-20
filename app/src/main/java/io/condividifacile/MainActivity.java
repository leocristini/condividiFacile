package io.condividifacile;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //this is the main activity for the project, must contain the sliding window and the group page
    private FirebaseAuth mAuth;

    private ExpandOrCollapse mAnimationManager;
    private ArrayList<Integer> colors;
    //test data
    private String groupName = "Merde";
    private String [] xData = {"Alimenti","Bollette","Internet"};
    private float [] yData = {100,40,25};
    //private String [] members = {"Leonardo","Silvio","Gianmaria"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        final RelativeLayout revealLayout = (RelativeLayout) findViewById(R.id.transitionLayout);
        revealLayout.setVisibility(View.INVISIBLE);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        final float maxRadius = Math.max(dm.heightPixels, dm.widthPixels);
        final FloatingActionButton expand_btn = (FloatingActionButton) findViewById(R.id.expandbtn);
        final RelativeLayout expandableLayout = (RelativeLayout) findViewById(R.id.expandableLayout);
        final TextView testView = (TextView) findViewById(R.id.textView);

        final boolean[] isExpanded = {false};


        final PieChart pieChart = (PieChart) findViewById(R.id.piechart);

        //setting chart
        pieChart.setUsePercentValues(true);
        Description dscr = new Description();
        dscr.setText("Spese recenti");
        pieChart.setDescription(dscr);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(10);
        pieChart.setTransparentCircleRadius(10);
        pieChart.setHoleColor(Color.TRANSPARENT);

        //chart value selected listener

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            public void onValueSelected(Entry e, Highlight h) {
                //display message on value selected
                if(e==null){
                    return;
                }

                PieEntry pieE = (PieEntry) e;
                int clickedIndex = 0;
                for(int i = 0; i < xData.length; i++){
                    if(xData[i].equals(pieE.getLabel())){
                        clickedIndex = i;
                    }
                }

                Toast.makeText(MainActivity.this, pieE.getLabel()+": "+pieE.getValue()+"€", Toast.LENGTH_SHORT).show();
                final Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
                detailsIntent.putExtra("categoria",pieE.getLabel());
                detailsIntent.putExtra("totale",pieE.getValue());
                detailsIntent.putExtra("color",colors.get(clickedIndex));
                Log.i("(x,y)","x: "+h.getXPx()+" , "+"y: "+h.getYPx());
                //animation

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    revealLayout.setBackgroundColor(colors.get(clickedIndex));
                    revealLayout.setVisibility(View.VISIBLE);
                    Animator circularReveal = ViewAnimationUtils.createCircularReveal(revealLayout, (int) h.getXPx(), (int) h.getYPx(), 0, maxRadius);

                    circularReveal.setDuration(600);

                    circularReveal.start();

                    circularReveal.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startActivity(detailsIntent);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                }


            }

            @Override
            public void onNothingSelected() {

            }
        });


        //adding data to chart
        ArrayList<PieEntry> values = new ArrayList<>();
        for(int i = 0; i < yData.length; i++){
            values.add(new PieEntry(yData[i],xData[i]));
        }

        PieDataSet dataSet = new PieDataSet(values,"Categorie spese");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        //Customizing legend
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        l.setXEntrySpace(7);
        l.setYEntrySpace(5);

        //Setting colors to chart
        colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        //instantiate pieData here
        PieData data = new PieData();
        data.setDataSet(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextColor(Color.GRAY);
        data.setValueTextSize(11f);

        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.invalidate();

        //From here on is the expandableLayout on the bottom
        testView.setText("BALANCE: 12€");
        mAnimationManager = new ExpandOrCollapse();

        expand_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isExpanded[0]) {
                    mAnimationManager.expand(expandableLayout, 1000, 450);
                    isExpanded[0] = true;
                    expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }else {
                    mAnimationManager.collapse(expandableLayout, 1000, 200);
                    isExpanded[0] = false;
                    expand_btn.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                }
            }
        });

        final FloatingActionButton addExpBtn = (FloatingActionButton) findViewById(R.id.addExp);
        addExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addExpenseIntent = new Intent(MainActivity.this,AddExpenseActivity.class);
                addExpenseIntent.putExtra("group",groupName);
                startActivity(addExpenseIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        RelativeLayout revealLayout = (RelativeLayout) findViewById(R.id.transitionLayout);
        revealLayout.setVisibility(View.INVISIBLE);
        super.onResume();
    }
}
